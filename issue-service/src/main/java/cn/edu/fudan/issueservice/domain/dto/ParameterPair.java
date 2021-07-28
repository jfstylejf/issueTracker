package cn.edu.fudan.issueservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: ParameterPair
 * @Description:
 * @Author wjzho
 * @Date 2021/7/14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParameterPair {
    /**
     * 参数类型
     */
    private String specifier;
    /**
     * 参数名
     */
    private String parameterName;

    /**
     * 起始行号
     */
    private int startPosition;

    /**
     * 结束行号
     */
    private int endPosition;
}
