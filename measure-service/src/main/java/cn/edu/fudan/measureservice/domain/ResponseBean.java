package cn.edu.fudan.measureservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * @author fancying
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBean<T> implements Serializable {

    private int code;

    private String msg;

    private T data;

}
