# ServicesFinder - Version 1.0

### Team Number: 09  

---

## Team Members and Contributions  
### Version 1.0

| Name | Contribution |
|------|--------------|
| **Van Anh Tran** | Implemented category filtering engine, debugging category-key logic, Firestore data modeling for provider services, phone normalization rules |
| **Ben Nguyen** | Designed signup and provider entry UI layouts, category chips UI style, implemented tab layout for sign-in/sign-up |
| **Nhat Anh Nguyen** | Implemented service detail view and navigation transitions, tested filtering and search behaviors |
| **Phuong Tong** | Integrated ProviderDashboardActivity, implemented update profile logic, Firestore save operations, final review & bug fixes |

---

## Project Description  
This is Version 1.0 of the **ServicesFinder** Android app, designed to connect local service providers with users looking for real-world services such as **Hair Care, Nail Services, Automotive Repair, Education Services, Pet Care**, etc.

Users can:  
- browse services  
- search by category or keyword  
- view provider details  
- contact providers  

Providers can:  
- register by phone or email  
- add multiple services  
- edit their profile  
- delete their account  

---

## Technical Requirements

- Android Studio  
- Java  
- Firebase Authentication  
- Firebase Firestore  
- Firebase Storage  
- MinSDK: 24+  

---

## Project Structure
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
│                   │   ├── FirestoreHelper.java
│                   │   ├── ProviderDatabase.java
│                   │   ├── ProviderServiceDatabase.java
│                   │   ├── ReviewDatabase.java
│                   │   ├── ServiceDatabase.java
│                   │   └── StorageHelper.java
│                   │
│                   ├── model/
│                   │   ├── Catalogue.java
│                   │   ├── Provider.java
│                   │   ├── ProviderService.java
│                   │   ├── Review.java
│                   │   └── Service.java
│                   │
│                   ├── util/
│                   │   └── ProToast.java
│                   │
│                   └── view/
│                       ├── EditProfileActivity.java
│                       ├── MainActivity.java
│                       ├── MultiSelectDropdown.java
│                       ├── ProviderDashboardActivity.java
│                       ├── ProviderEntryActivity.java
│                       └── ServiceDetailActivity.java
│
└── res/
    ├── layout/
    ├── values/
    ├── drawable/
    ├── mipmap/
    └── xml/

---

## How to Run

### Using Android Studio:
1. Clone repo  
2. Open project  
3. Add **google-services.json** to `/app`  
4. Build & Run  
5. Create provider account  
6. Firestore collections auto-populate  

---

**Course:** CS 175 – Android Mobile Development  
**Professor:** Yan Chen  
**Semester:** Fall 2025  
