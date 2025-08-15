package com.video.download;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends Activity {
    private static final int STORAGE_PERMISSION_CODE = 1001;
    
    private TextInputEditText etVideoUrl;
    private Spinner spinnerQuality;
    private MaterialButton btnDownload;
    private CardView progressCard;
    private TextView tvDownloadStatus;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    
    private VideoDownloader videoDownloader;
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

        btnDownload.setOnClickListener(v -> startDownload());
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
        videoDownloader = new VideoDownloader(this);
        videoDownloader.setDownloadListener(new VideoDownloader.DownloadListener() {
            @Override
            public void onStart() {
                runOnUiThread(() -> {
                    btnDownload.setEnabled(false);
                    btnDownload.setText("Downloading...");
                    progressCard.setVisibility(View.VISIBLE);
                    tvDownloadStatus.setText("Preparing download...");
                    progressBar.setProgress(0);
                    tvProgressPercent.setText("0%");
                });
            }

            @Override
            public void onProgress(int progress) {
                runOnUiThread(() -> {
                    progressBar.setProgress(progress);
                    tvProgressPercent.setText(progress + "%");
                    tvDownloadStatus.setText("Downloading... " + progress + "%");
                });
            }

            @Override
            public void onSuccess(String filePath) {
                runOnUiThread(() -> {
                    btnDownload.setEnabled(true);
                    btnDownload.setText("Download Video");
                    tvDownloadStatus.setText("Download completed!");
                    progressBar.setProgress(100);
                    tvProgressPercent.setText("100%");
                    
                    showToast("Video downloaded successfully!\nSaved to: " + filePath);
                    
                    // Hide progress card after 3 seconds
                    progressCard.postDelayed(() -> {
                        progressCard.setVisibility(View.GONE);
                    }, 3000);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    btnDownload.setEnabled(true);
                    btnDownload.setText("Download Video");
                    progressCard.setVisibility(View.GONE);
                    showToast("Download failed: " + error);
                });
            }
        });
    }

    private void startDownload() {
        String url = etVideoUrl.getText().toString().trim();
        
        // Enhanced URL validation
        ErrorHandler.ValidationResult validation = ErrorHandler.validateUrl(url);
        if (!validation.isValid()) {
            showToast(validation.getMessage());
            return;
        }

        // Check system requirements
        ErrorHandler.SystemCheckResult systemCheck = ErrorHandler.checkSystemRequirements(this);
        if (systemCheck.hasErrors()) {
            for (String error : systemCheck.getErrors()) {
                showToast("System Error: " + error);
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
            showToast("Starting download...");
            videoDownloader.downloadVideo(url, quality);
        } catch (Exception e) {
            errorHandler.handleError(e, "Starting download");
        }
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
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showToast("Storage permission is required to save downloaded videos");
        }
        
        ActivityCompat.requestPermissions(this, 
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 
            STORAGE_PERMISSION_CODE);
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
                showToast("Storage permission granted. You can now download videos.");
            } else {
                showToast("Storage permission denied. Cannot download videos without permission.");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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
                    showToast("URL pasted from clipboard");
                }
            }
        } catch (Exception e) {
            // Ignore clipboard errors
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up resources if needed
        if (videoDownloader != null) {
            videoDownloader.setDownloadListener(null);
        }
    }
}