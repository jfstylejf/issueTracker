package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MethodInfo {
    private String absoluteFilePath;
    private String methodName;
    private int methodCcn;
    /**
     * 方法修饰符
     */
    private String specifier;
    /**
     * 方法参数信息
     */
    private List<ParameterPair> methodParameter;
    /**
     * 方法起始行
     */
    private int startPosition;
    /**
     * 方法结束行
     */
    private int endPosition;

    {
        methodParameter = new ArrayList<>();
    }

    public String getMethodSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.specifier).append(" ").append(this.methodName).append("(");
        for (int i = 0; i < methodParameter.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(methodParameter.get(i).getSpecifier());
        }
        return sb.append(")").toString();
    }

}
