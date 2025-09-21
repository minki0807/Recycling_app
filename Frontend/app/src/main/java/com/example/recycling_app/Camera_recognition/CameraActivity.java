package com.example.recycling_app.Camera_recognition;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.recycling_app.R;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * CameraActivity는 사용자에게 카메라 미리보기를 제공하고,
 * 사용자가 특정 프레임 영역 내에서 사진을 찍을 수 있도록 하는 액티비티입니다.
 * 사진 촬영 후, 지정된 영역을 잘라내어 Photo_Recognition 액티비티로 전달합니다.
 */
public class CameraActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_PERMISSIONS = 10; // 권한 요청을 위한 고유 코드
    private static final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA}; // 필수 권한 목록
    private static final String TAG = "CameraActivity"; // 로그 태그

    // UI 컴포넌트
    private PreviewView cameraPreview;      // 카메라 미리보기를 표시하는 뷰
    private Button captureButton;           // 사진 촬영 버튼
    private View recognitionFrame;          // 사진을 자를 영역을 나타내는 프레임 뷰

    // CameraX 관련 객체
    private ImageCapture imageCapture;      // 이미지 캡처 사용 사례
    private ExecutorService cameraExecutor; // 카메라 작업 실행을 위한 스레드 풀

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cameraview); // 레이아웃 설정

        // UI 컴포넌트 초기화
        cameraPreview = findViewById(R.id.cameraPreview);
        captureButton = findViewById(R.id.button_capture);
        recognitionFrame = findViewById(R.id.recognition_frame);
        cameraExecutor = Executors.newSingleThreadExecutor(); // 단일 스레드 Executor 초기화

        // 카메라 권한 확인
        if (allPermissionsGranted()) {
            startCamera(); // 권한이 있으면 카메라 시작
        } else {
            // 권한이 없으면 사용자에게 요청
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        captureButton.setOnClickListener(v -> takePhoto()); // 사진 촬영 버튼 클릭 리스너 설정

        // 시스템 UI (상태바) 설정: 아이콘/텍스트를 밝게 표시하도록 설정
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }
    }

    /**
     * 필요한 모든 권한이 부여되었는지 확인합니다.
     * @return 모든 권한이 있으면 true, 아니면 false
     */
    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * CameraX를 사용하여 카메라를 시작합니다.
     * 미리보기, 이미지 캡처 사용 사례를 바인딩합니다.
     */
    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                // 카메라 프로바이더 가져오기
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                // 미리보기 사용 사례 빌드
                Preview preview = new Preview.Builder().build();
                // 미리보기 뷰에 Surface 제공
                preview.setSurfaceProvider(cameraPreview.getSurfaceProvider());

                // 이미지 캡처 사용 사례 빌드
                imageCapture = new ImageCapture.Builder().build();

                // 후면 카메라 선택
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                // 기존 바인딩 해제
                cameraProvider.unbindAll();
                // 라이프사이클에 카메라 바인딩
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                // 카메라 시작 중 오류 발생
                Log.e(TAG, "카메라 시작 실패", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    /**
     * 사진을 촬영하고, 미리보기 프레임에 맞춰 이미지를 자른 후,
     * Photo_Recognition 액티비티로 전달합니다.
     */
    private void takePhoto() {
        if (imageCapture == null) return;
        // 촬영 중 버튼 비활성화
        captureButton.setEnabled(false);

        // 원본 이미지를 저장할 임시 파일 생성
        File photoFile = new File(getCacheDir(), "original_" + System.currentTimeMillis() + ".jpg");

        // 출력 파일 옵션 설정
        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // 사진 촬영 실행
        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Bitmap originalBitmap = null;
                        Bitmap rotatedBitmap = null;
                        Bitmap croppedBitmap = null;

                        try {
                            // 1. 원본 이미지 로드
                            originalBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());

                            // 2. 이미지 회전
                            rotatedBitmap = rotateImageIfRequired(originalBitmap, photoFile.getAbsolutePath());

                            // 3. 미리보기(PreviewView)와 원본 이미지의 크기 비율 계산
                            float previewWidth = cameraPreview.getWidth();
                            float previewHeight = cameraPreview.getHeight();
                            float imageWidth = rotatedBitmap.getWidth();
                            float imageHeight = rotatedBitmap.getHeight();

                            // 화면에 보이는 이미지 영역의 크기와 시작점 계산
                            float scaleFactor = Math.max(previewWidth / imageWidth, previewHeight / imageHeight);
                            float imageStartX = (previewWidth - (imageWidth * scaleFactor)) / 2;
                            float imageStartY = (previewHeight - (imageHeight * scaleFactor)) / 2;

                            // 4. `recognition_frame` 뷰의 화면상 좌표를 실제 이미지 좌표로 변환
                            int frameLeft = (int) ((recognitionFrame.getLeft() - imageStartX) / scaleFactor);
                            int frameTop = (int) ((recognitionFrame.getTop() - imageStartY) / scaleFactor);
                            int frameWidth = (int) (recognitionFrame.getWidth() / scaleFactor);
                            int frameHeight = (int) (recognitionFrame.getHeight() / scaleFactor);

                            // 5. 이미지 경계를 벗어나지 않도록 자르기 영역 보정
                            frameLeft = Math.max(0, frameLeft);
                            frameTop = Math.max(0, frameTop);
                            frameWidth = Math.min(frameWidth, (int) (imageWidth - frameLeft));
                            frameHeight = Math.min(frameHeight, (int) (imageHeight - frameTop));

                            // 6. 원본 비트맵에서 지정된 영역 자르기
                            croppedBitmap = Bitmap.createBitmap(rotatedBitmap, frameLeft, frameTop, frameWidth, frameHeight);

                            // 7. 잘라낸 비트맵을 새 파일로 저장
                            String croppedFileName = "cropped_" + System.currentTimeMillis() + ".jpg";
                            File croppedFile = new File(getCacheDir(), croppedFileName);
                            try (FileOutputStream out = new FileOutputStream(croppedFile)) {
                                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                            }

                            // 원본 파일은 삭제하여 캐시 공간 확보
                            photoFile.delete();

                            // 8. 잘라낸 이미지의 Uri를 `Photo_Recognition` 액티비티로 전달하고 시작
                            Uri savedUri = Uri.fromFile(croppedFile);
                            Intent intent = new Intent(CameraActivity.this, Photo_Recognition.class);
                            intent.putExtra("imageUri", savedUri.toString());
                            startActivity(intent);
                            finish(); // 현재 액티비티 종료                          
                        } catch (Exception e) {
                            Log.e(TAG, "이미지 자르기 또는 저장 실패", e);
                            onError(new ImageCaptureException(ImageCapture.ERROR_FILE_IO, "Failed to crop/save image", e));
                        } finally {
                            // 사용한 모든 Bitmap 객체를 메모리에서 명시적으로 해제합니다.
                            if (originalBitmap != null && !originalBitmap.isRecycled()) {
                                originalBitmap.recycle();
                            }
                            // rotatedBitmap이 originalBitmap과 다른 객체일 경우에만 recycle 호출
                            if (rotatedBitmap != null && !rotatedBitmap.isRecycled() && rotatedBitmap != originalBitmap) {
                                rotatedBitmap.recycle();
                            }
                            if (croppedBitmap != null && !croppedBitmap.isRecycled()) {
                                croppedBitmap.recycle();
                            }
                            // 가비지 컬렉터 호출을 시스템에 제안
                            System.gc();
                        }
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Log.e(TAG, "사진 촬영 실패: " + exception.getMessage(), exception);
                        captureButton.setEnabled(true); // 버튼 다시 활성화
                        Toast.makeText(CameraActivity.this, "사진 촬영에 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * EXIF 정보에 따라 이미지를 회전시키는 헬퍼 메서드.
     * @param img 회전할 Bitmap
     * @param imagePath 이미지 파일 경로
     * @return 회전된 Bitmap, 회전이 필요 없으면 원본 Bitmap 반환
     */
    private Bitmap rotateImageIfRequired(Bitmap img, String imagePath) throws IOException {
        ExifInterface ei = new ExifInterface(imagePath);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    /**
     * 주어진 각도만큼 이미지를 회전시키는 헬퍼 메서드.
     * @param source 원본 Bitmap
     * @param angle 회전할 각도
     * @return 회전된 Bitmap
     */
    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    /**
     * 권한 요청 결과 처리
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera(); // 권한이 부여되면 카메라 시작
            } else {
                Toast.makeText(this, "카메라 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show();
                finish(); // 권한 없으면 액티비티 종료
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown(); // 스레드 풀 종료
    }
}