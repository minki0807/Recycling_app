package com.example.recycling_app.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Image {
    private String imageId;
    private String postId;
    private String url;
    private Data uploadedAt;
}
