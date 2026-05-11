/*
package com.woongjin.survey.domain.noti;

import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Getter
public class NotificationHistoryListQueryRequest extends PageQueryRequest {

    public NotificationHistoryListQueryRequest(int page, int size) {
        super(page, size);
    }

    @Override
    public PageRequest toPageable() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        return super.toPageable(sort);
    }
}*/
