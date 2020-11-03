package cn.edu.fudan.scanservice.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "scan请求参数DTO", description = "scan服务请求scan接口的数据封装在此entity中")
public class ScanRequestMessage {

    @ApiModelProperty(value = "扫描的repo库", name = "repoId")
    private String repoId;
    @ApiModelProperty(value = "扫描的起始commit", name = "commitId")
    private String commitId;
    @ApiModelProperty(value = "扫描的分支", name = "branch")
    private String branch;
    @ApiModelProperty(value = "扫描的起始时间.如果传入这个参数，则从这个时间后的第一个commit开始扫(后端还未实现)", name = "startTime")
    private String startTime;

}