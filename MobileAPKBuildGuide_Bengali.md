# মোবাইল দিয়ে APK বিল্ড করার সম্পূর্ণ গাইড 📱

## 🎯 **আপনার Video Downloader অ্যাপ এখন সম্পূর্ণ!**

### ✅ **অ্যাপের ফিচারগুলো:**
- YouTube, TikTok, Instagram, Facebook ভিডিও ডাউনলোড
- সুন্দর Material Design UI
- মাল্টিপল কোয়ালিটি অপশন (1080p, 720p, 480p)
- প্রগ্রেস ট্র্যাকিং
- অটো URL পেস্ট
- ফাইল শেয়ারিং

---

## 📱 **মোবাইল দিয়ে APK বানানোর পদ্ধতি**

### **পদ্ধতি ১: AIDE (Android IDE) ব্যবহার করুন**

#### **স্টেপ ১: AIDE ইনস্টল করুন**
```
1. Play Store থেকে "AIDE - Android Java IDE" ডাউনলোড করুন
2. অ্যাপ ওপেন করে premium version কিনুন (বা cracked version ব্যবহার করুন)
```

#### **স্টেপ ২: প্রজেক্ট ইমপোর্ট করুন**
```
1. VideoDownloader.zip ফাইল ডাউনলোড করুন
2. ফাইল ম্যানেজার দিয়ে zip extract করুন
3. AIDE ওপেন করে "Open Project" সিলেক্ট করুন
4. video ফোল্ডার সিলেক্ট করুন
```

#### **স্টেপ ৩: APK বিল্ড করুন**
```
1. AIDE এ প্রজেক্ট লোড হওয়ার পর
2. Menu → Run → Build APK সিলেক্ট করুন
3. কয়েক মিনিট অপেক্ষা করুন
4. Build সফল হলে APK ফাইল পাবেন
```

---

### **পদ্ধতি ২: CodeAssist IDE ব্যবহার করুন**

#### **স্টেপ ১: CodeAssist ইনস্টল**
```
1. "CodeAssist (Java/Kotlin IDE)" Play Store থেকে ইনস্টল করুন
2. প্রয়োজনীয় plugins ইনস্টল করুন
```

#### **স্টেপ ২: প্রজেক্ট সেটআপ**
```
1. VideoDownloader.zip extract করুন
2. CodeAssist এ "Import Project" সিলেক্ট করুন
3. video ফোল্ডার পাথ দিন
4. Gradle sync হওয়ার জন্য অপেক্ষা করুন
```

#### **স্টেপ ৩: Build & Run**
```
1. Build → Make Project সিলেক্ট করুন
2. কোনো error থাকলে fix করুন
3. Build → Generate Signed APK সিলেক্ট করুন
4. APK তৈরি হয়ে যাবে
```

---

### **পদ্ধতি ৩: Termux ব্যবহার করুন (Advanced)**

#### **স্টেপ ১: Termux সেটআপ**
```bash
# Termux ইনস্টল করুন Play Store থেকে
pkg update && pkg upgrade
pkg install git openjdk-17 gradle
```

#### **স্টেপ ২: Android SDK ইনস্টল**
```bash
# Android SDK ডাউনলোড
wget https://dl.google.com/android/repository/commandlinetools-linux-latest.zip
unzip commandlinetools-linux-latest.zip
mkdir $HOME/android-sdk
mv cmdline-tools $HOME/android-sdk/
```

#### **স্টেপ ৩: প্রজেক্ট বিল্ড**
```bash
# VideoDownloader প্রজেক্ট ক্লোন করুন
cd $HOME
unzip VideoDownloader.zip
cd video

# Gradle wrapper permission দিন
chmod +x gradlew

# APK বিল্ড করুন
./gradlew assembleDebug
```

---

## 🔧 **সম্ভাব্য সমস্যা ও সমাধান**

### **সমস্যা ১: Gradle Error**
```
সমাধান: 
- Gradle version 6.7.1 ব্যবহার করুন
- Android Gradle Plugin 4.2.2 ব্যবহার করুন
- Java 8 বা Java 11 ব্যবহার করুন
```

### **সমস্যা ২: Dependencies Error**
```
সমাধান:
- build.gradle এ সব dependencies properly add করা আছে
- Internet connection check করুন
- Gradle sync করুন
```

### **সমস্যা ৩: Signing Error**
```
সমাধান:
- Debug APK বানান প্রথমে
- Release APK এর জন্য keystore তৈরি করুন
```

---

## 📦 **APK পাওয়ার পর করণীয়**

### **ইনস্টল করার নিয়ম:**
```
1. Settings → Security → Unknown Sources enable করুন
2. APK ফাইলে tap করুন
3. Install button click করুন
4. Permissions grant করুন
```

### **প্রথম ব্যবহার:**
```
1. অ্যাপ ওপেন করুন
2. Storage permission allow করুন
3. যেকোনো video URL paste করুন
4. Quality select করে download করুন
```

---

## 🎨 **অ্যাপের ব্যবহার**

### **Supported URLs:**
- ✅ YouTube: `https://youtube.com/watch?v=...`
- ✅ TikTok: `https://tiktok.com/@user/video/...`
- ✅ Instagram: `https://instagram.com/p/...`
- ✅ Facebook: `https://facebook.com/watch?v=...`

### **Features:**
- 🎥 Multi-platform video download
- 📱 Beautiful UI with Material Design
- 📊 Real-time progress tracking
- 📂 Organized file management
- 🔄 Auto URL detection from clipboard
- 📤 Share downloaded videos

---

## 💡 **প্রো টিপস**

### **দ্রুত APK বানানোর জন্য:**
```
1. AIDE ব্যবহার করুন (সবচেয়ে সহজ)
2. ভালো internet connection রাখুন
3. ফোনে পর্যাপ্ত storage রাখুন
4. Ram 4GB+ এর ফোন ব্যবহার করুন
```

### **প্রোডাকশন রেডি করতে:**
```
1. App icon customize করুন
2. App name change করুন
3. Signed APK বানান
4. ProGuard enable করুন
```

---

## 📍 **ফাইল লোকেশন**

### **APK পাবেন:**
```
AIDE: /sdcard/aide/video/app/build/outputs/apk/debug/
CodeAssist: /sdcard/CodeAssistProjects/video/app/build/outputs/apk/
Termux: $HOME/video/app/build/outputs/apk/debug/
```

### **ডাউনলোড হওয়া ভিডিও:**
```
/sdcard/Download/VideoDownloader/
├── youtube/
├── tiktok/
├── instagram/
└── facebook/
```

---

## 🚀 **সফল হলে যা পাবেন**

✅ **সম্পূর্ণ কার্যকরী Video Downloader অ্যাপ**  
✅ **Multi-platform support (4টি প্ল্যাটফর্ম)**  
✅ **Professional UI/UX**  
✅ **File management system**  
✅ **Error handling**  
✅ **Progress tracking**  

---

## 📞 **সাহায্য প্রয়োজন হলে**

### **Common Issues:**
1. **Gradle Build Failed** → Java version change করুন
2. **APK Not Installing** → Unknown sources enable করুন  
3. **Download Not Working** → Internet connection check করুন
4. **Permission Error** → Storage permission grant করুন

### **Alternative Options:**
- অনলাইন APK builder ব্যবহার করুন
- ফ্রেন্ডের PC/Laptop দিয়ে build করুন
- Cloud IDE ব্যবহার করুন (GitHub Codespaces)

---

**🎉 আপনার Video Downloader অ্যাপ সম্পূর্ণ তৈরি! এখন মোবাইল দিয়ে APK বানিয়ে ব্যবহার করুন! 🎉**