# Gumroad Stats App

An Android app to view your Gumroad payouts data using the Gumroad API.

## Features

- 📊 View all your Gumroad payouts
- 💰 Display payout amounts, currency, and status
- 🏦 Show payment processor details (Stripe/PayPal)
- 📅 Display creation and processing dates
- 🔐 Secure access token input

## Setup

1. **Get your Gumroad Access Token:**
   - Go to your [Gumroad Applications page](https://app.gumroad.com/applications)
   - Create a new OAuth application (if you haven't already)
   - Click "Generate access token"
   - Make sure to request the `view_payouts` scope
   - Copy the access token

2. **Run the app:**
   - Open the project in Android Studio
   - Build and run the app on your device or emulator
   - Enter your access token when prompted
   - Click "Load Payouts" to fetch your data

## Architecture

The app follows MVVM architecture with the following components:

### Data Layer
- **Model**: `Payout.kt` - Data classes for API responses
- **API Service**: `GumroadApiService.kt` - Retrofit interface for API calls
- **Repository**: `GumroadRepository.kt` - Handles data operations

### UI Layer
- **ViewModel**: `PayoutsViewModel.kt` - Manages UI state and business logic
- **Screen**: `PayoutsScreen.kt` - Composable UI components

### Libraries Used
- **Retrofit** - REST API client
- **Gson** - JSON serialization/deserialization
- **OkHttp** - HTTP client with logging
- **Jetpack Compose** - Modern UI toolkit
- **Kotlin Coroutines** - Asynchronous programming

## Payout Status Types

- **Completed** - Payout has been processed and sent
- **Pending** - Payout is being processed
- **Payable** - Upcoming payout ready to be processed
- **Failed** - Payout processing failed

## API Information

The app uses the Gumroad API v2:
- Base URL: `https://api.gumroad.com/`
- Endpoint: `GET /v2/payouts`
- Required Scope: `view_payouts`

## Security Note

Your access token is stored in memory only and is not persisted. You'll need to enter it each time you launch the app.

## Requirements

- Android SDK 33 or higher
- Kotlin 2.0.21
- Internet connection

## License

This project is open source and available for personal use.

