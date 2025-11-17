# 📱 Placely - Placement Preparation Assistant

![Placely Logo](app_logo.png)

## 🎯 Overview

**Placely** is a comprehensive Android application designed to help students manage their campus placement preparation efficiently. Built with modern Android development practices, this app addresses the real-world challenge of juggling multiple company applications, coding tests, interviews, and deadlines during the placement season.

## 💡 Problem Statement

During placement season, students face:
- Multiple company tests and interviews on different dates
- Difficulty remembering important deadlines
- Scattered notes and preparation materials
- No centralized system to track applications and tasks
- Missing important notifications leading to missed opportunities

**Placely solves these problems** by providing an all-in-one solution for placement preparation management.

---

## ✨ Key Features

### 🔔 Smart Reminder System
- Create reminders for tests, interviews, resume submissions
- **Dual notification system**: Get notified BEFORE the event (15 min, 1 hour, 1 day) AND at the event time
- Visual indicators for overdue reminders
- Category-based organization (Online Test, Interview, Coding Contest, etc.)

### ✅ Task Management
- Create and manage preparation tasks
- Set priorities (High/Low)
- Optional deadlines
- Mark tasks as complete
- Filter completed/pending tasks
- Progress tracking

### 📝 Notes Feature
- Beautiful grid/list view for notes
- Pin important notes to the top
- Search functionality
- Color-coded note cards
- Rich text support
- Easy editing and deletion

### 📅 Calendar View
- Visualize all upcoming events
- Monthly/weekly view (coming soon)
- Quick overview of your schedule

### 🏠 Smart Dashboard
- "Due Today" section highlighting today's reminders and tasks
- Motivational quotes and tips
- Quick navigation to all features
- Time-based greetings (Good Morning/Afternoon/Evening)
- Real-time statistics

---

## 🛠️ Technical Stack

### **Architecture & Design Pattern**
- **Clean Architecture** - Separation of concerns
- **MVVM Pattern** - Model-View-ViewModel
- **Repository Pattern** - Data abstraction layer

### **Core Technologies**
- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose (Modern declarative UI)
- **Database**: Room Persistence Library
- **Dependency Injection**: Koin
- **Asynchronous Programming**: Kotlin Coroutines & Flow
- **Background Tasks**: WorkManager (for notifications)

### **Android Jetpack Components**
- Navigation Component
- LiveData & StateFlow
- ViewModel
- Lifecycle-aware components

### **UI/UX**
- Material Design 3
- Responsive layouts
- Dark mode support
- Smooth animations
- Intuitive navigation

---

## 📂 Project Structure
```
app/src/main/java/com/example/placely/
├── data/
│   ├── dao/                    # Database Access Objects
│   │   ├── ReminderDao.kt
│   │   ├── TaskDao.kt
│   │   └── NoteDao.kt
│   ├── entity/                 # Room Entities
│   │   ├── ReminderEntity.kt
│   │   ├── TaskEntity.kt
│   │   └── NoteEntity.kt
│   ├── AppDatabase.kt          # Room Database
│   └── PlacelyRepository.kt    # Data Repository
│
├── di/
│   └── AppModule.kt            # Koin Dependency Injection
│
├── navigation/
│   └── Navigation.kt           # Navigation Graph
│
├── ui/
│   ├── screens/                # Composable Screens
│   │   ├── HomeScreen.kt
│   │   ├── ReminderScreen.kt
│   │   ├── AddEditReminderScreen.kt
│   │   ├── TaskScreen.kt
│   │   ├── AddEditTaskScreen.kt
│   │   ├── NoteScreen.kt
│   │   ├── AddEditNoteScreen.kt
│   │   ├── ViewNoteScreen.kt
│   │   └── CalendarScreen.kt
│   │
│   └── viewmodel/              # ViewModels
│       ├── HomeViewModel.kt
│       ├── ReminderViewModel.kt
│       ├── TaskViewModel.kt
│       └── NoteViewModel.kt
│
├── util/
│   ├── DateTimeUtil.kt         # Date/Time utilities
│   ├── NotificationHelper.kt   # Notification management
│   └── WorkManagerHelper.kt    # Background task scheduling
│
├── workers/
│   └── NotificationWorker.kt   # WorkManager worker for notifications
│
├── PlacelyApplication.kt       # Application class
└── MainActivity.kt             # Main Activity
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog | 2023.1.1 or later
- Minimum SDK: 26 (Android 8.0)
- Target SDK: 35 (Android 15)
- Kotlin 1.9.0+

### Installation Steps

1. **Clone the repository**
```bash
   git clone https://github.com/thanujaa9/placely.git
   cd placely
```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Wait for Gradle to sync dependencies
   - Resolve any dependency issues if they arise

4. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" or press Shift + F10

### Alternative: Install APK Directly
**Download the APK:**  
[Placely v1.0 APK](https://drive.google.com/file/d/1Nf0qHqdExmRAiO5cQcuNCya95Y3GQNnK/view?usp=sharing)


#### Installation on Android Device:
1. Download the APK from the link above
2. Enable "Install from Unknown Sources" in your device settings
3. Open the downloaded APK file
4. Follow the installation prompts
5. Grant necessary permissions (Notifications, Alarms)

---

📱 App Screenshots

🏠 Home Screen
![Home Screen](https://drive.google.com/uc?export=view&id=1mGzO1qP5ShmoHbaWT-YqXIUPHVZIlDGk)


📝 Notes Screen
![Notes Screen](https://drive.google.com/uc?export=view&id=1LJHaZYCAGy6YTZ_yYmUqRXTXLJnMfBm8)


⏰ Reminders Screen
![Reminders Screen](https://drive.google.com/uc?export=view&id=1CbkkHNSNOHr0XxEEroGeFkF2zTp98rfU)


🗂️ Tasks Screen
![Tasks Screen](https://drive.google.com/uc?export=view&id=1WUvN_LaFPioKXQbfqb9CKDjQx94mU8tX)

🗓️ Calendar Screen
![Calendar Screen](https://drive.google.com/uc?export=view&id=1QVhBOqMS-4V5UKSKQC8RDASeBC3LLerf)


---

## 🔐 Permissions Required

- **POST_NOTIFICATIONS** - For reminder notifications
- **SCHEDULE_EXACT_ALARM** - For precise reminder timing
- **USE_EXACT_ALARM** - For alarm functionality

---

## 🎨 Features in Detail

### 1. Reminder Management
```kotlin
// Create a reminder
Reminder(
    title = "TCS Online Test",
    description = "DSA and Aptitude",
    dateTime = selectedDateTime,
    type = "Online Test",
    notificationAlert = 3600000L // 1 hour before
)
```

**Features:**
- Set date and time with intuitive pickers
- Choose reminder type from predefined categories
- Set notification alerts (15 min, 1 hour, 1 day before)
- Get TWO notifications: pre-alert + deadline notification
- Edit and delete reminders
- Visual indicators for overdue items

### 2. Task Management
```kotlin
// Create a task
Task(
    title = "Complete DSA Module 3",
    deadline = optionalDeadline,
    priority = "High",
    isCompleted = false
)
```

**Features:**
- Create tasks with optional deadlines
- Set priorities (High/Low)
- Mark as complete with checkbox
- Filter view (show/hide completed)
- Track progress
- "Due Today" quick view

### 3. Notes System
```kotlin
// Create a note
Note(
    title = "Interview Questions - Arrays",
    content = "1. Two Sum Problem...",
    isPinned = false
)
```

**Features:**
- Beautiful grid/list layout toggle
- Color-coded cards for visual distinction
- Pin important notes
- Search across titles and content
- Markdown support (future)
- Export/share notes

---

## 🏗️ Architecture Highlights

### Clean Architecture Layers
```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│    (UI - Jetpack Compose)          │
│    (ViewModel - Business Logic)     │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│         Domain Layer                │
│    (Use Cases - Future)             │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│          Data Layer                 │
│    (Repository - Data Management)   │
│    (Room Database - Local Storage)  │
└─────────────────────────────────────┘
```

### Key Design Decisions

1. **Single Source of Truth**: Room Database
2. **Reactive Data**: Flow and StateFlow for reactive UI
3. **Offline-First**: All data stored locally
4. **Background Processing**: WorkManager for reliable notifications
5. **Dependency Injection**: Koin for loose coupling
6. **Modern UI**: Jetpack Compose for declarative UI

---

## 📊 Database Schema

### Reminders Table
```sql
CREATE TABLE reminders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    description TEXT,
    dateTime INTEGER NOT NULL,
    type TEXT NOT NULL,
    notificationAlert INTEGER NOT NULL
);
```

### Tasks Table
```sql
CREATE TABLE tasks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    deadline INTEGER,
    isCompleted INTEGER NOT NULL DEFAULT 0,
    priority TEXT NOT NULL DEFAULT 'Low'
);
```

### Notes Table
```sql
CREATE TABLE notes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    isPinned INTEGER NOT NULL DEFAULT 0,
    timestamp INTEGER NOT NULL
);
```



## 🔮 Future Enhancements

- [ ] User authentication
- [ ] Collaborative features (share reminders with friends)
- [ ] Analytics dashboard
- [ ] Export data (CSV, PDF)
- [ ] Widget support
- [ ] Wear OS companion app
- [ ] AI-powered study recommendations
- [ ] Integration with college placement portals
- [ ] Resume builder module




## 📱 Download

**Latest Release: v1.0.0**

📥 **Download the APK:**  
[Placely v1.0 APK](https://drive.google.com/file/d/1Nf0qHqdExmRAiO5cQcuNCya95Y3GQNnK/view?usp=sharing)

**Installation Instructions:**
1. Download the APK from the link above
2. Enable "Install from Unknown Sources" in Settings → Security
3. Open the APK file and follow installation prompts
4. Grant notification permissions when prompted
5. Enjoy managing your placement preparation!

**System Requirements:**
- Android 8.0 (API 26) or higher
- ~15 MB storage space
- Internet not required (offline-first app)

---

<div align="center">

**Built with ❤️ for students, by a student**

**Made during placement season to make placement season easier for everyone**

</div>
