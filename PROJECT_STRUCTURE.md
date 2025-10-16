# GumroadStats - Project Structure Documentation

## 📁 Refactored Architecture

This project has been refactored into a clean, modular architecture for better maintainability and readability.

---

## 🏗️ Project Structure

```
com.sameerasw.gumroadstats/
├── MainActivity.kt                 # Main entry point - displays payouts
├── SettingsActivity.kt            # Separate activity for settings (with back navigation)
│
├── data/                          # Data layer
│   ├── api/                       # API services
│   │   ├── GumroadApiService.kt
│   │   └── RetrofitClient.kt
│   ├── local/                     # Local caching
│   │   └── PayoutsCache.kt
│   ├── model/                     # Data models
│   │   └── Payout.kt
│   ├── preferences/               # User preferences
│   │   ├── PreferencesManager.kt
│   │   └── UpdateInterval.kt
│   └── repository/                # Repository pattern
│       └── GumroadRepository.kt
│
├── ui/                            # UI layer
│   ├── components/                # Reusable UI components
│   │   ├── cards/                 # Card components
│   │   │   ├── PayablePayoutCard.kt      # Featured payable payout card
│   │   │   ├── CompactPayoutCard.kt      # Compact history card
│   │   │   └── PayoutsList.kt            # Main list container
│   │   ├── common/                # Common/shared components
│   │   │   ├── StatusChip.kt             # Text-based status chip
│   │   │   ├── StatusIconChip.kt         # Icon-based status chip
│   │   │   └── DetailRow.kt              # Label-value pair row
│   │   └── sheets/                # Bottom sheets
│   │       └── PayoutDetailsSheet.kt     # Payout details modal
│   ├── screens/                   # Screen-level composables
│   │   ├── PayoutsScreen.kt              # Main payouts screen
│   │   └── SettingsScreen.kt             # Settings screen
│   └── theme/                     # App theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── viewmodel/                     # ViewModels
│   └── PayoutsViewModel.kt
│
└── utils/                         # Utility functions
    └── Formatters.kt              # Date and number formatting
```

---

## 🎯 Key Components Breakdown

### **Activities**
- **MainActivity** - Hosts the main payouts screen, launches SettingsActivity
- **SettingsActivity** - Separate activity with proper back button navigation

### **UI Components - Cards**
- **PayablePayoutCard** - Highlighted card for available payouts with primary container styling
- **CompactPayoutCard** - Compact card for payout history with smart corner rounding
- **PayoutsList** - Container that manages payable vs history sections

### **UI Components - Common**
- **StatusChip** - Text-based chip showing payout status (Completed, Pending, Payable, Failed)
- **StatusIconChip** - Icon-based status indicator with Material icons
- **DetailRow** - Reusable label-value pair component

### **UI Components - Sheets**
- **PayoutDetailsSheet** - Bottom sheet modal displaying full payout information

### **Utilities**
- **Formatters.kt** - Contains `formatAmount()` and `formatDate()` utility functions

---

## 🔄 Navigation Flow

```
MainActivity (Payouts)
    ↓
    [Settings Button Click]
    ↓
SettingsActivity
    ↓
    [Back Button/Back Gesture]
    ↓
MainActivity (Restored)
```

The Settings screen is now a separate activity registered in `AndroidManifest.xml` with proper parent-child relationship, enabling:
- System back button support
- Back gesture navigation
- Up navigation from action bar
- Proper activity lifecycle management

---

## ✨ Features Included

### **Haptic Feedback (Comprehensive)**
- All button clicks (Settings, Save Token, Retry, etc.)
- Payout card taps
- Pull-to-refresh gesture with continuous subtle haptics
- Settings navigation items
- Dialog confirmations

### **Modular Design Benefits**
1. **Easy Testing** - Each component can be tested independently
2. **Reusability** - Components can be used in different screens
3. **Maintainability** - Changes to one component don't affect others
4. **Readability** - Clear separation of concerns
5. **Scalability** - Easy to add new features without cluttering existing files

---

## 🛠️ How to Use Components

### Example: Using PayoutsList
```kotlin
PayoutsList(
    payouts = listOfPayouts,
    onPayoutClick = { payout ->
        // Handle payout click
    }
)
```

### Example: Using StatusChip
```kotlin
StatusChip(status = "completed")
```

### Example: Using Formatters
```kotlin
import com.sameerasw.gumroadstats.utils.formatAmount
import com.sameerasw.gumroadstats.utils.formatDate

val formattedAmount = formatAmount("1234.56") // Returns "1,234.56"
val formattedDate = formatDate("2023-10-16T12:00:00Z") // Returns "Oct 16, 2023 12:00"
```

---

## 📝 File Responsibilities

### **Screen Files** (PayoutsScreen.kt, SettingsScreen.kt)
- Screen-level state management
- Navigation logic
- Scaffold/AppBar setup
- Composing smaller components together

### **Component Files** (Cards, Common, Sheets)
- Single responsibility components
- Reusable across the app
- Self-contained UI logic
- Well-documented with KDoc comments

### **Utility Files** (Formatters.kt)
- Pure functions
- No UI dependencies
- Easy to test
- Shared across components

---

## 🎨 Design Patterns Used

1. **Repository Pattern** - Data layer abstraction
2. **MVVM** - ViewModel for business logic
3. **Component Pattern** - Small, reusable UI components
4. **Separation of Concerns** - Clear boundaries between layers
5. **Single Responsibility** - Each file has one clear purpose

---

## 🚀 Future Enhancements

With this structure, you can easily:
- Add new card types in `ui/components/cards/`
- Create new common components in `ui/components/common/`
- Add new screens in `ui/screens/`
- Extend utilities in `utils/`
- Add animations in a new `ui/animations/` directory
- Implement dependency injection
- Add unit tests for each component

---

## 📋 Notes

- All components include proper KDoc documentation
- Haptic feedback is integrated throughout
- Edge-to-edge display is properly handled
- Material 3 design principles followed
- Proper error handling in formatters

---

**Last Updated:** Refactored on latest build
**Maintainer:** Sameera Sandakelum

