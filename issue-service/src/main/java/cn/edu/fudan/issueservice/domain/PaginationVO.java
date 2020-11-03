package cn.edu.fudan.issueservice.domain;

import java.io.Serializable;

/**
 * description: 展示分页信息
 *
 * @author fancying
 * create: 2020-10-20 16:58
 **/
public class PaginationVO<T>  implements Serializable {

    private Integer total;
    private Integer pageSize;
    private Integer page;

    private T data;
}