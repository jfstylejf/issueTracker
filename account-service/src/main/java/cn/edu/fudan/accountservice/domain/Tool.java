package cn.edu.fudan.accountservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zyh
 * @date 2020/2/25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tool {
    private String uuid;
    private String toolType;
    private String toolName;
    private String description;

    /**
     * 0表示不使用工具，1表示使用工具
     */
    private int enabled;
    private int installed;
    private String accountName;

}
