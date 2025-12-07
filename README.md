# 🔍 ServicesFinder

- **📚 Course:** CS 175 – Android Mobile Development
- **👨‍🏫 Professor:** Yan Chen
- **📅 Semester:** Fall 2025

---
### 👥 Team Number: 09
## Team Members and Contributions
| Name | Contribution |
|------|--------------|
| **Van Anh Tran** | Category filtering engine, Firestore data services, provider side authentication system, localization for 4 languages: English (US), Chinese, Vietnamese, Spanish (US), preparing presentation |
| **Ben Nguyen** | Provider UI layouts, category chips, customer reviews, review integration |
| **Nhat Anh Nguyen** | Filtering and search, customer profile, customer authentication UI, localization UI, favorites feature, review integration, image compression, smooth animations, error handling with retry dialogs, performance optimization |
| **Rohan Mehta** | UI design, final review, presenting |

---

### 📱 APK File:
https://drive.google.com/file/d/1QdVSm8_mozZUjqOe-86iy6RrGaG_C4R-/view?usp=sharing

### 📋 Backlog:
https://docs.google.com/spreadsheets/d/1pm37TSBmQL9C6HI7-OMaLhnKDPHRMzfbEjpT8pUAz8k/edit?gid=0#gid=0

---

## 📖 Project Description
**ServicesFinder** is a mobile app connecting local service providers with customers seeking services like Hair Care, Nail Services, Automotive Repair, Education, Pet Care, and more.

### 🛒 Customer Features
- **Authentication:** Sign in/up with email or phone number, seamless role switching to provider portal
- **Browse & Search:** Find services by category or keyword with real-time search
- **Advanced Sorting:** Sort by Most Recent, Price (Low/High), Highest Rating, or Popularity with actual review data
- **Language Support:** Switch between English, Spanish, Vietnamese, and Chinese with full UI localization
- **Favorites:** Save preferred providers with heart button, view all favorites in profile
- **Reviews & Ratings:** Read detailed reviews, write reviews with star ratings (requires login)
- **Profile Management:** View favorites, manage account settings, and logout
- **Contact Providers:** Call, email, or navigate to provider location directly from service details

### 🏢 Provider Features
- **Flexible Registration:** Sign up with phone or email, seamless role switching to customer portal
- **Service Management:** Add, edit, and delete multiple services with rich descriptions
- **Profile Management:** Edit personal information, change password securely
- **Image Upload:** Add service photos with automatic compression (1920x1080 @ 85% quality)
- **Dashboard:** View all services, manage listings, access account settings
- **Account Control:** Secure password change and account deletion options  

---

## 🔄 Dual-Role Architecture

ServicesFinder supports two distinct user roles with separate data management:

- **🏢 Providers:** Create and manage service listings, manage profile
- **🛒 Customers:** Browse services, write reviews, save favorites, manage preferences

**Key Design:**
- Separate Firestore collections (`providers/` and `customers/`)
- Independent authentication and session management
- Role-based UI and permissions
- Users can have both roles using different accounts

---

## ⚡ Key Technical Highlights

### Performance & UX Enhancements
- **Image Compression:** Automatic upload optimization to 1920x1080 @ 85% quality
- **Smooth Animations:** Fade transitions for loading and content states
- **Error Handling:** Network connectivity checks with user-friendly retry dialogs
- **Loading States:** Progress indicators throughout the app for better user feedback

### Advanced Features
- **Real Rating Sort:** Dynamic sorting by actual review ratings with asynchronous data fetching
- **Review Integration:** Customer names linked to reviews for transparency and accountability
- **Favorites System:** Persistent favorite providers with heart icon toggle
- **Role Switcher:** One-click navigation between customer and provider authentication portals
- **Multilingual Support:** Complete localization for 4 languages including role switcher UI

### Code Quality
- **MVC Architecture:** Clean separation of concerns with Model-View-Controller pattern
- **Utility Classes:** Reusable components (AnimationHelper, NetworkHelper, RetryDialog, ProToast)
- **Firebase Integration:** Secure authentication, real-time Firestore sync, and cloud storage
- **Material Design 3:** Modern UI components following Android design guidelines

---

## 🛠️ Technical Requirements

- Android Studio
- Java
- Firebase Authentication
- Firebase Firestore
- Firebase Storage
- Glide (Image loading library)
- MinSDK: 24+
- TargetSDK: 34

---

## Project Structure

```bash
app/
├── java/
│   └── edu/
│       └── sjsu/
│           └── android/
│               └── servicesfinder/
│                   ├── controller/
│                   │   ├── CatalogueController.java
│                   │   ├── CustomerController.java
│                   │   ├── FirestoreStringTranslator.java
│                   │   ├── FormHelper.java
│                   │   ├── HomeController.java
│                   │   ├── ProviderController.java
│                   │   ├── ProviderServiceController.java
│                   │   ├── ReviewAdapter.java
│                   │   ├── ServiceCardAdapter.java
│                   │   ├── SessionManager.java
│                   │   └── UIHelper.java
│                   │
│                   ├── database/
│                   │   ├── CatalogueDatabase.java
│                   │   ├── CustomerDatabase.java
│                   │   ├── FirestoreHelper.java
│                   │   ├── ProviderDatabase.java
│                   │   ├── ProviderServiceDatabase.java
│                   │   ├── ReviewDatabase.java
│                   │   ├── ServiceDatabase.java
│                   │   └── StorageHelper.java
│                   │
│                   ├── model/
│                   │   ├── Catalogue.java
│                   │   ├── Customer.java 
│                   │   ├── Provider.java
│                   │   ├── ProviderService.java
│                   │   ├── Review.java
│                   │   └── Service.java
│                   │
│                   ├── util/
│                   │   ├── AnimationHelper.java 
│                   │   ├── NetworkHelper.java 
│                   │   ├── ProToast.java
│                   │   └── RetryDialog.java   
│                   │
│                   └── view/
│                       ├── CustomerAuthActivity.java  
│                       ├── CustomerProfileActivity.java
│                       ├── EditProfileActivity.java
│                       ├── MainActivity.java 
│                       ├── MultiSelectDropdown.java
│                       ├── ProviderDashboardActivity.java
│                       ├── ProviderEntryActivity.java
│                       └── ServiceDetailActivity.java 
│
└── res/
    ├── layout/
    │   ├── activity_customer_auth.xml         
    │   ├── activity_customer_profile.xml   
    │   └── ...
    ├── values/
    ├── drawable/
    ├── mipmap/
    └── xml/
```

## 📖 User Guide

### 🎯 Getting Started
1. Download and install the APK from the link above
2. Launch ServicesFinder

---

### 🛒 For Customers (Service Seekers):

#### **Sign Up/Sign In (Not required)**
1. Tap **"Continue as Customer"** on the main screen
2. Enter your **email** or **phone number**
3. Create a **password** (minimum 6 characters)
4. Tap **"Sign Up"** (new user) or **"Sign In"** (existing user)

#### **Browse Services**
- View all available services on the home screen
- Scroll through the list to see different providers and their services

#### **Search for Services**
1. Use the **search bar** at the top of the home screen
2. Type keywords like "haircut", "car repair", "dog grooming", etc.
3. Results will filter automatically as you type

#### **Filter by Category**
1. Tap on **category chips** below the search bar
2. Choose from: Hair Care, Automotive, Pet Care, Education, Nail Services, etc.
3. Services will filter to show only the selected category

#### **Sort Services**
1. Tap the **sort icon** in the top right corner
2. Choose your preferred sorting:
   - **Most Recent:** Newest services first
   - **Price: Low to High:** Cheapest first
   - **Price: High to Low:** Most expensive first
   - **Highest Rating:** Best-rated services first
   - **Popularity:** Most reviewed services first

#### **View Service Details**
1. Tap on any **service card** to open full details
2. Read the description, pricing, availability, and customer reviews
3. See provider contact information and location

#### **Contact a Provider**
- Tap **"Call"** to phone the provider directly
- Tap **"Email"** to send an email
- Tap **"Get Directions"** to navigate to their location (requires Google Maps)

#### **Write a Review**
1. Open any **service detail page**
2. Scroll down to the reviews section
3. Tap **"Write a Review"** button
4. Select a **star rating** (1-5 stars)
5. Write your **review comments** in the text field
6. Tap **"Submit Review"**

#### **Change Language**
1. Go to your **Profile** (bottom navigation)
2. Tap **"Settings"**
3. Select **"Language"**
4. Choose from: **English**, **Spanish (Español)**, **Vietnamese (Tiếng Việt)**, or **Chinese (中文)**
5. The entire app interface will switch to your chosen language

---

### 🏢 For Providers (Service Sellers):

#### **Sign Up as a Provider**
1. From the main screen, tap **"Switch to Provider"**
2. In the Sign-Up tab, enter:
   - **Full Name** (required)
   - **Phone Number** (required, format: ###-###-####)
   - **Email** (optional)
   - **Business Address** (required)
   - **Password** (minimum 6 characters, required)
   - **Confirm Password** (required)
3. Tap **"Sign Up"**

#### **Sign In as an Existing Provider**
1. Tap **"Switch to Provider"** on the main screen
2. Switch to the **"Sign In"** tab
3. Enter your **email/phone** and **password**
4. Tap **"Sign In"**

#### **Add a New Service**
1. From your **Provider Dashboard**, tap the **"+" (Add)** button
2. Fill in the service details:
   - **Service Title:** Name of your service (e.g., "Professional Haircut")
   - **Description:** Detailed description of what you offer
   - **Pricing:** Your pricing structure (e.g., "$30 per session")
   - **Category:** Select one or multiple categories
   - **Service Area:** Location where you provide service
   - **Availability:** Your working hours/days
   - **Contact Preference:** How customers should reach you
   - **Service Image:** Upload a photo (optional)
3. Tap **"Save Service"**

#### **Edit an Existing Service**
1. From your dashboard, tap on the **service card** you want to edit
2. Tap the **"Edit"** button
3. Make your changes to any field
4. Tap **"Save Changes"**

#### **Delete a Service**
1. From your dashboard, tap on the **service card** you want to delete
2. Tap the **"Delete"** button
3. Confirm deletion in the popup dialog

#### **Edit Your Profile**
1. From your dashboard, tap **"Settings"** (gear icon)
2. Tap **"Edit Profile"**
3. Update any of the following: Full Name, Email, Phone Number, Business Address
4. Tap **"Save Changes"**

#### **Change Your Password**
1. Go to **Settings** from your dashboard
2. Tap **"Change Password"**
3. Enter your **current password**
4. Enter your **new password** (minimum 6 characters)
5. **Confirm new password**
6. Tap **"Update Password"**

#### **Switch to Customer Mode**
1. Logout from provider dashboard
2. At the authentication screen, tap **"Switch to Customer"**
3. Sign in with your customer account (different from provider account)

---



