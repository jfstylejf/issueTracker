package cn.edu.fudan.measureservice.domain.vo;

import cn.edu.fudan.measureservice.domain.bo.DeveloperCommitStandard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: ProjectCommitStandardTrendChart
 * @Description:
 * @Author wjzho
 * @Date 2021/3/26
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectCommitStandardTrendChart {
    /**
     * 项目 id
     */
    private String projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 按间隔的最后一天，如，按周间隔，则日期都为周日；按月间隔，日期都为月末
     */
    private String date;
    /**
     * 趋势图节点对应比值
     */
    private String num;
    /**
     * 数据明细
     */
    private List<DeveloperCommitStandard> detail;

    /**
     * 可选参数，用于描述上一行数据字段
     */
    private Map<String,Long> option;

    public void setOption(long value, long total) {
        option.put("value",value);
        option.put("total",total);
    }




}
