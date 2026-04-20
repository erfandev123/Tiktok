# Video Downloader App

A modern Android application for downloading videos from popular social media platforms including YouTube, TikTok, Instagram, and Facebook.

## Features

### 🎥 Multi-Platform Support
- **YouTube** - Download videos from YouTube links
- **TikTok** - Download TikTok videos and content
- **Instagram** - Download Instagram videos, reels, and IGTV
- **Facebook** - Download Facebook videos

### 📱 Modern UI/UX
- Clean, material design interface
- Intuitive user experience
- Real-time download progress tracking
- Auto-detect URLs from clipboard
- Quality selection options

### 🔧 Advanced Features
- Multiple video quality options (1080p, 720p, 480p, Audio-only)
- Organized file management by platform
- Progress tracking with percentage display
- Comprehensive error handling
- Automatic permission management
- File sharing capabilities

### 🛡️ Security & Permissions
- Storage permission for saving videos
- Internet access for downloading
- Network state monitoring
- FileProvider integration for secure file sharing

## Project Structure

```
video/
├── app/
│   ├── src/main/
│   │   ├── java/com/video/download/
│   │   │   ├── MainActivity.java          # Main activity with UI logic
│   │   │   ├── VideoDownloader.java       # Core download functionality
│   │   │   ├── FileManager.java           # File operations and management
│   │   │   └── ErrorHandler.java          # Error handling and validation
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml      # Modern UI layout
│   │   │   ├── values/
│   │   │   │   └── strings.xml            # App strings
│   │   │   └── xml/
│   │   │       └── provider_paths.xml     # FileProvider configuration
│   │   └── AndroidManifest.xml            # App configuration and permissions
│   └── build.gradle                       # Dependencies and build config
└── README.md                              # This file
```

## Installation & Setup

### Prerequisites
- Android Studio 4.0 or later
- Android SDK API level 23 or higher
- Gradle 7.0 or later

### Build Instructions

1. **Extract the project**
   ```bash
   cd /workspace
   unzip video.zip
   cd video
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the `video` folder
   - Wait for Gradle sync to complete

3. **Build the app**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on device**
   ```bash
   ./gradlew installDebug
   ```

## Usage

### Basic Usage
1. **Launch the app** - Open "Video Downloader" from your app drawer
2. **Paste URL** - Copy a video URL from supported platforms and paste it in the input field
3. **Select Quality** - Choose your preferred video quality from the dropdown
4. **Download** - Tap the "Download Video" button
5. **Monitor Progress** - Watch the progress bar for download status
6. **Access Files** - Downloaded videos are saved to `Downloads/VideoDownloader/[platform]/`

### Supported URL Formats

#### YouTube
- `https://www.youtube.com/watch?v=VIDEO_ID`
- `https://youtu.be/VIDEO_ID`

#### TikTok
- `https://www.tiktok.com/@username/video/VIDEO_ID`
- `https://vm.tiktok.com/SHORT_CODE`

#### Instagram
- `https://www.instagram.com/p/POST_ID/`
- `https://www.instagram.com/reel/REEL_ID/`
- `https://www.instagram.com/tv/IGTV_ID/`

#### Facebook
- `https://www.facebook.com/watch?v=VIDEO_ID`
- `https://fb.watch/SHORT_CODE`

### File Organization
Downloaded videos are automatically organized by platform:
```
Downloads/VideoDownloader/
├── youtube/
├── tiktok/
├── instagram/
└── facebook/
```

## Technical Details

### Architecture
- **MVP Pattern** - Clean separation of concerns
- **Modular Design** - Each component has specific responsibilities
- **Error Handling** - Comprehensive error management with user-friendly messages
- **File Management** - Organized storage with metadata tracking

### Dependencies
- `androidx.appcompat` - Modern Android UI components
- `com.google.android.material` - Material Design components
- `com.squareup.okhttp3` - HTTP client for network requests
- `androidx.work` - Background task management
- `androidx.core` - AndroidX core utilities

### Permissions Required
- `INTERNET` - Download videos from web
- `WRITE_EXTERNAL_STORAGE` - Save videos to device storage
- `READ_EXTERNAL_STORAGE` - Access saved videos
- `ACCESS_NETWORK_STATE` - Monitor network connectivity

## Development Notes

### Current Implementation
This is a **demonstration version** with the following characteristics:
- Platform detection and URL parsing are fully implemented
- UI and user experience are production-ready
- File management system is complete
- Error handling is comprehensive

### Production Considerations
For production deployment, consider:
1. **API Integration** - Implement proper APIs for each platform
2. **Legal Compliance** - Ensure compliance with platform terms of service
3. **Rate Limiting** - Implement download throttling
4. **Caching** - Add intelligent caching mechanisms
5. **Background Downloads** - Use WorkManager for large downloads

### Platform-Specific Notes

#### YouTube
- Requires YouTube Data API for production use
- Must comply with YouTube Terms of Service
- Consider using youtube-dl or similar libraries

#### TikTok
- API access may require TikTok Developer approval
- Short URLs need expansion before processing
- Consider rate limiting for bulk downloads

#### Instagram
- Instagram Basic Display API for authenticated access
- Private content requires user authorization
- Story downloads have time limitations

#### Facebook
- Facebook Graph API for video access
- Public videos only without user authentication
- Video quality depends on original upload

## Troubleshooting

### Common Issues

1. **Permission Denied**
   - Grant storage permissions in app settings
   - Check that external storage is available

2. **Download Fails**
   - Verify internet connection
   - Check if the video URL is valid and accessible
   - Ensure sufficient storage space

3. **App Crashes**
   - Check device logs for detailed error information
   - Verify all dependencies are properly installed

4. **Video Not Found**
   - Confirm the video is publicly accessible
   - Check if the video has been deleted or made private

### Debug Mode
Enable debug logging by checking device logs:
```bash
adb logcat | grep VideoDownloader
```

## Contributing

### Code Style
- Follow Android Kotlin style guide
- Use meaningful variable and method names
- Add comments for complex logic
- Implement proper error handling

### Testing
- Test on multiple Android versions (API 23+)
- Verify functionality across different platforms
- Test various video formats and qualities

## License

This project is for educational and demonstration purposes. Please ensure compliance with platform terms of service and local laws when using or modifying this application.

## Disclaimer

This application is provided as-is for educational purposes. Users are responsible for complying with the terms of service of the respective platforms (YouTube, TikTok, Instagram, Facebook) and applicable copyright laws. The developers are not responsible for any misuse of this application.

---

**Note**: This is a demonstration version. For production use, implement proper API integrations and ensure legal compliance with all supported platforms.
