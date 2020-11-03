package cn.edu.fudan.scanservice.domain.dbo;

import lombok.*;

/**
 * description: 工具属性
 *
 * @author fancying
 * create: 2020-03-09 18:20
 **/
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tool {

    public static final int ENABLED = 1;
    public static final int DISABLED = 0;

    private int id;
    private String toolType;
    private String toolName;
    private String description;
    /**
     * 1 for true
     * 0 for false
     */
    @Setter
    private int enabled;
    private int installed;
}