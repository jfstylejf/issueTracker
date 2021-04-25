package cn.edu.fudan.cloneservice.util;

import cn.edu.fudan.cloneservice.component.RestInterfaceManager;
import cn.edu.fudan.cloneservice.dao.UserInfoDTO;
import cn.edu.fudan.cloneservice.mapper.CloneMeasureMapper;
import cn.edu.fudan.cloneservice.mapper.RepoCommitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


import static org.reflections.Reflections.log;

@Component
public class UserUtil {

    RestInterfaceManager restInterfaceManager;
    CloneMeasureMapper cloneMeasureMapper;
    RepoCommitMapper repoCommitMapper;

    @Autowired
    public UserUtil(RestInterfaceManager restInterfaceManager, CloneMeasureMapper cloneMeasureMapper, RepoCommitMapper repoCommitMapper){
        this.restInterfaceManager = restInterfaceManager;
        this.cloneMeasureMapper = cloneMeasureMapper;
        this.repoCommitMapper = repoCommitMapper;
    }

    public List<Integer> getVisibleProjectByToken(String token) {
        UserInfoDTO userInfoDTO = null;
        try {
            userInfoDTO = restInterfaceManager.getUserInfoByToken(token);
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        if(userInfoDTO == null) {
            log.warn("no userInfo, token : {}",token);
            return new ArrayList<>();
        }
        //用户权限为admin时 查询所有的repo
        if (userInfoDTO.getRight().equals(0)) {
            List<Integer> list = new ArrayList<>();
            list.addAll(repoCommitMapper.getProjectIds());
            return list;
        }else {
            String userUuid = userInfoDTO.getUuid();
            return cloneMeasureMapper.getProjectByAccountId(userUuid);
        }
    }


}
