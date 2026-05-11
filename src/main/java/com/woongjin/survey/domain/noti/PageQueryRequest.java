package com.woongjin.survey.domain.noti;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PageQueryRequest {
    private final int page;
    private final int size;

    public PageQueryRequest(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0 || size >= 100) size = 10;

        this.page = page;
        this.size = size;
    }

    public PageRequest toPageable() { return PageRequest.of(page, size); }

    public PageRequest toPageable(Sort sort) { return PageRequest.of(page, size, sort); }
}