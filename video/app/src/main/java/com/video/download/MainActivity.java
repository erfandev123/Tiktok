package com.video.download;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import android.os.Build;
import android.graphics.Color;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.text.TextUtils;

public class MainActivity extends Activity {
    private static final int STORAGE_PERMISSION_CODE = 1001;
    
    private EditText etVideoUrl;
    private Spinner spinnerQuality;
    private Button btnDownload;
    private LinearLayout progressCard;
    private TextView tvDownloadStatus;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private LinearLayout videoInfoCard;
    private TextView tvVideoTitle;
    private TextView tvVideoDuration;
    private ImageView ivVideoThumbnail;
    
    private EnhancedVideoDownloader videoDownloader;
    private ErrorHandler errorHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupSpinner();
        setupDownloader();
        setupErrorHandler();
        checkPermissions();
        
        // Log app state for debugging
        ErrorHandler.logAppState(this);
    }

    private void initializeViews() {
        etVideoUrl = findViewById(R.id.etVideoUrl);
        spinnerQuality = findViewById(R.id.spinnerQuality);
        btnDownload = findViewById(R.id.btnDownload);
        progressCard = findViewById(R.id.progressCard);
        tvDownloadStatus = findViewById(R.id.tvDownloadStatus);
        progressBar = findViewById(R.id.progressBar);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        videoInfoCard = findViewById(R.id.videoInfoCard);
        tvVideoTitle = findViewById(R.id.tvVideoTitle);
        tvVideoDuration = findViewById(R.id.tvVideoDuration);
        ivVideoThumbnail = findViewById(R.id.ivVideoThumbnail);

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDownload();
            }
        });

        // Add URL change listener for real-time validation
        etVideoUrl.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateUrlInRealTime(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void validateUrlInRealTime(String url) {
        if (TextUtils.isEmpty(url.trim())) {
            btnDownload.setEnabled(false);
            btnDownload.setBackgroundColor(Color.parseColor("#cccccc"));
            return;
        }

        ErrorHandler.ValidationResult validation = ErrorHandler.validateUrl(url);
        if (validation.isValid()) {
            btnDownload.setEnabled(true);
            btnDownload.setBackgroundColor(Color.parseColor("#27ae60"));
            
            // Show platform indicator
            String platform = detectPlatformFromUrl(url);
            showPlatformIndicator(platform);
        } else {
            btnDownload.setEnabled(false);
            btnDownload.setBackgroundColor(Color.parseColor("#cccccc"));
        }
    }

    private void showPlatformIndicator(String platform) {
        // Update UI to show detected platform
        TextView tvPlatform = findViewById(R.id.tvPlatform);
        if (tvPlatform != null) {
            String platformText = "📹 " + platform;
            tvPlatform.setText(platformText);
            tvPlatform.setVisibility(View.VISIBLE);
        }
    }

    private void setupSpinner() {
        String[] qualities = {
            "High Quality (1080p)",
            "Medium Quality (720p)", 
            "Low Quality (480p)",
            "Audio Only (MP3)"
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this, 
            android.R.layout.simple_spinner_item, 
            qualities
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerQuality.setAdapter(adapter);
        spinnerQuality.setSelection(1); // Default to Medium Quality
    }

    private void setupErrorHandler() {
        errorHandler = new ErrorHandler(this);
    }

    private void setupDownloader() {
        videoDownloader = new EnhancedVideoDownloader(this);
        videoDownloader.setDownloadListener(new EnhancedVideoDownloader.DownloadListener() {
            @Override
            public void onStart() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnDownload.setEnabled(false);
                        btnDownload.setText("Analyzing Video...");
                        progressCard.setVisibility(View.VISIBLE);
                        tvDownloadStatus.setText("Extracting video information...");
                        progressBar.setProgress(0);
                        tvProgressPercent.setText("0%");
                        
                        // Hide video info card
                        videoInfoCard.setVisibility(View.GONE);
                        
                        // Add animation
                        Animation fadeIn = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_in);
                        progressCard.startAnimation(fadeIn);
                    }
                });
            }

            @Override
            public void onProgress(final int progress) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                        tvProgressPercent.setText(progress + "%");
                        
                        if (progress < 10) {
                            tvDownloadStatus.setText("Preparing download...");
                        } else if (progress < 50) {
                            tvDownloadStatus.setText("Downloading video...");
                        } else if (progress < 90) {
                            tvDownloadStatus.setText("Almost done...");
                        } else {
                            tvDownloadStatus.setText("Finalizing...");
                        }
                    }
                });
            }

            @Override
            public void onSuccess(final String filePath) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnDownload.setEnabled(true);
                        btnDownload.setText("Download Video");
                        tvDownloadStatus.setText("✅ Download completed successfully!");
                        progressBar.setProgress(100);
                        tvProgressPercent.setText("100%");
                        
                        showSuccessToast("Video downloaded successfully!\nSaved to: " + filePath);
                        
                        // Hide progress card after 3 seconds
                        progressCard.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Animation fadeOut = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.fade_out);
                                progressCard.startAnimation(fadeOut);
                                progressCard.setVisibility(View.GONE);
                            }
                        }, 3000);
                    }
                });
            }

            @Override
            public void onError(final String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        btnDownload.setEnabled(true);
                        btnDownload.setText("Download Video");
                        progressCard.setVisibility(View.GONE);
                        showErrorToast("Download failed: " + error);
                    }
                });
            }

            @Override
            public void onVideoInfo(final String title, final String duration, final String thumbnail) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Show video info card
                        tvVideoTitle.setText(title);
                        tvVideoDuration.setText(duration);
                        
                        // Show video info with animation
                        videoInfoCard.setVisibility(View.VISIBLE);
                        Animation slideIn = AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left);
                        videoInfoCard.startAnimation(slideIn);
                        
                        // Update download button
                        btnDownload.setText("Download Now");
                        btnDownload.setBackgroundColor(Color.parseColor("#e74c3c"));
                    }
                });
            }
        });
    }

    private void startDownload() {
        String url = etVideoUrl.getText().toString().trim();
        
        // Enhanced URL validation
        ErrorHandler.ValidationResult validation = ErrorHandler.validateUrl(url);
        if (!validation.isValid()) {
            showErrorToast(validation.getMessage());
            return;
        }

        // Check system requirements
        ErrorHandler.SystemCheckResult systemCheck = ErrorHandler.checkSystemRequirements(this);
        if (systemCheck.hasErrors()) {
            for (String error : systemCheck.getErrors()) {
                showErrorToast("System Error: " + error);
            }
            return;
        }

        if (!hasStoragePermission()) {
            requestStoragePermission();
            return;
        }

        String selectedQuality = spinnerQuality.getSelectedItem().toString();
        String quality = parseQuality(selectedQuality);
        
        try {
            String platform = detectPlatformFromUrl(url);
            showInfoToast("Starting download from " + platform + "...");
            videoDownloader.downloadVideo(url, quality);
        } catch (Exception e) {
            errorHandler.handleError(e, "Starting download");
        }
    }

    private String detectPlatformFromUrl(String url) {
        url = url.toLowerCase();
        if (url.contains("youtube.com") || url.contains("youtu.be")) {
            return "YouTube";
        } else if (url.contains("tiktok.com") || url.contains("vm.tiktok.com")) {
            return "TikTok";
        } else if (url.contains("instagram.com")) {
            return "Instagram";
        } else if (url.contains("facebook.com") || url.contains("fb.watch")) {
            return "Facebook";
        }
        return "Video";
    }

    private boolean isValidUrl(String url) {
        url = url.toLowerCase();
        return url.contains("youtube.com") || 
               url.contains("youtu.be") ||
               url.contains("tiktok.com") ||
               url.contains("vm.tiktok.com") ||
               url.contains("instagram.com") ||
               url.contains("facebook.com") ||
               url.contains("fb.watch");
    }

    private String parseQuality(String selectedQuality) {
        if (selectedQuality.contains("1080p")) return "1080";
        else if (selectedQuality.contains("720p")) return "720";
        else if (selectedQuality.contains("480p")) return "480";
        else if (selectedQuality.contains("Audio")) return "audio";
        return "720"; // default
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showInfoToast("Storage permission is required to save downloaded videos");
            }
            
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
                STORAGE_PERMISSION_CODE);
        }
    }

    private void checkPermissions() {
        if (!hasStoragePermission()) {
            requestStoragePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showSuccessToast("Storage permission granted. You can now download videos.");
            } else {
                showErrorToast("Storage permission denied. Cannot download videos without permission.");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void showSuccessToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(Color.parseColor("#27ae60"));
        toast.show();
    }

    private void showErrorToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.getView().setBackgroundColor(Color.parseColor("#e74c3c"));
        toast.show();
    }

    private void showInfoToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.getView().setBackgroundColor(Color.parseColor("#3498db"));
        toast.show();
    }

    // Handle paste from clipboard
    @Override
    protected void onResume() {
        super.onResume();
        // Auto-paste from clipboard if URL field is empty
        if (etVideoUrl.getText().toString().trim().isEmpty()) {
            tryAutoFillFromClipboard();
        }
    }

    private void tryAutoFillFromClipboard() {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                getSystemService(CLIPBOARD_SERVICE);
            
            if (clipboard != null && clipboard.getPrimaryClip() != null) {
                android.content.ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);
                String clipText = item.getText().toString();
                
                if (isValidUrl(clipText)) {
                    etVideoUrl.setText(clipText);
                    showInfoToast("URL pasted from clipboard");
                }
            }
        } catch (Exception e) {
            // Ignore clipboard errors
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources
        if (videoDownloader != null) {
            videoDownloader.shutdown();
        }
    }
}