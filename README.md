# Catalog-Project Upgraded to Music-Catalog-App Functionality

This version keeps the Catalog-Project visual style and dataset, but upgrades it to match the working feature depth of `music-catalog-app`.

## What this version now supports

- Login and logout
- Sign up for normal accounts
- Sign up for admin accounts from the sign-up screen
- Session restore after refresh using local storage
- Song catalog with search and filtering by:
  - title
  - artist
  - genre
  - album
  - year
  - lyrics through the main search box
- Playlist management:
  - create playlists
  - rename playlists
  - delete playlists
  - add songs to playlists
  - remove songs from playlists
- Admin controls:
  - add songs
  - edit songs
  - delete songs
  - create global playlists
- Dynamic image support using files in `src/main/resources/public/images`
- CSV persistence for users, songs, and playlists
- Backward-compatible CSV reading for the older Catalog-Project headers and the newer music-catalog-app headers

## Run

```bash
mvn compile exec:java
```

Then open:

```text
http://localhost:4570
```

## Default seeded admin

- Username: `admin`
- Password: `admin123`

## Data files used by this build

- `src/main/resources/private/users.csv`
- `src/main/resources/private/songs.csv`
- `src/main/resources/private/playlists.csv`
- `src/main/resources/public/images`

## Notes

- Existing Catalog-Project songs and images are preserved.
- When song or user data is rewritten, it is normalized to the newer standardized headers.
- The admin sign-up checkbox is intentionally open because that was explicitly requested. It is useful for demos, but not secure for production.