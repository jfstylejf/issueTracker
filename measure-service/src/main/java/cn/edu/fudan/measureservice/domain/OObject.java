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

    private String path;
    private int ncss;
    private int functions;
    private int classes;
    private int javaDocs;
    private int javaDocsLines;
    private int singleCommentLines;
    private int implementationCommentLines;

    private int ccn;
    private int totalLines;

}
