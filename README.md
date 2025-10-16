# GumroadStats

An Android app to track and monitor your Gumroad payouts directly from your device.

![gumroad](https://github.com/user-attachments/assets/c2d7f4df-dfae-4a3d-b656-1b50359bc976)


## Features

- View all your Gumroad payouts in a clean, modern interface
- Display payout amounts, currency, and status
- Show payment processor details (Stripe/PayPal)
- Display creation and processing dates
- Secure access token storage with DataStore
- Auto-refresh with configurable intervals
- Home screen widget for quick payout overview
- Material 3 design with dark mode support

## Setup

1. **Get your Gumroad Access Token:**
   - Visit your [Gumroad Applications page](https://app.gumroad.com/applications)
   - Create a new OAuth application
   - Generate an access token with `view_payouts` scope
   - Copy the access token

2. **Run the app:**
   - Open the project in Android Studio
   - Build and run the app
   - Enter your access token when prompted
   - Your payouts will load automatically

## Architecture

Built with modern Android development practices following MVVM architecture:

### Data Layer
- **Models**: Payout data classes for API responses
- **API Service**: Retrofit interface for Gumroad API
- **Repository**: Data operations and caching
- **Preferences**: DataStore for secure token storage

### UI Layer
- **ViewModel**: State management and business logic
- **Screens**: Jetpack Compose UI components
- **Widget**: Home screen widget for quick access

### Libraries
- Retrofit - REST API client
- Gson - JSON serialization
- OkHttp - HTTP client with logging
- Jetpack Compose - Modern declarative UI
- Material 3 - Latest Material Design
- DataStore - Preferences storage
- Kotlin Coroutines - Asynchronous operations

## Payout Status Types

- **Completed** - Payout processed and sent
- **Pending** - Currently being processed
- **Payable** - Ready to be processed
- **Failed** - Processing failed

## API Information

Uses Gumroad API v2:
- Base URL: `https://api.gumroad.com/`
- Endpoint: `GET /v2/payouts`
- Required Scope: `view_payouts`

## Security

Access tokens are securely stored using DataStore and persisted across app sessions. Tokens can be cleared from the settings page.

## Requirements

- Android 13 (API 33) or higher
- Kotlin 2.0+
- Internet connection

## Developer

Developed by Sameera Wijerathna

Website: [sameerasw.com](https://www.sameerasw.com)

## License

Open source and available for personal use.
