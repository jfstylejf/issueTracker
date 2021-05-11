package cn.edu.fudan.dependservice.controller;

import cn.edu.fudan.dependservice.domain.ResponseBean;
import cn.edu.fudan.dependservice.service.DeleteService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-05-10 16:30
 **/
@RestController
@Slf4j
public class DeleteController {
    @Autowired
    DeleteService deleteService;

    @ApiOperation(value = "删除指定repo信息", notes = "删除指定repo信息", httpMethod = "DELETE")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "repo_uuid", value = "repo的uuid", dataType = "String", required = true)
    })
    @DeleteMapping(value = {"/depend/{repo_uuid}"})
    @CrossOrigin
    public ResponseBean deleteOneRepo(@PathVariable(name = "repo_uuid") String repoUuid,
                                      HttpServletRequest request) {
        log.info("repoId: "+repoUuid);

        try {
            String token = request.getHeader("token");
            deleteService.deleteOneRepo(repoUuid, token);
            return new ResponseBean<>(HttpStatus.OK.value(), "delete success!", null);
        } catch (Exception e) {
            return new ResponseBean<>(HttpStatus.BAD_REQUEST.value(), "delete failed!", null);
        }

    }

}
