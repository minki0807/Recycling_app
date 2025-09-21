package com.example.recycling_app.Location;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recycling_app.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.KakaoMapSdk;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.MapView;
import com.kakao.vectormap.camera.CameraAnimation;
import com.kakao.vectormap.camera.CameraUpdateFactory;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelLayerOptions;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.route.RouteLine;
import com.kakao.vectormap.route.RouteLineManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LocationActivity extends AppCompatActivity implements LocationAccess.PermissionListener {

    private MapView mapView;    // 지도 화면 (findViewById)
    private KakaoMap kakaoMap;  // 카메라 이동, 마커조작 등
    private Label currentLocationLabel; // 마커(라벨)
    private boolean isInitialLocationSet = false; // 앱 실행 후 내 위치 떳는지 확인
    private LocationAccess locationAccess;  // 위치 권한 체크
    private SearchView searchView;  // 주소 검색창
    private RecyclerView recyclerView;  // 검색 결과 목록
    private LocationAdapter locationAdapter;    //RecyclerView 데이터 연결

    // --- 데이터 관련 변수 (서버에서 받아온 데이터를 저장) ---
    private final List<LocationData> allLocations = new ArrayList<>();
    private CardView addressBox; // CardView 변수 선언
    private TextView markerAddressTextView;
    private IconsManager iconsManager;
    private FirebaseFirestore db;
    private fetchDataFromFirestore dataFetcher;

    private LabelLayer currentLocationLayer;

    private RouteLineManager routeLineManager;
    private RouteLine currentRouteLine;
    private LocationData selectedLocationData;
    private ImageButton filterButton;
    private TextView markerTypeTextView;
    private final Set<String> activeFilters = new HashSet<>(Arrays.asList("clothes", "recycle", "phone", "homemachine", "pill", "battery"));

    private static final double SEARCH_RADIUS_METERS = 2000;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // SDK 초기화
        FirebaseApp.initializeApp(this);
        KakaoMapSdk.init(this, "f74b1223cc505a613aeb568c3277ff52");
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.location_main);


        db = FirebaseFirestore.getInstance();

        // xml 연결
        mapView = findViewById(R.id.map_view);
        searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.recycler_view);
        markerAddressTextView = findViewById(R.id.tv_marker_address); // TextView 초기화

        addressBox = findViewById(R.id.address_box); // xml에 정의된 ID로 가정
        markerTypeTextView = findViewById(R.id.tv_marker_type);
        filterButton = findViewById(R.id.filter_button);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        filterButton.setOnClickListener(v -> showFilterDialog());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (view, insets) -> {
            // 시스템 바(상태 바, 내비게이션 바)의 크기를 가져옵니다.
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

            // 검색창의 상단 마진을 상태 바 높이만큼 추가합니다.
            // 기존 마진 값(16dp)도 유지하기 위해 더해줍니다.
            if (searchView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) searchView.getLayoutParams();
                params.topMargin = topInset + (int) (16 * getResources().getDisplayMetrics().density); // 16dp를 px로 변환하여 더함
                searchView.setLayoutParams(params);
            }

            // 하단 주소창의 하단 마진을 내비게이션 바 높이만큼 추가합니다.
            // currentLocationButton은 address_box에 연관되어 있어 자동으로 함께 이동합니다.
            if (addressBox.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) addressBox.getLayoutParams();
                params.bottomMargin = bottomInset + (int) (16 * getResources().getDisplayMetrics().density); // 16dp를 px로 변환하여 더함
                addressBox.setLayoutParams(params);
            }

            // 내 위치 버튼은 address_box가 보이지 않을 때를 대비하여 별도로 마진을 줍니다.
            if (findViewById(R.id.currentLocationButton).getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) findViewById(R.id.currentLocationButton).getLayoutParams();
                params.bottomMargin = bottomInset + (int) (16 * getResources().getDisplayMetrics().density); // 16dp를 px로 변환하여 더함
                findViewById(R.id.currentLocationButton).setLayoutParams(params);
            }

            // 상단바 아이콘과 글씨 색상을 어둡게 설정 (Light Mode)
            WindowInsetsControllerCompat windowInsetsController =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.setAppearanceLightStatusBars(true);
            }
            return WindowInsetsCompat.CONSUMED; // Insets을 소비했음을 시스템에 알립니다.
        });

        // 위치 권한 확인
        locationAccess = new LocationAccess(this, this);
        locationAccess.checkLocationPermission();
    }

    @Override
    public void LocationOnPermissionGranted() {
        showInitialWarningDialog();
    }

    @Override
    public void LocationOnPermissionDenied() {
        Log.d("Permission", "위치 정보 권한이 거부되었습니다.");
    }

    // 시스템 콜백, LocationAccess에 전달
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationAccess.handleRequestPermissionsResult(requestCode, grantResults);
    }

    private void showInitialWarningDialog() {
        // 1. "app_prefs"라는 이름의 저장소에서 데이터를 불러옵니다.
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        // "has_seen_warning" 이라는 키의 값을 읽어옵니다. 저장된 값이 없으면 기본값 false를 사용합니다.
        boolean hasSeenWarning = prefs.getBoolean("has_seen_warning", false);

        // 2. 만약 이전에 경고창을 본 적이 있다면(값이 true라면),
        if (hasSeenWarning) {
            startMap(); // 바로 지도를 시작하고 메서드를 종료합니다.
            return;
        }

        // --- 이 아래 코드는 최초 실행 시에만 실행됩니다 ---
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.warning_dialog, null);
        Button confirmButton = dialogView.findViewById(R.id.info_close);

        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        confirmButton.setOnClickListener(v -> {
            dialog.dismiss();

            // 3. '확인' 버튼을 누르면, "has_seen_warning" 값을 true로 저장합니다.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("has_seen_warning", true);
            editor.apply(); // 저장을 완료합니다.

            // 이제 평소처럼 지도를 시작합니다.
            startMap();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        dialog.show();
    }

    // 지도 시작 (초기화)
    private void startMap() {
        Toast.makeText(this, "위치 권한이 승인되었습니다.", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "수거함 위치를 불러오는 중입니다...", Toast.LENGTH_SHORT).show();
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
            }   // 파괴

            @Override
            public void onMapError(Exception error) {
                Log.e("KakaoMap", "지도 초기화 오류: ", error);
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(KakaoMap map) {
                kakaoMap = map;
                // *** RouteLineManager 초기화 ***
                routeLineManager = kakaoMap.getRouteLineManager();
                currentLocationLayer = kakaoMap.getLabelManager().addLayer(LabelLayerOptions.from("currentLocationLayer"));

                iconsManager = new IconsManager(kakaoMap, addressBox, markerAddressTextView);

                iconsManager.setOnMarkerClickListener(locations-> {
                    updateAddressBox(locations);
                });

                dataFetcher = new fetchDataFromFirestore();
                if (allLocations.isEmpty()) {
                    dataFetcher.fetchAllLocations(new fetchDataFromFirestore.OnDataReadyCallback() {
                        @Override
                        public void onDataReady(List<LocationData> locations) {

                            // 데이터 로딩 성공!
                            // 받아온 데이터를 MainActivity의 allLocations 리스트에 저장합니다.
                            LocationActivity.this.allLocations.clear();
                            LocationActivity.this.allLocations.addAll(locations);

                            Log.d("MainActivity", "Firestore로부터 " + locations.size() + "개의 데이터를 성공적으로 로드했습니다.");
                            Toast.makeText(LocationActivity.this, "모든 수거함 정보를 불러왔습니다.", Toast.LENGTH_SHORT).show();

                            // 이제 데이터가 준비되었으니, 현재 지도 중심을 기준으로 마커를 표시합니다.
                            // 지도가 준비된 후 첫 마커 표시는 여기서 이루어집니다.
                            if (kakaoMap != null) {
                                filterAndDisplayMarkers(kakaoMap.getCameraPosition().getPosition());
                            }
                        }


                        @Override
                        public void onDataFetchFailed(String errorMessage) {
                            // 데이터 로딩 실패!
                            Log.e("MainActivity", "데이터 로드 실패: " + errorMessage);
                            Toast.makeText(LocationActivity.this, "데이터를 불러오는 데 실패했습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                kakaoMap.setOnCameraMoveEndListener((kakaoMap1, cameraPosition, gestureType) -> {
                    // 지도 이동이 완료된 시점의 지도 중심 좌표를 가져옵니다.
                    LatLng mapCenter = cameraPosition.getPosition();
                    Log.d("CameraMove", "지도 이동 완료. 새 중심: " + mapCenter.toString());
                    // 새로운 중심 좌표로 마커를 필터링하고 표시합니다.
                    filterAndDisplayMarkers(mapCenter);
                });

                setupMyLocationButton();
                setupRecyclerView();
                setupSearchView();
                startLocationUpdates();
                setupMapClickListener();
            }
        });
    }

    private String getTypeDisplayName(String type) {
        if (type == null) return "알 수 없는 장소";

        switch (type) {
            case "clothes":
                return "의류 수거함";
            case "recycle":
                return "재활용 수거함";
            case "phone":
                return "폐휴대폰";
            case "homemachine":
                return "소형가전";
            case "pill":
                return "폐의약품";
            case "battery":
                return "폐건전지/폐형광등";
            default:
                return type;
        }
    }

    private void setupMapClickListener() {
        if (kakaoMap == null) return;

        kakaoMap.setOnMapClickListener((kakaoMap, latLng, pointF, poi) -> {
            // 지도의 빈 공간을 클릭했을 때, 주소 박스가 보이고 있다면 숨깁니다.
            if (addressBox.getVisibility() == View.VISIBLE) {
                addressBox.setVisibility(View.GONE);
                selectedLocationData = null;

                if (currentRouteLine != null) {
                    routeLineManager.remove(currentRouteLine);
                    currentRouteLine = null;
                }
            }
        });
    }

    // 위치 실시간 업데이트
    private void startLocationUpdates() {
        // 위치 권한 확인
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("Location", "위치 권한이 없어 업데이트를 시작할 수 없습니다.");
            return;
        }

        // 위치 요청 설정
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000)
                .setMinUpdateIntervalMillis(1000) // 최소 업데이트 간격
                .setMinUpdateDistanceMeters(5)     // 최소 업데이트 거리
                .build();

        // 위치 콜백 정의
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // onLocationChanged 메서드를 직접 호출하여 위치 처리
                        onLocationChanged(location);
                    }
                }
            }
        };

        // 위치 업데이트 요청
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        Log.d("Location", "FusedLocationProvider를 통해 위치 업데이트를 시작합니다.");
    }



    // 위치 바뀔 때마다 콜백
    @Override
    public void onLocationChanged(@NonNull Location location) {
        if (kakaoMap == null) return;
        LatLng currentPosition = LatLng.from(location.getLatitude(), location.getLongitude());
        if (currentLocationLabel == null) {
            currentLocationLabel = currentLocationLayer.addLabel(LabelOptions.from(currentPosition).setStyles(R.drawable.current_location));
        } else {
            currentLocationLabel.moveTo(currentPosition);
        }

        if (!isInitialLocationSet) {
            kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentPosition, 15));
            isInitialLocationSet = true;
        }
    }

    private void showFilterDialog() {
        // 1. 레이아웃 파일을 View 객체로 Inflate
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_filter, null);

        // 2. 각 체크박스를 찾아서 현재 필터 상태에 따라 체크 여부 설정
        CheckBox recycleBox = dialogView.findViewById(R.id.recycle_box);
        CheckBox clothesBox = dialogView.findViewById(R.id.clothes_box);
        CheckBox phoneBox = dialogView.findViewById(R.id.phone_box);
        CheckBox homemachineBox = dialogView.findViewById(R.id.homemachine_box);
        CheckBox pillBox = dialogView.findViewById(R.id.pill_box);
        CheckBox batteryBox = dialogView.findViewById(R.id.battery_box);

        recycleBox.setChecked(activeFilters.contains("recycle"));
        clothesBox.setChecked(activeFilters.contains("clothes"));
        phoneBox.setChecked(activeFilters.contains("phone"));
        homemachineBox.setChecked(activeFilters.contains("homemachine"));
        pillBox.setChecked(activeFilters.contains("pill"));
        batteryBox.setChecked(activeFilters.contains("battery"));

        // 3. AlertDialog 생성
        new AlertDialog.Builder(this)
                .setTitle("표시할 종류 선택")
                .setView(dialogView)
                .setPositiveButton("확인", (dialog, which) -> {
                    // '확인' 버튼을 누르면, 새로운 필터 목록을 만듭니다.
                    activeFilters.clear();
                    if (recycleBox.isChecked()) activeFilters.add("recycle");
                    if (clothesBox.isChecked()) activeFilters.add("clothes");
                    if (phoneBox.isChecked()) activeFilters.add("phone");
                    if (homemachineBox.isChecked()) activeFilters.add("homemachine");
                    if (pillBox.isChecked()) activeFilters.add("pill");
                    if (batteryBox.isChecked()) activeFilters.add("battery");

                    // 새로운 필터를 적용하여 마커를 다시 표시합니다.
                    filterAndDisplayMarkers(kakaoMap.getCameraPosition().getPosition());
                    Toast.makeText(this, "필터가 적용되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("취소", null)
                .show();
    }


    private void filterAndDisplayMarkers(LatLng currentPosition) {
        if (iconsManager == null || allLocations.isEmpty()) {
            return; // 아직 준비되지 않았다면 실행하지 않음
        }

        List<LocationData> filteredLocations = new ArrayList<>();
        float[] results = new float[1];

        for (LocationData data : allLocations) {
            // 1. 타입(종류) 필터링: 현재 활성화된 필터에 포함된 종류의 데이터만 다음 단계로
            if (activeFilters.contains(data.type)) {

                // 2. 거리 필터링: 기존 로직 그대로 사용
                Location.distanceBetween(
                        currentPosition.latitude, currentPosition.longitude,
                        data.latitude, data.longitude,
                        results);
                float distanceInMeters = results[0];

                if (distanceInMeters <= SEARCH_RADIUS_METERS) {
                    filteredLocations.add(data);
                }
            }
        }

        // 필터링된 목록으로 마커를 새로 추가 (IconsManager 내부에서 기존 마커는 clear됨)
        iconsManager.addMarkers(filteredLocations);
        Log.d("MarkerFilter", "현재 위치 주변 " + (int)SEARCH_RADIUS_METERS + "m 내에 " + filteredLocations.size() + "개의 마커를 표시합니다.");
    }


    // UI 설정
    private void setupRecyclerView() {
        locationAdapter = new LocationAdapter(new ArrayList<>());   // 초기화
        recyclerView.setLayoutManager(new LinearLayoutManager(this));   // 목록 설정
        recyclerView.setAdapter(locationAdapter);   // 어댑터 연결
    }


    // SearchView 설정
    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);   // 검색 실행
                hideKeyboard(); // 키보드 내림
                return true;
            }


            // 글자 칠 때마다 호출
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);  // 목록 숨김 (글씨 없음)
                } else {
                    performSearch(newText); // 실시간 검색 (글씨 있음)
                }
                return true;
            }
        });
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            return;
        }

        List<LocationData> results = new ArrayList<>();
        for (LocationData data : allLocations) {
            if (data.address.contains(query) && activeFilters.contains(data.type)) {
                results.add(data);
            }
        }

        if (results.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
        } else {
            locationAdapter.updateData(results);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }



    // M버튼 설정 (내 위치로 돌아오기)
    private void setupMyLocationButton() {
        Button myLocationButton = findViewById(R.id.currentLocationButton);
        myLocationButton.setOnClickListener(v -> {
            if (kakaoMap != null && currentLocationLabel != null && currentLocationLabel.getPosition() != null) {
                // 누르면 내 위치로 이동 (줌 레벨 17)
                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(currentLocationLabel.getPosition(), 17), CameraAnimation.from(500));
            } else {
                Toast.makeText(this, "아직 위치 정보를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // 키보드 숨김
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
                 mapView.resume();
             }
    }

    // 업뎃 중지
    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.pause();
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            Log.d("Location", "위치 업데이트가 중지되었습니다.");
        }
    }

    /**
     * 하단 주소 카드뷰의 내용을 업데이트하고 표시하는 전용 메서드
     * @param locations 해당 지점의 위치 데이터 리스트
     */
    private void updateAddressBox(List<LocationData> locations) {
        if (locations == null || locations.isEmpty()) {
            return;
        }

        // 대표 주소는 첫 번째 항목으로 설정
        LocationData firstLocation = locations.get(0);
        markerAddressTextView.setText(firstLocation.address);
        selectedLocationData = firstLocation; // 다른 기능에서 사용할 수 있도록 선택된 데이터 설정

        // 수거함 종류 텍스트 조합
        if (locations.size() > 1) {
            // 데이터가 여러 개일 경우, 종류를 " / "로 연결
            StringBuilder typeBuilder = new StringBuilder();
            for (int i = 0; i < locations.size(); i++) {
                typeBuilder.append(getTypeDisplayName(locations.get(i).type));
                if (i < locations.size() - 1) {
                    typeBuilder.append(", ");
                }
            }
            markerTypeTextView.setText(typeBuilder.toString());
        } else {
            // 데이터가 하나일 경우, 해당 종류만 표시
            markerTypeTextView.setText(getTypeDisplayName(firstLocation.type));
        }

        // 주소창 보이기
        addressBox.setVisibility(View.VISIBLE);
    }

    // RecyclerView 위치 표시
    class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.LocationViewHolder> {
        private List<LocationData> items;

        LocationAdapter(List<LocationData> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location, parent, false);
            return new LocationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
            LocationData item = items.get(position);
            holder.addressTextView.setText(item.address);

            holder.itemView.setOnClickListener(v -> {
                LatLng targetPosition = LatLng.from(item.latitude, item.longitude);
                kakaoMap.moveCamera(CameraUpdateFactory.newCenterPosition(targetPosition, 17), CameraAnimation.from(500));

                updateAddressBox(Arrays.asList(item));
                selectedLocationData = item;
                markerAddressTextView.setText(item.address);
                markerTypeTextView.setText(getTypeDisplayName(item.type)); // 새로 추가

                addressBox.setVisibility(View.VISIBLE);
                // UI 정리
                recyclerView.setVisibility(View.GONE);
                searchView.setQuery("", false);
                searchView.clearFocus();
                hideKeyboard();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        void updateData(List<LocationData> newItems) {
            this.items.clear();
            this.items.addAll(newItems);
            notifyDataSetChanged();
        }

        class LocationViewHolder extends RecyclerView.ViewHolder {
            TextView addressTextView;
            LocationViewHolder(View itemView) {
                super(itemView);
                addressTextView = itemView.findViewById(R.id.tv_address);
            }
        }
    }
}
