package cn.edu.fudan.cloneservice.util;

import cn.edu.fudan.cloneservice.mapper.CloneMeasureMapper;
import cn.edu.fudan.cloneservice.mapper.RepoCommitMapper;

import java.util.ArrayList;
import java.util.List;

import static org.reflections.Reflections.log;

public class UserUtil {

    CloneMeasureMapper cloneMeasureMapper;
    RepoCommitMapper repoCommitMapper;

    public List<Integer> getVisibleProjectByToken(String token) {
        UserInfoDTO userInfoDTO = null;
        try {
            userInfoDTO = getUserInfoByToken(token);
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
