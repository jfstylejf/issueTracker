package cn.edu.fudan.dependservice.domain;

import lombok.Data;

import java.util.List;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-04-16 14:48
 **/
@Data
public class RelationData {
    Integer page;
    Integer records; // size of relation
    List<RelationView> rows;
    Integer total;  // all page' size
}
