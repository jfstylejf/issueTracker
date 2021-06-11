package cn.edu.fudan.dependservice.service;

import cn.edu.fudan.dependservice.domain.GroupData;
import cn.edu.fudan.dependservice.domain.RelationData;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

@Service
public interface GroupService {
    GroupData getGroups(String ps, String page, String project_names, String scan_until, String order);
    JSONObject getGroupDetail(String groupId,String commitId,String repoUuid);

}
