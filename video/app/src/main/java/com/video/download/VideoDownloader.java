package com.video.download;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoDownloader {
    private static final String TAG = "VideoDownloader";
    private Context context;
    private DownloadListener listener;

    public interface DownloadListener {
        void onProgress(int progress);
        void onSuccess(String filePath);
        void onError(String error);
        void onStart();
    }

    public VideoDownloader(Context context) {
        this.context = context;
    }

    public void setDownloadListener(DownloadListener listener) {
        this.listener = listener;
    }

    public void downloadVideo(String url, String quality) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (listener != null) listener.onStart();

                    VideoInfo videoInfo = extractVideoInfo(url, quality);
                    if (videoInfo == null) {
                        if (listener != null) listener.onError("Failed to extract video information");
                        return;
                    }

                    String fileName = generateFileName(videoInfo.title, videoInfo.platform);
                    File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "VideoDownloader");
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }

                    File outputFile = new File(downloadDir, fileName);
                    downloadFile(videoInfo.downloadUrl, outputFile.getAbsolutePath());

                } catch (Exception e) {
                    Log.e(TAG, "Download error", e);
                    if (listener != null) listener.onError("Download failed: " + e.getMessage());
                }
            }
        }).start();
    }

    private VideoInfo extractVideoInfo(String url, String quality) {
        try {
            String platform = detectPlatform(url);
            Log.d(TAG, "Detected platform: " + platform);

            switch (platform) {
                case "youtube":
                    return extractYouTubeInfo(url, quality);
                case "tiktok":
                    return extractTikTokInfo(url, quality);
                case "instagram":
                    return extractInstagramInfo(url, quality);
                case "facebook":
                    return extractFacebookInfo(url, quality);
                default:
                    return null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting video info", e);
            return null;
        }
    }

    private String detectPlatform(String url) {
        url = url.toLowerCase();
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return "youtube";
        } else if (url.contains("tiktok.com") || url.contains("vm.tiktok.com")) {
            return "tiktok";
        } else if (url.contains("instagram.com")) {
            return "instagram";
        } else if (url.contains("facebook.com") || url.contains("fb.watch")) {
            return "facebook";
        }
        return "unknown";
    }

    private VideoInfo extractYouTubeInfo(String url, String quality) {
        try {
            // Extract video ID
            String videoId = extractYouTubeVideoId(url);
            if (videoId == null) return null;

            // For demonstration, we'll use a simplified approach
            // In a real app, you'd need to use YouTube API or other methods
            VideoInfo info = new VideoInfo();
            info.platform = "youtube";
            info.title = "YouTube Video " + videoId;
            info.downloadUrl = "https://www.youtube.com/watch?v=" + videoId; // Placeholder
            
            // Note: Actual YouTube video extraction requires more complex implementation
            // due to YouTube's protection mechanisms
            Log.w(TAG, "YouTube download requires additional implementation");
            return info;

        } catch (Exception e) {
            Log.e(TAG, "YouTube extraction error", e);
            return null;
        }
    }

    private VideoInfo extractTikTokInfo(String url, String quality) {
        try {
            // Expand short URL if needed
            String expandedUrl = expandShortUrl(url);
            
            // Extract video ID from TikTok URL
            Pattern pattern = Pattern.compile("tiktok\\.com.*?/video/(\\d+)");
            Matcher matcher = pattern.matcher(expandedUrl);
            
            String videoId = null;
            if (matcher.find()) {
                videoId = matcher.group(1);
            }

            if (videoId == null) {
                // Try alternative pattern for mobile URLs
                pattern = Pattern.compile("tiktok\\.com.*?/(\\d+)");
                matcher = pattern.matcher(expandedUrl);
                if (matcher.find()) {
                    videoId = matcher.group(1);
                }
            }

            if (videoId != null) {
                VideoInfo info = new VideoInfo();
                info.platform = "tiktok";
                info.title = "TikTok Video " + videoId;
                // For demo purposes - actual implementation would extract real video URL
                info.downloadUrl = getTikTokVideoUrl(videoId);
                return info;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "TikTok extraction error", e);
        }
        return null;
    }

    private VideoInfo extractInstagramInfo(String url, String quality) {
        try {
            // Extract Instagram post ID
            Pattern pattern = Pattern.compile("instagram\\.com/(?:p|reel|tv)/(\\w+)");
            Matcher matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                String postId = matcher.group(1);
                VideoInfo info = new VideoInfo();
                info.platform = "instagram";
                info.title = "Instagram Video " + postId;
                // For demo purposes - actual implementation would extract real video URL
                info.downloadUrl = getInstagramVideoUrl(postId);
                return info;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Instagram extraction error", e);
        }
        return null;
    }

    private VideoInfo extractFacebookInfo(String url, String quality) {
        try {
            // Extract Facebook video ID
            Pattern pattern = Pattern.compile("facebook\\.com.*?/videos/(\\d+)");
            Matcher matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                String videoId = matcher.group(1);
                VideoInfo info = new VideoInfo();
                info.platform = "facebook";
                info.title = "Facebook Video " + videoId;
                // For demo purposes - actual implementation would extract real video URL
                info.downloadUrl = getFacebookVideoUrl(videoId);
                return info;
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Facebook extraction error", e);
        }
        return null;
    }

    private String expandShortUrl(String shortUrl) {
        try {
            URL url = new URL(shortUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("HEAD");
            connection.connect();
            
            String expandedUrl = connection.getHeaderField("Location");
            connection.disconnect();
            
            return expandedUrl != null ? expandedUrl : shortUrl;
        } catch (Exception e) {
            Log.e(TAG, "Error expanding URL", e);
            return shortUrl;
        }
    }

    private String extractYouTubeVideoId(String url) {
        Pattern pattern = Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([\\w-]+)");
        Matcher matcher = pattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    // Demo methods - in production, these would use proper APIs or web scraping
    private String getTikTokVideoUrl(String videoId) {
        // This is a placeholder - actual implementation would require proper TikTok API
        return "https://example.com/demo_tiktok_video.mp4";
    }

    private String getInstagramVideoUrl(String postId) {
        // This is a placeholder - actual implementation would require proper Instagram API
        return "https://example.com/demo_instagram_video.mp4";
    }

    private String getFacebookVideoUrl(String videoId) {
        // This is a placeholder - actual implementation would require proper Facebook API
        return "https://example.com/demo_facebook_video.mp4";
    }

    private void downloadFile(String fileUrl, String outputPath) {
        try {
            // For demo purposes, we'll create a sample video file
            // In production, this would download the actual video
            createDemoVideoFile(outputPath);
            
            if (listener != null) {
                listener.onSuccess(outputPath);
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Download error", e);
            if (listener != null) {
                listener.onError("Download failed: " + e.getMessage());
            }
        }
    }

    private void createDemoVideoFile(String outputPath) throws IOException {
        // Create a demo file to simulate download
        File file = new File(outputPath);
        FileWriter writer = new FileWriter(file);
        writer.write("This is a demo video file. In production, this would contain actual video data.");
        writer.close();
        
        // Simulate download progress
        for (int i = 0; i <= 100; i += 10) {
            if (listener != null) {
                listener.onProgress(i);
            }
            try {
                Thread.sleep(200); // Simulate download time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private String generateFileName(String title, String platform) {
        // Clean the title for filename
        String cleanTitle = title.replaceAll("[^a-zA-Z0-9\\-_\\.]", "_");
        if (cleanTitle.length() > 50) {
            cleanTitle = cleanTitle.substring(0, 50);
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        return platform + "_" + cleanTitle + "_" + timestamp + ".mp4";
    }

    public static class VideoInfo {
        public String title;
        public String downloadUrl;
        public String platform;
        public String quality;
        public long duration;
        public String thumbnail;
    }
}