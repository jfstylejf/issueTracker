package cn.edu.fudan.issueservice.domain.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeQualityResponse {
    private int totalCountQualities;
    private int page;
    private int ps;

    private Quality totalQuality;
    List<TimeQuality> qualities = new ArrayList<>();
    List<DeveloperQuality> developers = new ArrayList<>();
}
