package com.woongjin.survey.domain.noti;


import org.springframework.data.domain.Page;

import java.util.List;

public record PageQueryResponse<T>(
        long totalElements,
        int totalPages,
        List<T> contents
) {
    public PageQueryResponse(Page<T> page) {
        this(page.getTotalElements(), page.getTotalPages(), page.getContent());
    }
}