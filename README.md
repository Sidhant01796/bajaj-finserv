# Quiz Leaderboard System
## Bajaj Finserv Internship Assignment

---

## 🚀 How to Run (Windows)

### Step 1 — Set your Registration Number
Open `QuizLeaderboard.java` in any text editor (Notepad works).
Find line 16 and replace `YOUR_REG_NO_HERE` with your actual reg number:
```java
private static final String REG_NO = "2024CS101"; // ← your actual reg no
```

### Step 2 — Run
Simply **double-click `run.bat`**

> ⏱ The program takes ~50 seconds to complete (10 polls × 5 second delay each)

---

## 📋 Requirements
- **Java JDK 11 or higher** — Download from https://www.oracle.com/java/technologies/downloads/
- No other dependencies needed — zero external libraries

---

## 🧠 How It Works

### The Core Problem
The API may return the **same event data across multiple polls** (duplicate entries).
If you naively sum all scores, you get an inflated (wrong) total.

### The Solution: Deduplication via Composite Key
Each event is uniquely identified by `roundId + participant`.
We track every processed event in a `HashSet`. If we see the same key again → skip it.

```
Poll 0 → R1|Alice (score: 10) → NEW     → scores["Alice"] = 10
Poll 3 → R1|Alice (score: 10) → DUPLICATE → skipped ✓
Final Alice score = 10 ✅  (not 20 ❌)
```

### Full Flow
```
Poll API 10 times (poll=0 to poll=9), 5s apart
            ↓
    For each event in response:
      key = roundId + "|" + participant
      if key already seen → skip (duplicate)
      else → add score to participant total
            ↓
    Sort participants by totalScore (descending)
            ↓
    POST leaderboard to /quiz/submit (once)
```

---

## 📁 File Structure
```
quiz-leaderboard/
├── QuizLeaderboard.java   ← Main source code (edit REG_NO here)
├── run.bat                ← Double-click to compile & run on Windows
└── README.md              ← This file
```

---

## 🔧 Manual Compile & Run (if run.bat doesn't work)
```cmd
javac QuizLeaderboard.java
java QuizLeaderboard
```

---

## 📤 Submission
Push this folder to a **public GitHub repo** and submit the link in the form.
