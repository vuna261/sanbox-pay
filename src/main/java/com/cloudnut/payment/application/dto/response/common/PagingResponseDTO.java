package com.cloudnut.payment.application.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagingResponseDTO<T> {
    private List<T> pageContent;

    private PageInfo pageInfo;

    public static <T> PagingResponseDTO<T> from(List<T> data, int totalPages, long totalElements) {
        return new PagingResponseDTO<>(data, new PageInfo(totalPages, totalElements));
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static class PageInfo {
        private Integer totalPages;
        private Long totalElements;
    }
}
