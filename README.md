# Project Dreams

Project Dreams is an open-source Android package downloader and installer. It interfaces directly with the Google Play Store API to fetch, install, and update applications across different regional storefronts (Global and Japan).

## Core Functions

- **Direct API Fetching:** Packages are retrieved directly from Google Play servers using GPlayAPI.
- **Region Management:** Bypasses regional storefront restrictions, allowing simultaneous installation of Global (GL) and Japanese (JP) application packages.
- **Dynamic Configuration:** Supports manual package ID resolution for applications not indexed by standard API queries.
- **Telemetry-Free:** Open-source architecture with zero analytics or tracking implementation.

## Technical Disclaimer

- **No Asset Hosting:** The application operates strictly as an API client wrapper. No APKs, OBBs, or proprietary assets are hosted, stored, or distributed by this repository.
- **Dependencies:** Relies on AuroraOSS's PlayAPI implementation for authentication and network operations.

## Credits

- **[AuroraStore](https://gitlab.com/AuroraOSS/AuroraStore):** Core GPlayAPI implementation for network and authentication protocols.
