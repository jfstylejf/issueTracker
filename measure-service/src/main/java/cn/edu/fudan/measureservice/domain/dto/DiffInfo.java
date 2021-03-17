package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: TextInfo
 * @Description: 文件文本信息
 * @Author wjzho
 * @Date 2021/3/11
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DiffInfo {

    private String filePath;
    private String repoPath;
    private String repoUuid;
    private String commitId;
    private int addLines;
    private int delLines;
    private int addCommentLines;
    private int delCommentLines;
    private int addWhiteLines;
    private int delWhiteLines;

}
