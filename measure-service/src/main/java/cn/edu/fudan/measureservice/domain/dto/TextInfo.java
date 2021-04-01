package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: TextInfo
 * @Description:
 * @Author wjzho
 * @Date 2021/3/30
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TextInfo {

    private int codeLines;

    private int blankLines;

    private int commentLines;

    private int totalLines;

}
