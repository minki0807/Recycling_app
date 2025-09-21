package com.example.recycling_app.Howtobox;

import java.util.List;

// '시/군/구'의 행정 구역 정보를 담는 클래스입니다.
public class Sigungu {
    public final String name;
    public final List<String> dongeupmyeonList;

    public Sigungu(String name, List<String> dongeupmyeonList) {
        this.name = name;
        this.dongeupmyeonList = dongeupmyeonList;
    }
}