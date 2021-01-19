package cn.edu.fudan.measureservice.domain.vo;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperWorkLoadFrontend {
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
    private List<DeveloperWorkLoad> rows;
}
