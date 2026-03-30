const state = {
  token: localStorage.getItem('pulseify_token') || '',
  user: JSON.parse(localStorage.getItem('pulseify_user') || 'null'),
  songs: [],
  playlists: [],
};

const el = {
  authScreen: document.getElementById('authScreen'),
  appScreen: document.getElementById('appScreen'),
  loginTab: document.getElementById('loginTab'),
  signupTab: document.getElementById('signupTab'),
  loginForm: document.getElementById('loginForm'),
  signupForm: document.getElementById('signupForm'),
  logoutBtn: document.getElementById('logoutBtn'),
  currentUsername: document.getElementById('currentUsername'),
  currentRole: document.getElementById('currentRole'),
  adminPanel: document.getElementById('adminPanel'),
  globalPlaylistRow: document.getElementById('globalPlaylistRow'),
  playlistForm: document.getElementById('playlistForm'),
  playlistName: document.getElementById('playlistName'),
  playlistIsGlobal: document.getElementById('playlistIsGlobal'),
  playlistList: document.getElementById('playlistList'),
  songGrid: document.getElementById('songGrid'),
  songCount: document.getElementById('songCount'),
  toast: document.getElementById('toast'),
  searchInput: document.getElementById('searchInput'),
  artistFilter: document.getElementById('artistFilter'),
  titleFilter: document.getElementById('titleFilter'),
  genreFilter: document.getElementById('genreFilter'),
  albumFilter: document.getElementById('albumFilter'),
  yearFilter: document.getElementById('yearFilter'),
  songForm: document.getElementById('songForm'),
  clearSongBtn: document.getElementById('clearSongBtn'),
  songId: document.getElementById('songId'),
  songTitle: document.getElementById('songTitle'),
  songArtist: document.getElementById('songArtist'),
  songAlbum: document.getElementById('songAlbum'),
  songYear: document.getElementById('songYear'),
  songGenre: document.getElementById('songGenre'),
  songImageFile: document.getElementById('songImageFile'),
  songLyrics: document.getElementById('songLyrics'),
  loginUsername: document.getElementById('loginUsername'),
  loginPassword: document.getElementById('loginPassword'),
  signupUsername: document.getElementById('signupUsername'),
  signupPassword: document.getElementById('signupPassword'),
  signupIsAdmin: document.getElementById('signupIsAdmin'),
};

function setAuth(token, user) {
  state.token = token || '';
  state.user = user || null;

  if (state.token) {
    localStorage.setItem('pulseify_token', state.token);
  } else {
    localStorage.removeItem('pulseify_token');
  }

  if (state.user) {
    localStorage.setItem('pulseify_user', JSON.stringify(state.user));
  } else {
    localStorage.removeItem('pulseify_user');
  }
}

async function api(path, options = {}) {
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  if (state.token) {
    headers.Authorization = `Bearer ${state.token}`;
  }

  const response = await fetch(path, { ...options, headers });
  const text = await response.text();
  const data = text ? JSON.parse(text) : {};

  if (!response.ok) {
    throw new Error(data.error || 'Request failed');
  }

  return data;
}

function showToast(message, isError = false) {
  el.toast.textContent = message;
  el.toast.classList.remove('hidden', 'success', 'error');
  el.toast.classList.add(isError ? 'error' : 'success');
  clearTimeout(showToast.timer);
  showToast.timer = setTimeout(() => {
    el.toast.classList.add('hidden');
    el.toast.classList.remove('success', 'error');
  }, 2600);
}

function showApp() {
  const loggedIn = Boolean(state.token && state.user);
  el.authScreen.classList.toggle('hidden', loggedIn);
  el.appScreen.classList.toggle('hidden', !loggedIn);

  if (loggedIn) {
    el.currentUsername.textContent = state.user.username;
    el.currentRole.textContent = state.user.isAdmin ? 'Administrator account' : 'Standard account';
    el.adminPanel.classList.toggle('hidden', !state.user.isAdmin);
    el.globalPlaylistRow.classList.toggle('hidden', !state.user.isAdmin);
  }
}

function switchTab(mode) {
  const login = mode === 'login';
  el.loginTab.classList.toggle('active', login);
  el.signupTab.classList.toggle('active', !login);
  el.loginForm.classList.toggle('hidden', !login);
  el.signupForm.classList.toggle('hidden', login);
}

function getFilteredSongs() {
  const search = el.searchInput.value.trim().toLowerCase();
  const artist = el.artistFilter.value.trim().toLowerCase();
  const title = el.titleFilter.value.trim().toLowerCase();
  const genre = el.genreFilter.value.trim().toLowerCase();
  const album = el.albumFilter.value.trim().toLowerCase();
  const year = el.yearFilter.value.trim();

  return state.songs.filter((song) => {
    const haystack = [song.title, song.artist, song.album, song.genre, String(song.year), song.lyrics || '']
      .join(' ')
      .toLowerCase();

    return (!search || haystack.includes(search))
      && (!artist || (song.artist || '').toLowerCase().includes(artist))
      && (!title || (song.title || '').toLowerCase().includes(title))
      && (!genre || (song.genre || '').toLowerCase().includes(genre))
      && (!album || (song.album || '').toLowerCase().includes(album))
      && (!year || String(song.year) === year);
  });
}

function songById(songId) {
  return state.songs.find((song) => song.id === songId);
}

function playlistOptionsMarkup() {
  if (!state.playlists.length) {
    return '<option value="">No playlist yet</option>';
  }

  return [
    '<option value="">Choose playlist</option>',
    ...state.playlists.map((playlist) => `<option value="${playlist.id}">${escapeHtml(playlist.name)}${playlist.global ? ' (Global)' : ''}</option>`),
  ].join('');
}

function playlistTypeBadge(playlist) {
  return `<span class="playlist-badge ${playlist.global ? 'global' : 'personal'}">${playlist.global ? 'Global' : 'Personal'}</span>`;
}

function renderSongs() {
  const songs = getFilteredSongs();
  el.songCount.textContent = `${songs.length} song${songs.length === 1 ? '' : 's'}`;

  if (!songs.length) {
    el.songGrid.innerHTML = '<div class="empty-state">No songs match the current filters.</div>';
    return;
  }

  el.songGrid.innerHTML = songs.map((song) => {
    const imageFile = song.imageFile || 'default.svg';
    return `
      <article class="song-card">
        <img src="/song-images/${encodeURIComponent(imageFile)}" alt="${escapeHtml(song.title)} cover art" />
        <div>
          <div class="song-title">${escapeHtml(song.title)}</div>
          <div class="song-meta">
            ${escapeHtml(song.artist)}<br>
            ${escapeHtml(song.album || 'Unknown album')} • ${escapeHtml(song.genre || 'Unknown genre')} • ${song.year || 'N/A'}
          </div>
        </div>
        <div class="song-actions">
          <select data-song-id="${song.id}" class="playlist-select">
            ${playlistOptionsMarkup()}
          </select>
          <button class="secondary-btn compact add-to-playlist-btn" data-song-id="${song.id}">Add</button>
          ${state.user?.isAdmin ? `
            <button class="ghost-btn compact edit-song-btn" data-song-id="${song.id}">Edit</button>
            <button class="danger-btn compact delete-song-btn" data-song-id="${song.id}">Delete</button>
          ` : ''}
        </div>
      </article>
    `;
  }).join('');
}

function renderPlaylists() {
  if (!state.playlists.length) {
    el.playlistList.innerHTML = '<div class="empty-state">Create your first playlist.</div>';
    return;
  }

  el.playlistList.innerHTML = state.playlists.map((playlist) => {
    const songs = playlist.songIds.map(songById).filter(Boolean);
    return `
      <article class="playlist-card">
        <div class="playlist-card-header">
          <div>
            <strong>${escapeHtml(playlist.name)}</strong>
            <div class="playlist-subline">
              ${playlistTypeBadge(playlist)}
              <small>${songs.length} song${songs.length === 1 ? '' : 's'}</small>
            </div>
          </div>
          <div class="song-actions">
            <button class="ghost-btn compact rename-playlist-btn" data-playlist-id="${playlist.id}">Rename</button>
            <button class="danger-btn compact delete-playlist-btn" data-playlist-id="${playlist.id}">Delete</button>
          </div>
        </div>
        <div class="playlist-song-list">
          ${songs.length ? songs.map((song) => `
            <div class="playlist-song-item">
              <span>${escapeHtml(song.title)} — ${escapeHtml(song.artist)}</span>
              <button class="ghost-btn compact remove-song-btn" data-playlist-id="${playlist.id}" data-song-id="${song.id}">Remove</button>
            </div>
          `).join('') : '<small>No songs in this playlist yet.</small>'}
        </div>
      </article>
    `;
  }).join('');
}

function fillSongForm(song) {
  el.songId.value = song.id || '';
  el.songTitle.value = song.title || '';
  el.songArtist.value = song.artist || '';
  el.songAlbum.value = song.album || '';
  el.songYear.value = song.year || '';
  el.songGenre.value = song.genre || '';
  el.songImageFile.value = song.imageFile || '';
  el.songLyrics.value = song.lyrics || '';
}

function clearSongForm() {
  fillSongForm({});
}

function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

async function refreshSongs() {
  state.songs = await api('/api/songs');
  renderSongs();
  renderPlaylists();
}

async function refreshPlaylists() {
  if (!state.token) {
    state.playlists = [];
    return;
  }
  state.playlists = await api('/api/playlists');
  renderPlaylists();
  renderSongs();
}

async function initializeAppData() {
  await refreshSongs();
  if (state.token) {
    await refreshPlaylists();
  }
}

async function ensureSession() {
  if (!state.token) {
    showApp();
    switchTab('login');
    return;
  }

  try {
    state.user = await api('/api/auth/me');
    setAuth(state.token, state.user);
    showApp();
    await initializeAppData();
  } catch {
    setAuth('', null);
    showApp();
    switchTab('login');
  }
}

el.loginTab.addEventListener('click', () => switchTab('login'));
el.signupTab.addEventListener('click', () => switchTab('signup'));

el.loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const data = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: el.loginUsername.value,
        password: el.loginPassword.value,
      }),
    });
    setAuth(data.token, data.user);
    showApp();
    await initializeAppData();
    showToast('Logged in successfully');
    el.loginForm.reset();
  } catch (error) {
    showToast(error.message, true);
  }
});

el.signupForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const data = await api('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify({
        username: el.signupUsername.value,
        password: el.signupPassword.value,
        isAdmin: el.signupIsAdmin.checked,
      }),
    });
    setAuth(data.token, data.user);
    showApp();
    await initializeAppData();
    showToast(data.user.isAdmin ? 'Admin account created' : 'Account created');
    el.signupForm.reset();
  } catch (error) {
    showToast(error.message, true);
  }
});

el.logoutBtn.addEventListener('click', async () => {
  try {
    await api('/api/auth/logout', { method: 'POST' });
  } catch (_) {
    // ignore logout failure
  }
  setAuth('', null);
  state.playlists = [];
  showApp();
  switchTab('login');
  showToast('Logged out');
});

el.playlistForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    await api('/api/playlists', {
      method: 'POST',
      body: JSON.stringify({
        name: el.playlistName.value,
        isGlobal: state.user?.isAdmin ? el.playlistIsGlobal.checked : false,
      }),
    });
    el.playlistForm.reset();
    await refreshPlaylists();
    showToast('Playlist created');
  } catch (error) {
    showToast(error.message, true);
  }
});

el.songForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const payload = {
    title: el.songTitle.value,
    artist: el.songArtist.value,
    album: el.songAlbum.value,
    year: el.songYear.value,
    genre: el.songGenre.value,
    imageFile: el.songImageFile.value,
    lyrics: el.songLyrics.value,
  };

  try {
    if (el.songId.value) {
      await api(`/api/admin/songs/${el.songId.value}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });
      showToast('Song updated');
    } else {
      await api('/api/admin/songs', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      showToast('Song added');
    }
    clearSongForm();
    await refreshSongs();
  } catch (error) {
    showToast(error.message, true);
  }
});

el.clearSongBtn.addEventListener('click', clearSongForm);

[
  el.searchInput,
  el.artistFilter,
  el.titleFilter,
  el.genreFilter,
  el.albumFilter,
  el.yearFilter,
].forEach((input) => input.addEventListener('input', renderSongs));

el.songGrid.addEventListener('click', async (event) => {
  const addBtn = event.target.closest('.add-to-playlist-btn');
  const editBtn = event.target.closest('.edit-song-btn');
  const deleteBtn = event.target.closest('.delete-song-btn');

  if (addBtn) {
    const songId = addBtn.dataset.songId;
    const select = el.songGrid.querySelector(`.playlist-select[data-song-id="${songId}"]`);
    const playlistId = select?.value;
    if (!playlistId) {
      showToast('Choose a playlist first', true);
      return;
    }
    try {
      await api(`/api/playlists/${playlistId}/songs`, {
        method: 'POST',
        body: JSON.stringify({ songId }),
      });
      await refreshPlaylists();
      showToast('Song added to playlist');
    } catch (error) {
      showToast(error.message, true);
    }
    return;
  }

  if (editBtn) {
    const song = songById(editBtn.dataset.songId);
    if (song) {
      fillSongForm(song);
      showToast('Loaded song into admin form');
      el.songTitle.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
    return;
  }

  if (deleteBtn) {
    const confirmed = window.confirm('Delete this song globally?');
    if (!confirmed) {
      return;
    }
    try {
      await api(`/api/admin/songs/${deleteBtn.dataset.songId}`, { method: 'DELETE' });
      await refreshSongs();
      await refreshPlaylists();
      showToast('Song deleted');
    } catch (error) {
      showToast(error.message, true);
    }
  }
});

el.playlistList.addEventListener('click', async (event) => {
  const removeBtn = event.target.closest('.remove-song-btn');
  const deleteBtn = event.target.closest('.delete-playlist-btn');
  const renameBtn = event.target.closest('.rename-playlist-btn');

  try {
    if (removeBtn) {
      await api(`/api/playlists/${removeBtn.dataset.playlistId}/songs/${removeBtn.dataset.songId}`, {
        method: 'DELETE',
      });
      await refreshPlaylists();
      showToast('Song removed');
      return;
    }

    if (deleteBtn) {
      const confirmed = window.confirm('Delete this playlist?');
      if (!confirmed) {
        return;
      }
      await api(`/api/playlists/${deleteBtn.dataset.playlistId}`, { method: 'DELETE' });
      await refreshPlaylists();
      showToast('Playlist deleted');
      return;
    }

    if (renameBtn) {
      const current = state.playlists.find((playlist) => playlist.id === renameBtn.dataset.playlistId);
      const name = window.prompt('New playlist name:', current?.name || '');
      if (!name) {
        return;
      }
      await api(`/api/playlists/${renameBtn.dataset.playlistId}`, {
        method: 'PUT',
        body: JSON.stringify({ name }),
      });
      await refreshPlaylists();
      showToast('Playlist renamed');
    }
  } catch (error) {
    showToast(error.message, true);
  }
});

showApp();
ensureSession();
