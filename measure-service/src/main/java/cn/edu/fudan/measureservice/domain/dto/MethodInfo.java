package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String position;
}
