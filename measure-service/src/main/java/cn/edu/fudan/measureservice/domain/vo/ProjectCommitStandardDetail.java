package cn.edu.fudan.measureservice.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.converters.booleanconverter.BooleanStringConverter;
import com.alibaba.excel.converters.string.StringBooleanConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ProjectCommitStandardDetail
 * @Description: 项目总览页面 提交规范性明细
 * @Author wjzho
 * @Date 2021/4/14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectCommitStandardDetail {
    /**
     * 项目id
     */
    @ExcelIgnore
    private String projectId;
    /**
     * 项目名称
     */
    @ColumnWidth(10)
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
    @ColumnWidth(10)
    @ExcelProperty("库名称")
    private String repoName;
    /**
     * 提交commit id
     */
    @ColumnWidth(50)
    @ExcelProperty("提交ID号")
    private String commitId;
    /**
     * 提交时间
     */
    @ColumnWidth(30)
    @ExcelProperty("提交时间")
    private String commitTime;
    /**
     * 提交人
     */
    @ColumnWidth(10)
    @ExcelProperty("提交人")
    private String committer;
    /**
     * 提交内容
     */
    @ColumnWidth(50)
    @ExcelProperty("提交明细")
    private String message;
    /**
     * 是否是规范的提交
     * note 这里的boolean再导入excel需要用到 converter，具体的转换类型可见 {@link com.alibaba.excel.converters}
     */
    @ExcelProperty(value = "提交是否规范",converter = StringBooleanConverter.class)
    private boolean isValid;
}
