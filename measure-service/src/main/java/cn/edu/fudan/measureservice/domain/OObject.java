package cn.edu.fudan.measureservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OObject对应的就是一个文件内的信息
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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


//    public OObject(String name, int ncss, int functions, int classes, int javaDocs, int javaDocsLines, int singleCommentLines, int implementationCommentLines) {
//        this.name = name;
//        this.ncss = ncss;
//        this.functions = functions;
//        this.classes = classes;
//        this.javaDocs = javaDocs;
//        this.javaDocsLines = javaDocsLines;
//        this.singleCommentLines = singleCommentLines;
//        this.implementationCommentLines = implementationCommentLines;
//    }
}
