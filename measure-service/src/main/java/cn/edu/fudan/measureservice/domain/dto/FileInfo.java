package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfo {

    private String absolutePath;
    private String relativePath;
    private List<MethodInfo> methodInfoList;
    private int fileCcn;
    private int codeLines;
    private int blankLines;
    private int totalLines;

    private static int defaultValue = 0;

    public void calFileCcn() {
        if (fileCcn==defaultValue) {
            if (methodInfoList == null) {
                return;
            }
            for (MethodInfo m : methodInfoList) {
                fileCcn += m.getMethodCcn();
            }
        }
    }
}
