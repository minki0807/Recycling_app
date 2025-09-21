package com.example.recycling_app.Profile.profieedit;

import android.Manifest; // 안드로이드 권한 관련 클래스
import android.content.Intent; // 다른 액티비티를 시작하거나 데이터 전달 시 사용
import android.content.pm.PackageManager; // 패키지 관리자 (권한 확인 등)
import android.database.Cursor; // 데이터베이스 쿼리 결과에 접근
import android.net.Uri; // 리소스 식별자
import android.os.Build; // 빌드 버전 정보
import android.os.Bundle; // 액티비티 상태 저장/복원 시 사용
import android.provider.MediaStore; // 미디어 콘텐츠(이미지, 비디오 등)에 접근

import android.view.MenuItem; // 툴바 메뉴 아이템

import android.widget.Toast; // 짧은 메시지 팝업

import androidx.annotation.NonNull; // null이 아님을 명시하는 어노테이션
import androidx.appcompat.app.AppCompatActivity; // Android 기본 Activity 클래스
import androidx.appcompat.widget.Toolbar; // 앱 바 (액션 바 대체)
import androidx.core.app.ActivityCompat; // 런타임 권한 요청 지원
import androidx.core.content.ContextCompat; // 권한 상태 확인 지원
import androidx.recyclerview.widget.GridLayoutManager; // RecyclerView 항목을 그리드 형태로 배열
import androidx.recyclerview.widget.RecyclerView; // 스크롤 가능한 대량의 항목을 효율적으로 표시

import com.example.recycling_app.R;

import java.util.ArrayList; // 동적 배열 리스트
import java.util.List; // 리스트 컬렉션

// 사용자 정의 갤러리 기능을 제공하는 액티비티
// 기기 내 이미지들을 불러와 그리드 형태로 보여주고, 선택된 이미지의 URI를 반환
public class CustomGalleryActivity extends AppCompatActivity {

    // 외부 저장소 읽기 권한 요청 코드
    private static final int REQUEST_READ_EXTERNAL_STORAGE = 1;
    private RecyclerView recyclerView; // 이미지들을 표시할 RecyclerView
    private GalleryAdapter galleryAdapter; // RecyclerView에 데이터를 연결할 어댑터
    private List<Uri> imageUris; // 로드된 이미지들의 URI를 담을 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_gallery); // 레이아웃 파일을 이 액티비티에 연결

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar); // 레이아웃에서 툴바 찾기
        setSupportActionBar(toolbar); // 툴바를 액션바로 설정
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바에 뒤로가기 버튼 활성화
            getSupportActionBar().setTitle("갤러리"); // 툴바 제목 설정
        }

        recyclerView = findViewById(R.id.gallery_recycler_view); // 갤러리 RecyclerView 찾기
        // RecyclerView 레이아웃 매니저 설정: 3열의 그리드 형태로 이미지 표시
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imageUris = new ArrayList<>(); // 이미지 URI 리스트 초기화
        // 갤러리 어댑터 초기화: 이미지 리스트와 클릭 리스너 전달
        galleryAdapter = new GalleryAdapter(this, imageUris, new GalleryAdapter.OnImageClickListener() {
            @Override
            // 이미지가 클릭되었을 때 호출되는 콜백 메서드
            public void onImageClick(Uri imageUri) {
                // 선택된 이미지의 URI를 Intent에 담아 결과로 반환
                Intent resultIntent = new Intent();
                resultIntent.setData(imageUri); // URI를 데이터로 설정
                setResult(RESULT_OK, resultIntent); // 결과 코드와 Intent 설정
                finish(); // 현재 액티비티 종료
            }
        });
        recyclerView.setAdapter(galleryAdapter); // RecyclerView에 어댑터 설정

        // 앱 시작 시 권한 확인 및 요청
        checkPermissions();
    }

    // 툴바의 메뉴 아이템 클릭 처리 (여기서는 뒤로가기 버튼만 해당)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 클릭된 아이템의 ID가 홈 버튼(뒤로가기)인지 확인
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // 액티비티의 뒤로가기 동작 수행
            return true; // 이벤트 처리 완료
        }
        return super.onOptionsItemSelected(item); // 그 외의 아이템은 부모 클래스에 위임
    }

    // 외부 저장소 읽기 권한을 확인하고 요청하는 메서드
    private void checkPermissions() {
        // Android 13 (API 33) 이상 버전인지 확인
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // READ_MEDIA_IMAGES 권한이 부여되었는지 확인
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없으면 사용자에게 권한 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // 권한이 이미 있다면 이미지 로드
                loadImages();
            }
        } else {
            // Android 12 (API 32) 이하 버전
            // READ_EXTERNAL_STORAGE 권한이 부여되었는지 확인
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없으면 사용자에게 권한 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                // 권한이 이미 있다면 이미지 로드
                loadImages();
            }
        }
    }

    // 권한 요청 결과 처리 메서드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 요청 코드가 외부 저장소 읽기 권한 요청 코드와 일치하는지 확인
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            // 권한이 부여되었는지 확인 (grantResults가 비어있지 않고 첫 번째 결과가 PERMISSION_GRANTED인지)
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImages(); // 권한이 부여되면 이미지 로드
            } else {
                // 권한이 거부되면 사용자에게 메시지 표시 후 액티비티 종료
                Toast.makeText(this, "갤러리 접근 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                finish(); // 현재 액티비티 종료
            }
        }
    }

    // 기기에서 이미지를 로드하는 메서드
    private void loadImages() {
        imageUris.clear(); // 기존 이미지 URI 리스트 초기화

        Uri collection; // 이미지 컬렉션의 URI
        // Android Q (API 29) 이상에서는 MediaStore.VOLUME_EXTERNAL을 사용하여 콘텐츠 URI 가져옴
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            // Android Q 미만에서는 MediaStore.Images.Media.EXTERNAL_CONTENT_URI 사용
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        // 쿼리할 이미지 정보 컬럼 (ID, 이름, 파일 경로)
        String[] projection = new String[]{
                MediaStore.Images.Media._ID, // 이미지 고유 ID
                MediaStore.Images.Media.DISPLAY_NAME, // 이미지 파일명
                MediaStore.Images.Media.DATA // 이미지 파일 경로 (Android Q 미만에서 주로 사용)
        };
        // 이미지 정렬 순서: 최신 추가된 사진부터 내림차순 정렬
        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        // getContentResolver().query를 사용하여 이미지 데이터 쿼리
        try (Cursor cursor = getContentResolver().query(
                collection, // 쿼리할 URI
                projection, // 가져올 컬럼
                null,       // selection (WHERE 절, null이면 모든 행)
                null,       // selectionArgs (selection의 플레이스홀더 값)
                sortOrder   // 정렬 순서
        )) {
            // 커서가 유효하면
            if (cursor != null) {
                // _ID 컬럼의 인덱스 가져오기 (컬럼이 없을 경우 예외 발생)
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                // 커서를 다음 행으로 이동하며 이미지 데이터 처리
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idColumn); // 이미지 ID 가져오기
                    // 이미지 ID를 사용하여 실제 이미지의 content URI 생성
                    Uri contentUri = Uri.withAppendedPath(
                            collection,
                            String.valueOf(id)
                    );
                    imageUris.add(contentUri); // 생성된 URI를 리스트에 추가
                }
                galleryAdapter.notifyDataSetChanged(); // 어댑터에 데이터 변경을 알려 RecyclerView 업데이트
            }
        } catch (Exception e) {
            // 이미지 로드 중 오류 발생 시 토스트 메시지 표시 및 스택 트레이스 출력
            Toast.makeText(this, "이미지 로드 중 오류 발생: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}