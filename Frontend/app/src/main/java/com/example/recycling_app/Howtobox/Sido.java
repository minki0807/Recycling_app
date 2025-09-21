package com.example.recycling_app.Howtobox;

import java.util.List;

// '시' 또는 '도'의 행정 구역 정보를 담는 클래스입니다.
public class Sido {
    public final String name;
    public final List<Sigungu> sigunguList;

    public Sido(String name, List<Sigungu> sigunguList) {
        this.name = name;
        this.sigunguList = sigunguList;
    }
}