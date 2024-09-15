# 🍽️ Meal Maestro - Your Personal Recipe and Meal Planner App

Welcome to **Meal Maestro** – the ultimate app for meal planning, recipe discovery, and AI-driven meal suggestions! Whether you’re a casual cook or an expert chef, Meal Maestro offers personalized recipes based on your preferences, integrates social features to share your culinary skills, and helps you organize your meals efficiently.

---

## 🌟 Features

- 🔥 **AI-Powered Recipe Suggestions**: Enter ingredients or a meal idea, and Meal Maestro will suggest personalized recipes.
- 📸 **Recipe Sharing**: Post and share your favorite recipes with friends and the community.
- 💬 **Comments & Likes**: Interact with posts, leave comments, and give likes to your favorite recipes.
- 🕒 **Meal Planning**: Organize and plan your meals for the week.
- ❤️ **Save Recipes**: Bookmark your favorite recipes and save them for later.
- 🍲 **Community Driven**: Follow your friends and fellow foodies, explore their recipes, and get inspired.

---

## 🚀 Getting Started

### Prerequisites
Make sure you have the following tools installed:

- **Android Studio** (latest version)
- **Firebase account** with Firestore and Realtime Database enabled
- A machine with **Kotlin SDK** installed
- A compatible Android device/emulator running Android version 7.0 (Nougat) or higher

### Firebase Setup

1. **Create a Firebase Project**:
   - Go to the [Firebase Console](https://console.firebase.google.com/).
   - Create a new project named **Meal Maestro**.

2. **Enable Firestore and Realtime Database**:
   - In the Firebase project, enable **Firestore** and **Realtime Database** for storing user posts, comments, and likes.

3. **Download `google-services.json`**:
   - After configuring Firebase, download the `google-services.json` file and place it in the `app/` directory of your Android Studio project.

4. **Firebase Authentication**:
   - Set up Firebase Authentication to enable user logins. Use **email and password** authentication for simplicity.

---

## ⚙️ Installation

### Clone the Repository
Clone the repository to your local machine:

```bash
git clone https://github.com/yourusername/meal-maestro.git
cd meal-maestro

Open in Android Studio
Open Android Studio.
Select Open an existing project.
Navigate to the cloned directory and open the project.
Build and Run the Project
Sync your project with Gradle files by clicking on File > Sync Project with Gradle Files.
Once synced, connect an Android device or use the emulator, then click on the Run button to build and launch the app.
📦 Dependencies
Glide: For efficient image loading.
Firebase SDK: Firestore, Realtime Database, Firebase Auth.
Material Design Components: For a sleek and modern UI.
💡 How to Use
Sign Up / Log In
Use Firebase Authentication to sign up or log in.

Explore Recipes
Scroll through the feed to discover recipes shared by the community.

Ask AI for Recipes
Click on the Ask AI button and input ingredients or a meal idea. The AI will suggest recipes based on your input.

Post a Recipe
Share your own recipes with the community. Add a picture, list ingredients, and write down the instructions.

Save and Comment
Like, save, and comment on recipes that inspire you.

Meal Planning
Plan your meals by selecting recipes for the upcoming days. Keep track of your diet and schedule meals.

💻 Technical Details
Firebase
Firestore: Used to store and retrieve user posts, comments, and interactions such as likes.
Realtime Database: Used for real-time updates to the friends list and public user data.
Backend
Firebase Functions: Handle additional backend logic for notifications, meal recommendations, and ensuring data consistency across Firestore and Realtime Database.
AI Integration
OpenAI's GPT-4: Integrated to provide meal suggestions based on user input. The app sends a request to the OpenAI API with a meal description, and the AI responds with a personalized recipe suggestion.
🛠️ Customization
You can customize the following features in the app:

Theme Colors: Change the primary and accent colors in res/values/colors.xml to match your branding.
Firebase Rules: Set your Firestore and Realtime Database rules to ensure data security and privacy.
AI Model: Customize the AI model's temperature or tokens based on your preference for more or less creative meal suggestions.
🔧 Troubleshooting
Firebase Authentication Issues
Double-check your google-services.json file and ensure Firebase Authentication is correctly configured in the console.

App Crashes on Startup
Ensure your Gradle dependencies are synced and the correct SDK versions are installed.

API Errors
If the AI isn’t responding correctly, verify your OpenAI API key is correctly fetched from Firebase Remote Config.

📄 License
This project is licensed under the MIT License. See the LICENSE file for more details.

Thank you for using Meal Maestro! If you have any issues or suggestions, feel free to open an issue or contact us via email at MealMaestrohelp@gmail.com.
