package cn.edu.fudan.dependservice.domain;

import lombok.Data;

import java.util.Map;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-06-07 16:11
 **/
@Data
public class RelationNode {
    private int id;
    private int sourceId;
    private int targetId;

    private Map<String, Integer> dependsOnTypes;

}
