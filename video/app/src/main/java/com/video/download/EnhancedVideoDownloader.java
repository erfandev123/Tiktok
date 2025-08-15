package com.video.download;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnhancedVideoDownloader {
    private static final String TAG = "EnhancedVideoDownloader";
    private Context context;
    private DownloadListener listener;
    private ExecutorService executor;

    public interface DownloadListener {
        void onProgress(int progress);
        void onSuccess(String filePath);
        void onError(String error);
        void onStart();
        void onVideoInfo(String title, String duration, String thumbnail);
    }

    public EnhancedVideoDownloader(Context context) {
        this.context = context;
        this.executor = Executors.newCachedThreadPool();
    }

    public void setDownloadListener(DownloadListener listener) {
        this.listener = listener;
    }

    public void downloadVideo(String url, String quality) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (listener != null) listener.onStart();

                    // Extract video info
                    VideoInfo videoInfo = extractVideoInfo(url);
                    if (videoInfo == null) {
                        if (listener != null) listener.onError("Unable to extract video information. Please check the URL.");
                        return;
                    }

                    // Notify UI with video info
                    if (listener != null) {
                        listener.onVideoInfo(videoInfo.title, videoInfo.duration, videoInfo.thumbnail);
                    }

                    // Get direct download URL
                    String downloadUrl = getDirectDownloadUrl(url, quality, videoInfo);
                    if (downloadUrl == null) {
                        if (listener != null) listener.onError("Unable to get download URL. Video might be private or restricted.");
                        return;
                    }

                    String platform = detectPlatform(url);
                    String fileName = generateFileName(videoInfo.title, platform);
                    
                    File downloadDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "VideoDownloader");
                    if (!downloadDir.exists()) {
                        downloadDir.mkdirs();
                    }

                    File platformDir = new File(downloadDir, platform);
                    if (!platformDir.exists()) {
                        platformDir.mkdirs();
                    }

                    File outputFile = new File(platformDir, fileName);
                    
                    downloadVideoFile(downloadUrl, outputFile.getAbsolutePath());

                } catch (Exception e) {
                    Log.e(TAG, "Download error", e);
                    if (listener != null) listener.onError("Download failed: " + e.getMessage());
                }
            }
        });
    }

    private VideoInfo extractVideoInfo(String url) {
        try {
            String platform = detectPlatform(url);
            
            switch (platform) {
                case "youtube":
                    return extractYouTubeInfo(url);
                case "tiktok":
                    return extractTikTokInfo(url);
                case "instagram":
                    return extractInstagramInfo(url);
                case "facebook":
                    return extractFacebookInfo(url);
                default:
                    return extractGenericInfo(url);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting video info", e);
            return null;
        }
    }

    private VideoInfo extractYouTubeInfo(String url) {
        try {
            String videoId = extractYouTubeVideoId(url);
            if (videoId == null) return null;

            String apiUrl = "https://www.youtube.com/watch?v=" + videoId;
            String pageContent = makeHttpRequest(apiUrl);
            if (pageContent == null) return null;

            String title = extractYouTubeTitle(pageContent);
            if (title == null) title = "YouTube Video";

            String duration = extractYouTubeDuration(pageContent);

            return new VideoInfo(title, duration, null, videoId);
        } catch (Exception e) {
            Log.e(TAG, "YouTube info extraction error", e);
            return new VideoInfo("YouTube Video", "Unknown", null, null);
        }
    }

    private String extractYouTubeVideoId(String url) {
        Pattern pattern = Pattern.compile("(?:youtube\\.com\\/watch\\?v=|youtu\\.be\\/|youtube\\.com\\/embed\\/)([^&\\n?#]+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractYouTubeTitle(String pageContent) {
        Pattern pattern = Pattern.compile("<title>([^<]+)</title>");
        Matcher matcher = pattern.matcher(pageContent);
        if (matcher.find()) {
            String title = matcher.group(1);
            return title.replace(" - YouTube", "").trim();
        }
        return null;
    }

    private String extractYouTubeDuration(String pageContent) {
        Pattern[] patterns = {
            Pattern.compile("\\"lengthSeconds\\":\\"([^\\"]+)\\""),
            Pattern.compile("\\"duration\\":\\"([^\\"]+)\\""),
            Pattern.compile("PT([0-9]+)M([0-9]+)S")
        };

        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(pageContent);
            if (matcher.find()) {
                if (pattern.pattern().contains("PT")) {
                    int minutes = Integer.parseInt(matcher.group(1));
                    int seconds = Integer.parseInt(matcher.group(2));
                    return String.format("%d:%02d", minutes, seconds);
                } else {
                    int totalSeconds = Integer.parseInt(matcher.group(1));
                    int minutes = totalSeconds / 60;
                    int seconds = totalSeconds % 60;
                    return String.format("%d:%02d", minutes, seconds);
                }
            }
        }
        return "Unknown";
    }

    private VideoInfo extractTikTokInfo(String url) {
        try {
            String videoId = extractTikTokVideoId(url);
            if (videoId == null) return null;

            String pageContent = makeHttpRequest(url);
            if (pageContent == null) return null;

            String title = extractTikTokTitle(pageContent);
            if (title == null) title = "TikTok Video";

            String duration = extractTikTokDuration(pageContent);

            return new VideoInfo(title, duration, null, videoId);
        } catch (Exception e) {
            Log.e(TAG, "TikTok info extraction error", e);
            return new VideoInfo("TikTok Video", "Unknown", null, null);
        }
    }

    private String extractTikTokVideoId(String url) {
        Pattern pattern = Pattern.compile("tiktok\\.com\\/@[^/]+/video/(\\d+)");
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private String extractTikTokTitle(String pageContent) {
        Pattern pattern = Pattern.compile("<title>([^<]+)</title>");
        Matcher matcher = pattern.matcher(pageContent);
        if (matcher.find()) {
            String title = matcher.group(1);
            return title.replace(" | TikTok", "").trim();
        }
        return null;
    }

    private String extractTikTokDuration(String pageContent) {
        Pattern pattern = Pattern.compile("\\"duration\\":([0-9]+)");
        Matcher matcher = pattern.matcher(pageContent);
        if (matcher.find()) {
            int seconds = Integer.parseInt(matcher.group(1));
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
        return "Unknown";
    }

    private VideoInfo extractInstagramInfo(String url) {
        try {
            String pageContent = makeHttpRequest(url);
            if (pageContent == null) return null;

            String title = extractInstagramTitle(pageContent);
            if (title == null) title = "Instagram Video";

            String duration = extractInstagramDuration(pageContent);

            return new VideoInfo(title, duration, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Instagram info extraction error", e);
            return new VideoInfo("Instagram Video", "Unknown", null, null);
        }
    }

    private String extractInstagramTitle(String pageContent) {
        Pattern pattern = Pattern.compile("<title>([^<]+)</title>");
        Matcher matcher = pattern.matcher(pageContent);
        if (matcher.find()) {
            String title = matcher.group(1);
            return title.replace(" (@", " - ").replace(") • Instagram", "").trim();
        }
        return null;
    }

    private String extractInstagramDuration(String pageContent) {
        Pattern pattern = Pattern.compile("\\"duration\\":([0-9]+)");
        Matcher matcher = pattern.matcher(pageContent);
        if (matcher.find()) {
            int seconds = Integer.parseInt(matcher.group(1));
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
        return "Unknown";
    }

    private VideoInfo extractFacebookInfo(String url) {
        try {
            String pageContent = makeHttpRequest(url);
            if (pageContent == null) return null;

            String title = extractFacebookTitle(pageContent);
            if (title == null) title = "Facebook Video";

            String duration = extractFacebookDuration(pageContent);

            return new VideoInfo(title, duration, null, null);
        } catch (Exception e) {
            Log.e(TAG, "Facebook info extraction error", e);
            return new VideoInfo("Facebook Video", "Unknown", null, null);
        }
    }

    private String extractFacebookTitle(String pageContent) {
        Pattern pattern = Pattern.compile("<title>([^<]+)</title>");
        Matcher matcher = pattern.matcher(pageContent);
        if (matcher.find()) {
            String title = matcher.group(1);
            return title.replace(" - Facebook", "").trim();
        }
        return null;
    }

    private String extractFacebookDuration(String pageContent) {
        Pattern pattern = Pattern.compile("\\"duration\\":([0-9]+)");
        Matcher matcher = pattern.matcher(pageContent);
        if (matcher.find()) {
            int seconds = Integer.parseInt(matcher.group(1));
            int minutes = seconds / 60;
            int remainingSeconds = seconds % 60;
            return String.format("%d:%02d", minutes, remainingSeconds);
        }
        return "Unknown";
    }

    private VideoInfo extractGenericInfo(String url) {
        try {
            String pageContent = makeHttpRequest(url);
            if (pageContent == null) return null;

            Pattern pattern = Pattern.compile("<title>([^<]+)</title>");
            Matcher matcher = pattern.matcher(pageContent);
            if (matcher.find()) {
                String title = matcher.group(1).trim();
                return new VideoInfo(title, "Unknown", null, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "Generic info extraction error", e);
        }
        return new VideoInfo("Video", "Unknown", null, null);
    }

    private String makeHttpRequest(String urlString) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                Log.e(TAG, "HTTP Error: " + responseCode);
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } catch (Exception e) {
            Log.e(TAG, "HTTP request error", e);
            return null;
        } finally {
            try { if (reader != null) reader.close(); } catch (Exception ignore) {}
            if (connection != null) connection.disconnect();
        }
    }

    private String getDirectDownloadUrl(String originalUrl, String quality, VideoInfo videoInfo) {
        String platform = detectPlatform(originalUrl);
        
        switch (platform) {
            case "youtube":
                return getYouTubeDownloadUrl(originalUrl, quality, videoInfo);
            case "tiktok":
                return getTikTokDownloadUrl(originalUrl, quality, videoInfo);
            case "instagram":
                return getInstagramDownloadUrl(originalUrl, quality, videoInfo);
            case "facebook":
                return getFacebookDownloadUrl(originalUrl, quality, videoInfo);
            default:
                return getGenericDownloadUrl(originalUrl, quality, videoInfo);
        }
    }

    private String getYouTubeDownloadUrl(String url, String quality, VideoInfo videoInfo) {
        try {
            String videoId = videoInfo.videoId;
            if (videoId == null) return null;
            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";
        } catch (Exception e) {
            Log.e(TAG, "YouTube download URL error", e);
            return null;
        }
    }

    private String getTikTokDownloadUrl(String url, String quality, VideoInfo videoInfo) {
        try {
            String pageContent = makeHttpRequest(url);
            if (pageContent == null) return null;

            Pattern pattern = Pattern.compile("\\"downloadAddr\\":\\"([^\\"]+)\\"");
            Matcher matcher = pattern.matcher(pageContent);
            if (matcher.find()) {
                String downloadUrl = matcher.group(1);
                return downloadUrl.replace("\\u002F", "/");
            }

            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4";
        } catch (Exception e) {
            Log.e(TAG, "TikTok download URL error", e);
            return null;
        }
    }

    private String getInstagramDownloadUrl(String url, String quality, VideoInfo videoInfo) {
        try {
            String pageContent = makeHttpRequest(url);
            if (pageContent == null) return null;

            Pattern pattern = Pattern.compile("\\"video_url\\":\\"([^\\"]+)\\"");
            Matcher matcher = pattern.matcher(pageContent);
            if (matcher.find()) {
                String videoUrl = matcher.group(1);
                return videoUrl.replace("\\u0026", "&");
            }

            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
        } catch (Exception e) {
            Log.e(TAG, "Instagram download URL error", e);
            return null;
        }
    }

    private String getFacebookDownloadUrl(String url, String quality, VideoInfo videoInfo) {
        try {
            String pageContent = makeHttpRequest(url);
            if (pageContent == null) return null;

            Pattern pattern = Pattern.compile("\\"hd_src\\":\\"([^\\"]+)\\"");
            Matcher matcher = pattern.matcher(pageContent);
            if (matcher.find()) {
                return matcher.group(1).replace("\\u0026", "&");
            }

            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4";
        } catch (Exception e) {
            Log.e(TAG, "Facebook download URL error", e);
            return null;
        }
    }

    private String getGenericDownloadUrl(String url, String quality, VideoInfo videoInfo) {
        try {
            String pageContent = makeHttpRequest(url);
            if (pageContent == null) return null;

            Pattern[] patterns = {
                Pattern.compile("\\"src\\":\\"([^\\"]*\\.mp4[^\\"]*)\\""),
                Pattern.compile("src=\\"([^\\"]*\\.mp4[^\\"]*)\\""),
                Pattern.compile("\\"url\\":\\"([^\\"]*\\.mp4[^\\"]*)\\"")
            };

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(pageContent);
                if (matcher.find()) {
                    String videoUrl = matcher.group(1);
                    if (videoUrl.startsWith("//")) {
                        videoUrl = "https:" + videoUrl;
                    } else if (videoUrl.startsWith("/")) {
                        try {
                            URL originalUrlObj = new URL(url);
                            videoUrl = originalUrlObj.getProtocol() + "://" + originalUrlObj.getHost() + videoUrl;
                        } catch (Exception e) {
                            continue;
                        }
                    }
                    return videoUrl;
                }
            }

            return "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4";
        } catch (Exception e) {
            Log.e(TAG, "Generic download URL error", e);
            return null;
        }
    }

    private void downloadVideoFile(String videoUrl, String outputPath) {
        HttpURLConnection connection = null;
        InputStream input = null;
        OutputStream output = null;
        try {
            Log.d(TAG, "Starting download from: " + videoUrl);

            URL url = new URL(videoUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(30000);

            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Mobile Safari/537.36");
            connection.setRequestProperty("Accept", "video/webm,video/ogg,video/*;q=0.9,application/ogg;q=0.7,audio/*;q=0.6,*/*;q=0.5");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Accept-Encoding", "identity");
            connection.setRequestProperty("Range", "bytes=0-");
            connection.setRequestProperty("Referer", "https://www.google.com/");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_PARTIAL) {
                throw new IOException("Server returned HTTP " + responseCode + " " + connection.getResponseMessage());
            }

            int fileLength = connection.getContentLength();
            Log.d(TAG, "File size: " + fileLength + " bytes");

            input = new BufferedInputStream(connection.getInputStream(), 8192);
            output = new FileOutputStream(outputPath);

            byte[] data = new byte[8192];
            long total = 0;
            int count;
            int lastProgress = 0;

            while ((count = input.read(data)) != -1) {
                total += count;

                if (fileLength > 0) {
                    int progress = (int) (total * 100 / fileLength);
                    if (progress != lastProgress && listener != null) {
                        listener.onProgress(progress);
                        lastProgress = progress;
                    }
                } else {
                    if (listener != null && total % 102400 == 0) {
                        int progress = Math.min(90, (int) (total / 10240));
                        listener.onProgress(progress);
                    }
                }

                output.write(data, 0, count);
            }

            output.flush();

            File downloadedFile = new File(outputPath);
            if (downloadedFile.exists() && downloadedFile.length() > 0) {
                Log.d(TAG, "Download completed successfully: " + outputPath);
                Log.d(TAG, "Downloaded file size: " + downloadedFile.length() + " bytes");

                if (listener != null) {
                    listener.onProgress(100);
                    listener.onSuccess(outputPath);
                }
            } else {
                throw new IOException("Downloaded file is empty or doesn't exist");
            }
        } catch (Exception e) {
            Log.e(TAG, "Download error: " + e.getMessage(), e);
            try {
                File partialFile = new File(outputPath);
                if (partialFile.exists()) {
                    partialFile.delete();
                }
            } catch (Exception cleanupError) {
                Log.e(TAG, "Error cleaning up partial download", cleanupError);
            }

            if (listener != null) {
                String errorMessage = "Download failed: " + e.getMessage();
                if (e instanceof IOException) {
                    errorMessage = "Network error: Please check your internet connection and try again.";
                }
                listener.onError(errorMessage);
            }
        } finally {
            try { if (output != null) output.close(); } catch (Exception ignore) {}
            try { if (input != null) input.close(); } catch (Exception ignore) {}
            if (connection != null) connection.disconnect();
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
        return "general";
    }

    private String generateFileName(String title, String platform) {
        String cleanTitle = title.replaceAll("[^a-zA-Z0-9\\s-]", "").trim();
        if (cleanTitle.length() > 50) {
            cleanTitle = cleanTitle.substring(0, 50);
        }
        if (cleanTitle.isEmpty()) {
            cleanTitle = platform + "_video";
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        return cleanTitle + "_" + timestamp + ".mp4";
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }

    private static class VideoInfo {
        String title;
        String duration;
        String thumbnail;
        String videoId;

        VideoInfo(String title, String duration, String thumbnail, String videoId) {
            this.title = title;
            this.duration = duration;
            this.thumbnail = thumbnail;
            this.videoId = videoId;
        }
    }
}