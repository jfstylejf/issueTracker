package cn.edu.fudan.measureservice.domain.core;

import lombok.*;

/**
 * description:
 *
 * @author fancying
 * create: 2020-06-10 23:56
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMeasure{
    String uuid;
    String repoUuid;
    String commitId;
    String commitTime;
    String filePath;

    int diffCcn;
    int ccn;
    int addLine;
    int deleteLine;
    /**
     * 相对物理行（去除 注释，空白行）
     */
    int totalLine;
    /**
     * 绝对物理行（包含 注释，空白行）
     */
    int absoluteLine;
}