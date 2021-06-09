package cn.edu.fudan.measureservice.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @ClassName: ProjectBigFileDetail
 * @Description: 项目总览页面 超大文件数明细
 * @Author wjzho
 * @Date 2021/4/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectBigFileDetail implements Serializable {
    /**
     * 项目id
     */
    @ExcelIgnore
    private String projectId;
    /**
     * 项目名称
     */
    @ColumnWidth(20)
    @ExcelProperty("项目名")
    private String projectName;
    /**
     * 代码库
     */
    @ExcelIgnore
    private String repoUuid;
    /**
     * 库名称
     */
    @ColumnWidth(40)
    @ExcelProperty("库名称")
    private String repoName;
    /**
     * 文件路径
     */
    @ColumnWidth(60)
    @ExcelProperty("文件路径")
    private String filePath;
    /**
     * 最近修改时间
     */
    @ColumnWidth(20)
    @ExcelProperty("最新修改时间")
    private String currentModifyTime;
    /**
     * 最新行数
     */
    @ColumnWidth(10)
    @ExcelProperty("最新行数")
    private int currentLines;
}
