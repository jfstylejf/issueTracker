package cn.edu.fudan.measureservice.util;

import cn.edu.fudan.measureservice.domain.dto.DiffInfo;
import cn.edu.fudan.measureservice.domain.dto.TextInfo;

/**
 * description:
 *
 * @author fancying
 * create: 2020-01-06 14:08
 **/
public abstract class FileFilter {

    /**
     * 对文件过滤
     * @param filePath 待过滤文件路径
     * @return 是否过滤 true:过滤 ， false:添加该文件
     */
    public abstract Boolean fileFilter(String filePath);

    /**
     * 对代码行过滤
     * @param fileText 文件代码行
     * @return
     */
    // fixme 注释行匹配有误
    public static DiffInfo diffLineFilter(String[] fileText) {
        int addWhiteLines = 0;
        int addCommentLines = 0;
        int delWhiteLines = 0;
        int delCommentLines = 0;
        for (String line : fileText){
            //若是增加的行，则执行以下筛选语句
            if (line.startsWith("+") && ! line.startsWith("+++")){
                //去掉开头的"+"
                line = line.substring(1);
                //去掉头尾的空白符
                line = line.trim();
                //匹配空白行
                if (line.matches("^[\\s]*$")){
                    addWhiteLines++;
                }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){
                    //匹配注释行
                    addCommentLines++;
                }
            }
            //若是删除的行，则执行以下筛选语句
            if (line.startsWith("-") && ! line.startsWith("---")){
                //去掉开头的"-"
                line = line.substring(1);
                //去掉头尾的空白符
                line = line.trim();
                if (//匹配空白行
                        line.matches("^[\\s]*$")){
                    delWhiteLines++;
                }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){
                    //匹配注释行
                    delCommentLines++;
                }
            }
        }
        return DiffInfo.builder()
                .addWhiteLines(addWhiteLines)
                .addCommentLines(addCommentLines)
                .delWhiteLines(delWhiteLines)
                .delCommentLines(delCommentLines)
                .build();
    }

    public static TextInfo textFilter(String[] text) {
        int totalLines = text.length;
        int blankLines = 0;
        int commentLines = 0;
        for (String line : text) {
            line = line.trim();
            if (line.matches("^[\\s]*$")){
                blankLines++;
            }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){
                //匹配注释行
                commentLines++;
            }
        }
        int codeLines = totalLines - blankLines -commentLines;
        return TextInfo.builder()
                .blankLines(blankLines)
                .codeLines(codeLines)
                .commentLines(commentLines)
                .totalLines(totalLines)
                .build();
    }



}