// Central front-end state object.
// This keeps the most important client-side data in one place.
const state = {
  // Reuse any saved token so the user can stay logged in after a refresh.
  token: localStorage.getItem('pulseify_token') || '',

  // Reuse the last known user object if one was saved previously.
  user: JSON.parse(localStorage.getItem('pulseify_user') || 'null'),

  // Songs are loaded from the API after login or session restore.
  songs: [],
};

// Cache DOM elements once so the code does not repeatedly query the page.
const el = {
  authScreen: document.getElementById('authScreen'),
  appScreen: document.getElementById('appScreen'),
  loginTab: document.getElementById('loginTab'),
  signupTab: document.getElementById('signupTab'),
  loginForm: document.getElementById('loginForm'),
  signupForm: document.getElementById('signupForm'),
  loginUsername: document.getElementById('loginUsername'),
  loginPassword: document.getElementById('loginPassword'),
  signupUsername: document.getElementById('signupUsername'),
  signupPassword: document.getElementById('signupPassword'),
  currentUsername: document.getElementById('currentUsername'),
  currentRole: document.getElementById('currentRole'),
  logoutBtn: document.getElementById('logoutBtn'),
  searchInput: document.getElementById('searchInput'),
  artistFilter: document.getElementById('artistFilter'),
  titleFilter: document.getElementById('titleFilter'),
  genreFilter: document.getElementById('genreFilter'),
  albumFilter: document.getElementById('albumFilter'),
  yearFilter: document.getElementById('yearFilter'),
  songGrid: document.getElementById('songGrid'),
  songCount: document.getElementById('songCount'),
  toast: document.getElementById('toast'),
};

// Switches the auth card between login mode and sign-up mode.
function setView(mode) {
  const loginMode = mode === 'login';

  // Highlight the correct tab.
  el.loginTab.classList.toggle('active', loginMode);
  el.signupTab.classList.toggle('active', !loginMode);

  // Show the matching form and hide the other one.
  el.loginForm.classList.toggle('hidden', !loginMode);
  el.signupForm.classList.toggle('hidden', loginMode);
}

// Stores a successful login or sign-up response locally.
function saveSession(payload) {
  state.token = payload.token;
  state.user = payload.user;

  localStorage.setItem('pulseify_token', state.token);
  localStorage.setItem('pulseify_user', JSON.stringify(state.user));
}

// Clears all locally stored authentication data.
function clearSession() {
  state.token = '';
  state.user = null;

  localStorage.removeItem('pulseify_token');
  localStorage.removeItem('pulseify_user');
}

// Generic helper for talking to the back-end API.
async function api(url, options = {}) {
  // Start with JSON headers and then merge in any custom headers.
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };

  // Send the current login token when one exists.
  if (state.token) headers.Authorization = `Bearer ${state.token}`;

  // Make the HTTP request.
  const response = await fetch(url, { ...options, headers });

  // Read the response body as text first.
  const text = await response.text();

  // Convert it to JSON when there is content.
  const data = text ? JSON.parse(text) : {};

  // Throw an Error for failed requests so callers can use try/catch.
  if (!response.ok) throw new Error(data.error || 'Request failed');

  return data;
}

// Escapes HTML-sensitive characters before inserting text into innerHTML.
function escapeHtml(value) {
  return String(value ?? '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Shows a small success or error message in the top-right corner.
function toast(message, isError = false) {
  el.toast.textContent = message;
  el.toast.className = `toast ${isError ? 'error' : 'success'}`;

  // Hide the toast automatically after a short delay.
  setTimeout(() => { el.toast.className = 'toast hidden'; }, 2200);
}

// Reads the current values from all filter inputs.
function currentFilters() {
  return {
    search: el.searchInput.value.toLowerCase().trim(),
    artist: el.artistFilter.value.toLowerCase().trim(),
    title: el.titleFilter.value.toLowerCase().trim(),
    genre: el.genreFilter.value.toLowerCase().trim(),
    album: el.albumFilter.value.toLowerCase().trim(),
    year: el.yearFilter.value.trim(),
  };
}

// Returns only the songs that match every active filter.
function filteredSongs() {
  const filters = currentFilters();

  return state.songs.filter((song) => {
    // Create one combined searchable string for the broad search box.
    const haystack = [song.title, song.artist, song.album, song.genre, song.year].join(' ').toLowerCase();

    // Every active filter must pass for the song to be kept.
    return (!filters.search || haystack.includes(filters.search))
      && (!filters.artist || song.artist.toLowerCase().includes(filters.artist))
      && (!filters.title || song.title.toLowerCase().includes(filters.title))
      && (!filters.genre || song.genre.toLowerCase().includes(filters.genre))
      && (!filters.album || song.album.toLowerCase().includes(filters.album))
      && (!filters.year || String(song.year) === filters.year);
  });
}

// Renders the current filtered song list into the page.
function renderSongs() {
  const songs = filteredSongs();

  // Update the small song count label above the grid.
  el.songCount.textContent = `${songs.length} song${songs.length === 1 ? '' : 's'}`;

  // Show a friendly message when no results match.
  if (!songs.length) {
    el.songGrid.innerHTML = '<div class="empty-state">No songs match the current filters.</div>';
    return;
  }

  // Build the song cards as HTML.
  el.songGrid.innerHTML = songs.map((song) => `
    <article class="song-card">
      <img src="/song-images/${encodeURIComponent(song.imageFile || 'default.svg')}" alt="${escapeHtml(song.title)} cover art" />
      <div>
        <div class="song-title">${escapeHtml(song.title)}</div>
        <div class="song-meta">${escapeHtml(song.artist)}<br>${escapeHtml(song.album || 'Unknown album')} • ${escapeHtml(song.genre || 'Unknown genre')} • ${song.year || 'N/A'}</div>
      </div>
    </article>
  `).join('');
}

// Downloads the latest song list from the API and then renders it.
async function refreshSongs() {
  state.songs = await api('/api/songs');
  renderSongs();
}

// Tries to restore a previous session using the saved token.
async function restoreSession() {
  if (!state.token) return false;

  try {
    // Ask the server who the current token belongs to.
    state.user = await api('/api/auth/me');

    // Refresh the cached user object with the server-approved version.
    localStorage.setItem('pulseify_user', JSON.stringify(state.user));
    return true;
  } catch (error) {
    // If the token is invalid or expired, clear it locally.
    clearSession();
    return false;
  }
}

// Shows either the login screen or the main app screen.
function renderApp() {
  const loggedIn = !!state.token && !!state.user;

  el.authScreen.classList.toggle('hidden', loggedIn);
  el.appScreen.classList.toggle('hidden', !loggedIn);

  // When logged in, fill in the sidebar account details.
  if (loggedIn) {
    el.currentUsername.textContent = state.user.username;
    el.currentRole.textContent = state.user.isAdmin ? 'Administrator account' : 'Standard account';
  }
}

// Clicking the auth tabs just changes which form is visible.
el.loginTab.addEventListener('click', () => setView('login'));
el.signupTab.addEventListener('click', () => setView('signup'));

// Login form submission.
el.loginForm.addEventListener('submit', async (event) => {
  event.preventDefault();

  try {
    // Send the login request and store the returned session.
    saveSession(await api('/api/auth/login', { method: 'POST', body: JSON.stringify({ username: el.loginUsername.value, password: el.loginPassword.value }) }));

    renderApp();
    await refreshSongs();
    toast('Logged in');
    el.loginForm.reset();
  } catch (error) {
    toast(error.message, true);
  }
});

// Sign-up form submission.
el.signupForm.addEventListener('submit', async (event) => {
  event.preventDefault();

  try {
    // Create the account and immediately save the returned session.
    saveSession(await api('/api/auth/signup', { method: 'POST', body: JSON.stringify({ username: el.signupUsername.value, password: el.signupPassword.value }) }));

    renderApp();
    await refreshSongs();
    toast('Account created');
    el.signupForm.reset();
  } catch (error) {
    toast(error.message, true);
  }
});

// Logout action.
el.logoutBtn.addEventListener('click', async () => {
  // The server logout request is attempted first, but even if it fails,
  // the local session is still cleared.
  try { await api('/api/auth/logout', { method: 'POST' }); } catch (error) {}

  clearSession();
  renderApp();
  toast('Logged out');
});

// Any change in any filter input immediately re-renders the visible song list.
[el.searchInput, el.artistFilter, el.titleFilter, el.genreFilter, el.albumFilter, el.yearFilter]
  .forEach((input) => input.addEventListener('input', renderSongs));

// Self-running startup function.
// It restores the UI, checks for an existing session, and loads songs if needed.
(async function init() {
  setView('login');
  const ok = await restoreSession();
  renderApp();
  if (ok) await refreshSongs();
})();
