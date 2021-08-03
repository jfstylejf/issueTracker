package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileInfo implements Serializable {

    private String absolutePath;
    private String relativePath;
    /**
     * 文件方法列表
     */
    private List<MethodInfo> methodInfoList;
    /**
     * 文件声明类的成员列表
     */
    private List<ParameterPair> memberList;
    /**
     * 文件枚举类成员列表
     */
    private List<ParameterPair> enumList;
    /**
     * 文件全局变量列表
     */
    private List<ParameterPair> globalParameterList;

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
