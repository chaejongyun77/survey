package com.woongjin.survey.domain.noti;




public record PageQueryResponse<T>(
        long totalElements,
        int totalPages,
        List<T> contents
) {
    public PageQueryResponse(Page<T> page) {
        this(page.getTotalElements(), page.getTotalPages(), page.getContent());
    }
}