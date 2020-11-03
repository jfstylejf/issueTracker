package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TimeGranularity {
    //时间粒度每天
    DAY("day"),
    //时间粒度每周
    WEEK("week"),
    //时间粒度每月
    MONTH("month");
    private String type;
}
