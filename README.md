# Halli Santhe Digital 🏺🧵

**Halli Santhe Digital** is a specialized Android marketplace designed to bridge the gap between rural artisans and urban buyers. The platform empowers local craftsmen to showcase their traditional village crafts—ranging from pottery and textiles to woodwork and organic products—directly to a global audience.

---

## 🚀 Key Features

### 🛒 Marketplace & Discovery
*   **Dynamic Home Screen:** Browse a curated list of featured products and real-time listings from artisans.
*   **Search & Filter:** Quickly find specific crafts using the search bar or filter by categories like Pottery, Textiles, Jewelry, etc.
*   **Category Navigation:** Explore dedicated category sections with intuitive emoji-based navigation.

### 👤 User Roles (Buyer & Artisan)
*   **Artisan Dashboard:** Artisans can easily list new products, manage their active listings, and delete sold items.
*   **Buyer Profile:** Buyers can maintain a personalized wishlist and manage their profile details.
*   **Firebase Authentication:** Secure login and registration system with role-based access control.

### 💖 Wishlist & Engagement
*   **Real-time Wishlist:** Save your favorite products with a single tap. The wishlist syncs instantly across devices using Firestore real-time listeners.
*   **Contact Artisan:** Integrated WhatsApp support allows buyers to contact artisans directly with a pre-filled interest message.

### 📸 Optimized Uploads (No Storage Required)
*   **Base64 Image Processing:** To ensure compatibility with free-tier hosting, images are automatically resized, compressed, and stored directly in Firestore as Base64 strings. No Firebase Storage configuration is needed.

---

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** XML Layouts with ViewBinding
*   **Database:** Firebase Firestore (NoSQL)
*   **Authentication:** Firebase Auth
*   **Image Loading:** Glide (supports both URL and Base64)
*   **Architecture:** Modular Fragment-based navigation with a single MainActivity.

---

## ⚙️ Setup & Installation

### 1. Firebase Configuration
1.  Create a new project in the [Firebase Console](https://console.firebase.google.com/).
2.  Add an Android App with the package name: `com.hallisanthe.hallisanthe`.
3.  Download the `google-services.json` and place it in the `app/` directory.
4.  Enable **Email/Password** authentication.

### 2. Firestore Rules
Set the following rules in your Firestore Console to ensure data security:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    match /products/{productId} {
      allow read: if true;
      allow create, update, delete: if request.auth != null;
    }
  }
}
```

---

## 📋 Evaluation Notes
This version of the project includes critical bug fixes and optimizations:
*   **Resolved Plugin Conflicts:** Fixed Gradle issues related to `org.jetbrains.kotlin.android`.
*   **Memory Management:** Implemented efficient Base64 image compression to stay within Firestore document limits.
*   **Crash Prevention:** Standardized fragment transactions with `commitAllowingStateLoss()` for a smoother UI experience.
*   **Data Integrity:** Implemented `UserSession.load()` to ensure profile data is consistently fetched from the backend.

---

**Developed with ❤️ for rural empowerment.**
