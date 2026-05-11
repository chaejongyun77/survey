package com.woongjin.survey.domain.noti;

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
}