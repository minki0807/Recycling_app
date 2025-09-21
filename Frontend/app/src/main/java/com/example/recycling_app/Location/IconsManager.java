package com.example.recycling_app.Location;

import android.widget.TextView;

import com.kakao.sdk.common.util.Utility;
import androidx.cardview.widget.CardView;
import com.example.recycling_app.R;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelLayerOptions;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 지도 위의 데이터 마커(라벨)를 관리하는 클래스.
 */
public class IconsManager {
    private final KakaoMap kakaoMap;
    private final CardView addressBox;
    private final TextView markerAddressTextView;
    private final Map<com.kakao.vectormap.label.Label, LocationData> markerDataMap = new HashMap<>();

    // *** 데이터 마커들을 위한 전용 레이어 ***
    private final LabelLayer dataMarkerLayer;
    private final List<LocationData> currentLocations = new ArrayList<>();

    public interface OnMarkerClickListener {
        void onMarkerClicked(List<LocationData> locations);
    }
    private OnMarkerClickListener markerClickListener;

    public IconsManager(KakaoMap kakaoMap, CardView addressBox, TextView markerAddressTextView) {
        this.kakaoMap = kakaoMap;
        this.addressBox = addressBox;
        this.markerAddressTextView = markerAddressTextView;
        // *** 데이터 마커 전용 레이어를 생성합니다. ***
        this.dataMarkerLayer = kakaoMap.getLabelManager().addLayer(LabelLayerOptions.from("dataMarkerLayer"));
        setupLabelClickListener();
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.markerClickListener = listener;
    }

    /**
     * 데이터 마커 레이어에 있는 모든 마커를 제거합니다.
     */
    public void clearMarkers() {
        if (dataMarkerLayer != null) {
            dataMarkerLayer.removeAll();
        }
        markerDataMap.clear();
    }

    /**
     * LocationData 리스트를 받아와 전용 레이어에 마커를 추가합니다.
     */
    public void addMarkers(List<LocationData> locations) {
        if (dataMarkerLayer == null || locations == null) return;
        clearMarkers();
        this.currentLocations.clear();
        this.currentLocations.addAll(locations);

        for (LocationData data : locations) {
            int markerResourceId;
            if ("clothes".equals(data.type)) {
                markerResourceId = R.drawable.clothes_bin;
            } else if("recycle".equals(data.type)){
                markerResourceId = R.drawable.recycle_bin;
            }  else if("phone".equals(data.type)){
                markerResourceId = R.drawable.phone_bin;
            }  else if("homemachine".equals(data.type)){
                markerResourceId = R.drawable.homemachine_bin;
            }  else {
                markerResourceId = R.drawable.battery_bin;
            }

            LatLng position = LatLng.from(data.latitude, data.longitude);
            LabelOptions options = LabelOptions.from(position)
                    .setStyles(LabelStyles.from(LabelStyle.from(markerResourceId).setZoomLevel(13)));


            // *** 전용 레이어에 마커를 추가합니다. ***
            Label addedLabel = dataMarkerLayer.addLabel(options);
            markerDataMap.put(addedLabel, data);
        }
    }

    private void setupLabelClickListener() {
        if (kakaoMap == null) return;
        kakaoMap.setOnLabelClickListener((kakaoMap, labelLayer, label) -> {
            if (labelLayer.equals(dataMarkerLayer) && markerClickListener != null) {
                LatLng clickedPosition = label.getPosition();
                List<LocationData> locationsAtPoint = new ArrayList<>();

                // 저장된 전체 데이터에서 동일한 좌표를 가진 모든 항목을 찾음
                for (LocationData data : currentLocations) {
                    if (data.getLatitude() == clickedPosition.latitude && data.getLongitude() == clickedPosition.longitude) {
                        locationsAtPoint.add(data);
                    }
                }

                // 찾은 데이터 리스트를 리스너를 통해 전달
                if (!locationsAtPoint.isEmpty()) {
                    markerClickListener.onMarkerClicked(locationsAtPoint);
                }
            }
            return true;
        });
    }
}