# GumroadStats

An Android app to track and monitor your Gumroad payouts directly from your device.

![Frame 6 Large](https://github.com/user-attachments/assets/e1efe5ee-c7be-425c-a08d-70b31b307d88)


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

