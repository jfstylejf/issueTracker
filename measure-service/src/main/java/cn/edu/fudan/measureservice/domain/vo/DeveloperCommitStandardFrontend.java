package cn.edu.fudan.measureservice.domain.vo;

import cn.edu.fudan.measureservice.domain.bo.DeveloperCommitStandard;
import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeveloperCommitStandardFrontend {
    /**
     * 当前页
     */
    private int page;
    /**
     * 总页数
     */
    private int total;
    /**
     * 总记录数
     */
    private int records;
    /**
     * 具体工作行数据
     */
    private List<DeveloperCommitStandard> rows;
}
