package cn.edu.fudan.scanservice.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 *
 * @author fancying
 *
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "API返回的结构体", description = "所有API返回形式都以这个为准")
public class ResponseBean<T> implements Serializable {

    @ApiModelProperty(value = "自定义状态码", name = "code")
    private int code;

    @ApiModelProperty(value = "此次请求返回的消息描述", name = "msg")
    private String msg;

    @ApiModelProperty(value = "此次请求返回的具体数据", name = "data")
    private T data;

}

