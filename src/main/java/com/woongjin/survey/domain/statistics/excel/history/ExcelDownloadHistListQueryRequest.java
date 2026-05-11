package com.woongjin.survey.domain.statistics.excel.history;

import com.woongjin.survey.domain.noti.PageQueryRequest;
import lombok.Getter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Getter
public class ExcelDownloadHistListQueryRequest extends PageQueryRequest {

    public ExcelDownloadHistListQueryRequest(int page, int size) {
        super(page, size);
    }

    @Override
    public PageRequest toPageable() {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate");
        return super.toPageable(sort);
    }
}
