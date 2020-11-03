package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBean<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

}
