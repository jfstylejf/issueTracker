package cn.edu.fudan.dependservice.domain;

import lombok.Data;

import java.util.List;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-06-04 16:23
 **/
@Data
public class GroupData {
    Integer page;
    Integer records; // size of relation
    List<GroupView> rows;
    Integer total;  // all page' size

}
