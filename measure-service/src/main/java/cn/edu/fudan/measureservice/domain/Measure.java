package cn.edu.fudan.measureservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * todo 先沿用一下之前的类 后面重构
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Measure {
    private Total total;
    private Packages packages;
    private Objects objects;
    private Functions functions;
}
