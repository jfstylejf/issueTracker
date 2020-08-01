package cn.edu.fudan.cloneservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author fancying
 */
@Data
@Builder
@AllArgsConstructor
public class ResponseBean implements Serializable {

    private int code;
    private String msg;
    private Object data;

}
