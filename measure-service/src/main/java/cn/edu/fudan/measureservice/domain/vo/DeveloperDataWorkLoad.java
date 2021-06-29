package cn.edu.fudan.measureservice.domain.vo;

import cn.edu.fudan.measureservice.domain.bo.DeveloperCommitStandard;
import cn.edu.fudan.measureservice.domain.bo.DeveloperWorkLoad;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: DeveloperDataWorkLoad
 * @Description:
 * @Author wjzho
 * @Date 2021/6/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeveloperDataWorkLoad {

    /**
     * 开发者姓名
     */
    private String developerName;
    /**
     * 起始时间
     */
    private String since;
    /**
     * 截至时间
     */
    private String until;
    /**
     * 开发者工作量明细
     */
    private DeveloperWorkLoad detail;
    /**
     * 开发者新增物理行数
     */
    private int addLines;
    /**
     * 开发者删除代码行数
     */
    private int deleteLines;
    /**
     * 开发者增删总物理行数
     */
    private int totalLoc;
    /**
     * 开发者工作量评级
     */
    private String level;

}
