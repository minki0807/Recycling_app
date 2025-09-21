package com.example.recycling_app.Camera_recognition;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.bumptech.glide.Glide;
import com.example.recycling_app.Community.CommunityActivity;
import com.example.recycling_app.Location.LocationActivity;
import com.example.recycling_app.MainscreenActivity;
import com.example.recycling_app.Profile.MypageActivity;
import com.example.recycling_app.R;
import com.example.recycling_app.service.GeminiApiService;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.pytorch.IValue;
import org.pytorch.LiteModuleLoader;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * 사진을 받아 PyTorch 모델로 객체를 인식하고,
 * 그 결과를 바탕으로 분리수거 정보를 보여주는 액티비티입니다.
 * 사용자는 다른 분리수거 항목을 선택하여 정보를 다시 조회할 수도 있습니다.
 */
public class Photo_Recognition extends AppCompatActivity {

    // --- UI 컴포넌트 변수 선언 ---
    private ImageView resultImageView; // 촬영된 또는 선택된 이미지를 보여주는 뷰
    private TextView titleTextView; // "분리수거 종류: OOO" 텍스트를 보여주는 뷰
    private TextView resultTextView; // 분리수거 상세 설명 또는 API 응답을 보여주는 뷰
    private ProgressBar progressBar; // 데이터 로딩 중임을 나타내는 로딩 바
    private ImageButton recycling_baseline_list; // 다른 품목을 선택할 수 있는 목록 다이얼로그를 여는 버튼

    // --- 백엔드 및 데이터 처리 변수 선언 ---
    private FirebaseFirestore db; // Firebase Firestore 데이터베이스 인스턴스
    private final List<Module> pytorchModules = new ArrayList<>(); // 로드된 PyTorch 모델들을 저장하는 리스트
    private final ExecutorService executorService = Executors.newSingleThreadExecutor(); // 백그라운드 작업을 위한 스레드 풀
    private final AtomicBoolean isModelLoaded = new AtomicBoolean(false); // 모델 로딩 완료 여부를 저장하는 변수

    // --- 데이터 및 설정 변수 선언 ---
    private final Map<String, String> geminiCache = new HashMap<>(); // Gemini API 응답을 캐싱하여 반복 호출을 방지
    private final String[] modelFiles = {"Train_model1.ptl", "Train_model2.ptl"}; // 로드할 PyTorch 모델 파일 이름 배열
    // PyTorch 모델이 인식하는 클래스 라벨 (영어)
    private final String[] classLabelsEng = new String[]{"metal_can", "plastic_bag", "styrofoam", "glass_bottle", "paper", "plastic", "plastic_bottle"};

    // 각 분류 항목에 대한 정보를 담는 맵
    private Map<String, ClassificationInfo> classificationMap;

    /**
     * 각 분리수거 품목의 상세 정보(한글 이름, Firebase 문서 이름, 대표 이미지)를 담는 내부 클래스
     */
    private static class ClassificationInfo {
        final String koreanName; // 화면에 표시될 한글 이름 (예: "금속캔")
        final String firebaseDocName; // Firestore에서 문서를 찾을 때 사용할 이름 (예: "metal_can")
        @DrawableRes
        final int drawableId; // 해당 품목을 대표하는 이미지 리소스 ID (예: R.drawable.metal_recycle_img)

        ClassificationInfo(String koreanName, String firebaseDocName, @DrawableRes int drawableId) {
            this.koreanName = koreanName;
            this.firebaseDocName = firebaseDocName;
            this.drawableId = drawableId;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.photo_recognition_result);

        // --- 초기화 작업 수행 ---
        initializeViews(); // UI 컴포넌트 초기화
        initializeClassificationMap(); // 분리수거 품목 정보 맵 초기화
        db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 가져오기
        setupBottomNavigation(); // 하단 내비게이션 바 설정
        setupSystemBars(); // 시스템 바(상태 바) 스타일 설정
        loadPytorchModels(); // 백그라운드에서 PyTorch 모델 로딩 시작
        setupOptionsButton(); // '다른 품목 선택' 버튼 리스너 설정

        String imageUriString = getIntent().getStringExtra("imageUri");
        if (imageUriString == null) {
            Toast.makeText(this, "오류: 이미지 경로를 받지 못했습니다.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // --- 이미지 처리 및 분석 시작 ---
        Uri imageUri = Uri.parse(imageUriString);
        Glide.with(this).load(imageUri).into(resultImageView); // Glide를 이용해 이미지 표시
        progressBar.setVisibility(View.VISIBLE); // 로딩 바 표시

        // 별도 스레드에서 모델 로딩을 기다린 후 이미지 분석 실행
        executorService.execute(() -> {
            while (!isModelLoaded.get()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            runAnalysis(imageUri);
        });

        // 시스템 인셋(상태 바 등)에 따라 UI 패딩 조정
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }
    }

    /**
     * XML 레이아웃의 UI 컴포넌트들을 찾아와 변수에 할당
     */
    private void initializeViews() {
        resultImageView = findViewById(R.id.resultImageView);
        titleTextView = findViewById(R.id.titleTextView);
        resultTextView = findViewById(R.id.resultTextView);
        progressBar = findViewById(R.id.progressBar);
        recycling_baseline_list = findViewById(R.id.recycling_baseline_list);
    }

    /**
     * 분리수거 품목 정보를 담는 `classificationMap`을 초기화
     */
    private void initializeClassificationMap() {
        classificationMap = new HashMap<>();
        classificationMap.put("metal_can", new ClassificationInfo("금속캔", "metal_can", R.drawable.metal_recycle_img));
        classificationMap.put("plastic_bag", new ClassificationInfo("비닐", "plasticbag", R.drawable.plasticbag_recycle_img));
        classificationMap.put("styrofoam", new ClassificationInfo("스티로폼", "styrofoam", R.drawable.styrofoam_recycle_img));
        classificationMap.put("glass_bottle", new ClassificationInfo("유리병", "glass_bottle", R.drawable.glassbottle_recycle_img));
        classificationMap.put("paper", new ClassificationInfo("종이", "paper", R.drawable.paper_recycle_img));
        classificationMap.put("plastic", new ClassificationInfo("플라스틱", "plastic", R.drawable.plastic_recycle_img));
        classificationMap.put("plastic_bottle", new ClassificationInfo("페트병", "plastic_bottle", R.drawable.plastic_bottle_recycle_img));
        classificationMap.put("waste_phone", new ClassificationInfo("폐휴대폰", "waste_phone", R.drawable.waste_phone_img));
        classificationMap.put("waste_home_appliances", new ClassificationInfo("폐소형가전", "waste_home_appliances", R.drawable.waste_appliances_img));
        classificationMap.put("unused_medicines", new ClassificationInfo("폐의약품", "unused_medicines", R.drawable.waste_medicine_img));
        classificationMap.put("used_clothing", new ClassificationInfo("의류", "used_clothing", R.drawable.clothing_img));
        classificationMap.put("waste_battery", new ClassificationInfo("폐건전지", "waste_battery", R.drawable.waste_battery_recycle_img));
        classificationMap.put("waste_fluorescent_lamp", new ClassificationInfo("폐형광등", "waste_fluorescent_lamp", R.drawable.waste_fluorescent_lamp_recycle_img));
    }

    /**
     * '다른 품목 선택' 버튼에 클릭 리스너를 설정
     */
    private void setupOptionsButton() {
        recycling_baseline_list.setOnClickListener(v -> showOptionsDialog());
    }

    /**
     * 분리수거 품목 목록을 보여주는 다이얼로그를 생성하고 표시
     */
    private void showOptionsDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_selection_list_recycle, null);

        // 원하는 순서대로 품목 이름 배열을 수정
        String[] customOrder = {
                "페트병",
                "플라스틱",
                "비닐",
                "스티로폼",
                "종이",
                "유리병",
                "금속캔",
                "의류",
                "폐건전지",
                "폐형광등",
                "폐휴대폰",
                "폐소형가전",
                "폐의약품"
        };
        // 배열을 리스트로 변환
        List<String> recyclingTypes = java.util.Arrays.asList(customOrder);

        // 리스트뷰에 어댑터를 설정하여 품목 목록을 표시
        ListView recyclingListView = dialogView.findViewById(R.id.recycling_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dialog_selection_list_style, R.id.recycling_type_text, recyclingTypes);
        recyclingListView.setAdapter(adapter);

        // 다이얼로그 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialogTheme);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // 리스트의 항목을 클릭했을 때의 동작 설정
        recyclingListView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedType = recyclingTypes.get(position);
            updateClassificationResultFromList(selectedType);
            Toast.makeText(this, selectedType + "(으)로 변경되었습니다.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * 사용자가 목록에서 품목을 선택했을 때 UI를 업데이트
     * (Firebase에서 정보 조회)
     */
    private void updateClassificationResultFromList(String koreanClassification) {
        ClassificationInfo info = findInfoByKoreanName(koreanClassification);
        if (info == null) {
            Log.e("UpdateResult", "알 수 없는 분류입니다: " + koreanClassification);
            Toast.makeText(this, "분류 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Glide.with(this).load(info.drawableId).into(resultImageView);

        displayTitleAndImage(info);
        progressBar.setVisibility(View.VISIBLE);
        resultTextView.setText("'" + info.koreanName + "'에 대한 상세 정보를 조회합니다.");
        getRecyclingDataFromFirebaseOnly(info);
    }

    /**
     * 사진 분석 후 결과를 바탕으로 UI를 업데이트
     * (Gemini API로 정보 조회)
     */
    private void updateClassificationResultFromPhoto(String koreanClassification) {
        ClassificationInfo info = findInfoByKoreanName(koreanClassification);
        if (info == null) {
            Log.e("UpdateResult", "알 수 없는 분류입니다: " + koreanClassification);
            Toast.makeText(this, "분류 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        Glide.with(this).load(info.drawableId).into(resultImageView); // 대표 이미지로 변경
        displayTitleAndImage(info); // 제목 변경
        progressBar.setVisibility(View.VISIBLE);
        resultTextView.setText("'" + info.koreanName + "'에 대한 상세 정보를 조회합니다.");
        
        askGemini(info.koreanName);
    }

    /**
     * Firestore에서 분리수거 정보를 가져와 화면에 표시
     */
    private void getRecyclingDataFromFirebaseOnly(ClassificationInfo info) {
        db.collection("Separate_recycling")
                .document(info.firebaseDocName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        displayDataFromSnapshot(documentSnapshot);
                    } else {
                        Log.d("Firebase", "문서가 존재하지 않음: " + info.firebaseDocName);
                        resultTextView.setText("해당 품목에 대한 분리수거 정보가 아직 준비되지 않았습니다.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "데이터 가져오기 실패", e);
                    progressBar.setVisibility(View.GONE);
                    resultTextView.setText("데이터를 가져오는 중 오류가 발생했습니다. 네트워크 상태를 확인해주세요.");
                });
    }

    /**
     * Gemini API를 호출하여 분리수거 정보를 문의하고 결과를 화면에 표시
     */
    private void askGemini(String koreanClassification) {
        // 캐시에 결과가 있으면 API를 호출하지 않고 캐시된 결과를 사용
        if (geminiCache.containsKey(koreanClassification)) {
            Log.d("GeminiCache", "캐시된 결과 사용: " + koreanClassification); // 앱 캐시 저장
            String cachedResult = geminiCache.get(koreanClassification);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                resultTextView.setText(cachedResult);
            });
            return;
        }

        // Gemini API 서비스 호출
        Log.d("GeminiAPI", "캐시 없음. Gemini API 호출: " + koreanClassification);
        GeminiApiService.getInstance().getRecyclingInfo(koreanClassification, new GeminiApiService.ApiCallback() {
            @Override
            public void onSuccess(String resultText) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    geminiCache.put(koreanClassification, resultText);
                    Log.d("BackendAPI", "새로운 결과 캐싱 및 사용: " + koreanClassification);
                    resultTextView.setText(resultText);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("BackendAPI", "API 호출 실패: " + error);
                    resultTextView.setText("상세 정보를 가져오는 데 실패했습니다. 네트워크 상태를 확인해주세요.");
                    Toast.makeText(Photo_Recognition.this, "서버에 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * Firestore DocumentSnapshot에서 데이터를 추출하여 TextView에 설정
     */
    private void displayDataFromSnapshot(DocumentSnapshot doc) {
        List<String> instructions = (List<String>) doc.get("instructions");
        String description = doc.getString("description");

        StringBuilder resultText = new StringBuilder();
        if (description != null && !description.isEmpty()) {
            resultText.append(description).append("\n\n");
        }
        if (instructions != null && !instructions.isEmpty()) {
            for (String instruction : instructions) {
                resultText.append("• ").append(instruction).append("\n\n");
            }
        }
        resultTextView.setText(resultText.length() > 0 ? resultText.toString().trim() : "분리수거 정보가 없습니다.");
    }

    /**
     * "분리수거 종류: OOO" 형태의 제목을 아이콘과 함께 설정
     */
    private void displayTitleAndImage(ClassificationInfo info) {
        SpannableStringBuilder titleTextBuilder = new SpannableStringBuilder();
        Drawable drawable = getResources().getDrawable(R.drawable.recyclecontainer, getTheme());
        int size = (int) (titleTextView.getTextSize() * 1.2);
        drawable.setBounds(0, 0, size, size);
        ImageSpan imageSpan = new ImageSpan(drawable, ImageSpan.ALIGN_BOTTOM);
        titleTextBuilder.append(" ", imageSpan, 0)
                .append(" ")
                .append("분리수거 종류 : ")
                .append(info.koreanName);
        titleTextView.setText(titleTextBuilder);
    }

    /**
     * 이미지 URI를 받아 비트맵으로 변환하고, PyTorch 모델 추론과 Gemini API 요청을 동시에 진행합니다.
     */
    private void runAnalysis(Uri localUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), localUri);

            String englishLabel = runEnsembleInference(bitmap);
            if (englishLabel == null) {
                throw new RuntimeException("PyTorch 모델 분석에 실패했습니다.");
            }

            ClassificationInfo info = classificationMap.get(englishLabel);
            if (info == null) {
                throw new RuntimeException("인식된 라벨에 대한 정보가 없습니다: " + englishLabel);
            }

            // 1. PyTorch 인식 결과가 나오자마자 즉시 Gemini API 요청을 백그라운드에서 시작
            askGemini(info.koreanName);

            // 2. 동시에 UI 스레드에서는 인식 결과와 대표 이미지를 먼저 보여줌
            runOnUiThread(() -> {
                progressBar.setVisibility(View.VISIBLE); // API 응답을 기다리는 동안 로딩 바 유지
                resultTextView.setText("'" + info.koreanName + "'(으)로 인식되었습니다.\n상세 정보를 조회합니다.");
                displayTitleAndImage(info);
                Glide.with(this).load(info.drawableId).into(resultImageView); // 대표 이미지로 교체
            });

        } catch (Exception e) {
            Log.e("PhotoRecognition", "분석 중 오류 발생", e);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                resultTextView.setText("오류가 발생했습니다: " + e.getMessage());
            });
        }
    }

    /**
     * 비트맵 이미지를 받아 PyTorch 모델로 추론을 실행하고 가장 확률이 높은 클래스 라벨을 반환
     */
    private String runEnsembleInference(Bitmap bitmap) {
        // 이미지를 모델 입력 크기(512x512)로 조절하고 텐서로 변환
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 512, 512, true);
        final Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB, TensorImageUtils.TORCHVISION_NORM_STD_RGB);

        float[] aggregatedProbabilities = new float[classLabelsEng.length];

        // 로드된 모든 모델에 대해 추론을 실행하고 결과(확률)를 합산 (앙상블)
        for (Module module : pytorchModules) {
            final Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
            final float[] logits = outputTensor.getDataAsFloatArray();
            final float[] probabilities = softmax(logits);

            for (int i = 0; i < probabilities.length; i++) {
                aggregatedProbabilities[i] += probabilities[i];
            }
        }

        // 합산된 확률 중 가장 높은 값을 가진 클래스의 인덱스를 찾음
        float maxProbability = -Float.MAX_VALUE;
        int maxProbIdx = -1;
        for (int i = 0; i < aggregatedProbabilities.length; i++) {
            if (aggregatedProbabilities[i] > maxProbability) {
                maxProbability = aggregatedProbabilities[i];
                maxProbIdx = i;
            }
        }

        // 해당 인덱스의 영어 라벨을 반환
        return (maxProbIdx != -1) ? classLabelsEng[maxProbIdx] : null;
    }

    /**
     * 한글 품목 이름을 받아 해당하는 ClassificationInfo 객체를 찾아서 반환
     */
    private ClassificationInfo findInfoByKoreanName(String koreanName) {
        for (Map.Entry<String, ClassificationInfo> entry : classificationMap.entrySet()) {
            if (Objects.equals(entry.getValue().koreanName, koreanName)) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * assets 폴더에서 모델 파일을 읽어와 내부 저장소에 복사하고, 모델을 로드
     */
    private void loadPytorchModels() {
        executorService.execute(() -> {
            try {
                for (String modelFile : modelFiles) {
                    pytorchModules.add(LiteModuleLoader.load(assetFilePath(modelFile)));
                }
                isModelLoaded.set(true);
                Log.d("PhotoRecognition", "PyTorch 모델 로딩 완료");
            } catch (IOException e) {
                Log.e("PhotoRecognition", "모델 로딩 실패", e);
                runOnUiThread(() -> Toast.makeText(this, "모델 로딩 실패", Toast.LENGTH_LONG).show());
            }
        });
    }

    /**
     * 상태 표시줄 아이콘 색상을 밝은 배경에 맞게 설정
     */
    private void setupSystemBars() {
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }
    }

    /**
     * 모델의 출력(logits)을 확률 분포로 변환하는 Softmax 함수
     */
    private float[] softmax(float[] logits) {
        float[] probabilities = new float[logits.length];
        float maxLogit = -Float.MAX_VALUE;
        for (float logit : logits) {
            if (logit > maxLogit) {
                maxLogit = logit;
            }
        }

        float sumExp = 0.0f;
        for (int i = 0; i < logits.length; i++) {
            probabilities[i] = (float) Math.exp(logits[i] - maxLogit);
            sumExp += probabilities[i];
        }

        for (int i = 0; i < logits.length; i++) {
            probabilities[i] /= sumExp;
        }
        return probabilities;
    }

    /**
     * assets 폴더에 있는 파일의 경로를 가져오는 헬퍼 메서드
     */
    public String assetFilePath(String assetName) throws IOException {
        File file = new File(getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }
        try (InputStream is = getAssets().open(assetName); OutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }
        return file.getAbsolutePath();
    }

    /**
     * 액티비티가 파괴될 때 스레드 풀을 종료
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    /**
     * 하단 내비게이션 바의 아이콘들에 대한 클릭 이벤트 처리
     */
    private void setupBottomNavigation() {
        ImageButton homeIcon = findViewById(R.id.home_icon);
        ImageButton mapIcon = findViewById(R.id.map_icon);
        ImageButton cameraIcon = findViewById(R.id.camera_icon);
        ImageButton messageIcon = findViewById(R.id.message_icon);
        ImageButton accountIcon = findViewById(R.id.account_icon);

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Photo_Recognition.this, MainscreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        mapIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Photo_Recognition.this, LocationActivity.class);
            startActivity(intent);
        });

        cameraIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Photo_Recognition.this, CameraActivity.class);
            startActivity(intent);
        });

        homeIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Photo_Recognition.this, CommunityActivity.class);
            startActivity(intent);
        });

        accountIcon.setOnClickListener(v -> {
            Intent intent = new Intent(Photo_Recognition.this, MypageActivity.class);
            startActivity(intent);
        });
    }
}