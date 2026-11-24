package com.agrireach.app.ui.admin;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.format.Formatter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.agrireach.app.R;
import com.agrireach.app.core.network.RetrofitClient;
import com.agrireach.app.data.remote.api.VideoApi;
import com.agrireach.app.data.repository.VideoRepository;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;


public class UploadVideoActivity extends AppCompatActivity {
    private static final long MAX_FILE_SIZE = 524288000L;
    private TextInputEditText videoTitleInput;
    private TextInputEditText videoDescriptionInput;
    private MaterialCardView selectVideoCard;
    private TextView selectedVideoText;
    private Button uploadVideoButton;
    private Uri selectedVideoUri;
    private String selectedVideoName;
    private long selectedVideoSize;
    private ActivityResultLauncher<String[]> videoPickerLauncher;

    private VideoRepository videoRepository;

    private VideoRepository.UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);

        // Initialize Retrofit API
        VideoApi videoApi = RetrofitClient.getInstance().create(VideoApi.class);

        videoRepository = new VideoRepository(videoApi, getContentResolver());

        // Initialize UI components
        videoTitleInput = findViewById(R.id.videoTitleInput);
        videoDescriptionInput = findViewById(R.id.videoDescriptionInput);
        selectVideoCard = findViewById(R.id.selectVideoCard);
        selectedVideoText = findViewById(R.id.selectedVideoText);
        uploadVideoButton = findViewById(R.id.uploadVideoButton);
        // Initialize video picker
        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        handleSelectedVideo(uri);
                    }
                }
        );
        // Open phone file picker
        selectVideoCard.setOnClickListener(v ->
                videoPickerLauncher.launch(new String[]{"video/mp4"})
        );
        // Upload button click
        uploadVideoButton.setOnClickListener(v ->
                validateAndRequestUpload()
        );
    }

    private void handleSelectedVideo(Uri uri) {
        selectedVideoUri = uri;
        // Preserve permission to access selected file
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException e) {
            // Current session access may still be available.
        }
        selectedVideoName = getFileName(uri);
        selectedVideoSize = getFileSize(uri);
        String fileDetails = selectedVideoName;
        if (selectedVideoSize > 0) {
            fileDetails += "\nSize: " +
                    Formatter.formatFileSize(this, selectedVideoSize);
        } else {
            fileDetails += "\nSize: Not available";
        }
        selectedVideoText.setText(fileDetails);
    }

    private String getFileName(Uri uri) {
        String fileName = "Selected video";
        try (Cursor cursor = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(
                        OpenableColumns.DISPLAY_NAME
                );
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        } catch (Exception e) {
            // Use default filename.
        }
        return fileName;
    }

    private long getFileSize(Uri uri) {
        try (Cursor cursor = getContentResolver().query(
                uri,
                null,
                null,
                null,
                null
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(
                        OpenableColumns.SIZE
                );
                if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                    return cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            // File size unavailable.
        }
        return -1;
    }

    private void validateAndRequestUpload() {
        String title = videoTitleInput.getText() != null
                ? videoTitleInput.getText().toString().trim()
                : "";
        String description = videoDescriptionInput.getText() != null
                ? videoDescriptionInput.getText().toString().trim()
                : "";
        if (title.isEmpty()) {
            videoTitleInput.setError("Please enter video title");
            return;
        }
        if (selectedVideoUri == null) {
            Toast.makeText(
                    this,
                    "Please select a video first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        if (selectedVideoSize <= 0) {
            Toast.makeText(
                    this,
                    "Unable to read video file size",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }
        if (selectedVideoSize > MAX_FILE_SIZE) {
            Toast.makeText(
                    this,
                    "Video cannot exceed 500 MB",
                    Toast.LENGTH_LONG
            ).show();
            return;
        }
        startVideoUpload(title, description);
    }

    private void startVideoUpload(String title, String description) {
        uploadVideoButton.setEnabled(false);
        uploadVideoButton.setText("Uploading... 0%");
        uploadTask = videoRepository.uploadVideo(
                selectedVideoUri,
                title,
                description,
                selectedVideoSize,
                new VideoRepository.UploadCallback() {
                    @Override
                    public void onProgress(int progress) {
                        uploadVideoButton.setText(
                                "Uploading... " + progress + "%"
                        );
                    }
                    @Override
                    public void onSuccess(String videoId) {

                        uploadVideoButton.setEnabled(true);
                        uploadVideoButton.setText("Upload Video");
                        Toast.makeText(
                                UploadVideoActivity.this,
                                "Video uploaded and verified successfully!",
                                Toast.LENGTH_LONG
                        ).show();

                        selectedVideoUri = null;
                        selectedVideoName = null;
                        selectedVideoSize = 0;
                        selectedVideoText.setText("No video selected");
                        videoTitleInput.setText("");
                        videoDescriptionInput.setText("");
                    }

                    @Override
                    public void onFailure(String message) {
                        uploadVideoButton.setEnabled(true);
                        uploadVideoButton.setText("Upload Video");
                        Toast.makeText(
                                UploadVideoActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

}