package cn.edu.fudan.measureservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OObject对应的就是一个文件内的信息
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OObject {

    /**
     * 相对路径
     */
    private String path;
    /**
     * 有效代码行 (去除注释，空白行)
     */
    private int ncss;
    private int functions;
    private int classes;
    private int javaDocs;
    private int javaDocsLines;
    private int singleCommentLines;
    private int implementationCommentLines;

    private int ccn;
    /**
     *  note 文件相对物理行数 (去除注释，空白行)， 与 ncss 重复了先保留着
     */
    private int totalLines;
    /**
     * 文件绝对物理行（包含注释， 空白行）
     */
    private int absoluteLines;

}
