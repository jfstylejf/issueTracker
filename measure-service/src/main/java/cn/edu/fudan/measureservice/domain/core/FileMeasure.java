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
    int totalLine;
}