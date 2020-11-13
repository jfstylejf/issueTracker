package cn.edu.fudan.common.http;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * note: T 类型如果不是基础类型需要手动实现序列化
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseEntity<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

}