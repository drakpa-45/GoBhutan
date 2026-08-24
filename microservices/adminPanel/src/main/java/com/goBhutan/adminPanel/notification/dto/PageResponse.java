package com.goBhutan.adminPanel.notification.dto;

import lombok.*;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private String nextCursor;
    private int limit;
}
