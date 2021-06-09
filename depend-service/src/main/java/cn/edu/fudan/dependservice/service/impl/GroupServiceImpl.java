package cn.edu.fudan.dependservice.service.impl;

import cn.edu.fudan.dependservice.dao.RelationDao;
import cn.edu.fudan.dependservice.dao.StatisticsDao;
import cn.edu.fudan.dependservice.domain.*;
import cn.edu.fudan.dependservice.mapper.LocationMapper;
import cn.edu.fudan.dependservice.service.GroupService;
import cn.edu.fudan.dependservice.util.TimeUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupServiceImpl implements GroupService {
    @Autowired
    RelationDao relationDao;

    private Map<String, String> type_C2E;

    @Autowired
    public void setType_C2E() {
        type_C2E = new HashMap<>();
        type_C2E.put("调用", "CALL");
        type_C2E.put("继承", "EXTENDS");
        type_C2E.put("实现", "IMPLEMENTS");
    }

    private Map<String, String> type_E2C;

    @Autowired
    public void setType_E2C() {
        type_E2C = new HashMap<>();
        type_E2C.put("CALL", "调用");
        type_E2C.put("EXTENDS", "继承");
        type_E2C.put("IMPLEMENTS", "实现");
    }

    @Autowired
    StatisticsDao statisticsDao;

    LocationMapper locationMapper;


    @Autowired
    public void locationMapper(LocationMapper locationMapper) {
        this.locationMapper = locationMapper;
    }

    @Override
    public GroupData getGroups(String ps, String page, String project_names, String scan_until, String order) {
        if (scan_until == null || scan_until.length() == 0) {
            scan_until = TimeUtil.getCurrentDateTime();
        }
        List<RelationView> res = relationDao.getRelationBydate(scan_until);
        if (project_names != null && project_names.length() > 0) {
            List<String> projects = Arrays.asList(project_names.split(","));
            res = res.stream().filter(e -> projects.contains(e.getProjectName())).collect(Collectors.toList());
        }
        GroupData groupData = new GroupData();
        Map<String, GroupView> map = new HashMap<>();
//        int id = 1;
        for (RelationView r : res) {
            String groupid = r.getProjectName() + '-' + r.getRepoName() + '-' + r.getGroupId();
            if (map.containsKey(groupid)) {
                map.get(groupid).getFileStrings().add(r.getSourceFile());
                map.get(groupid).getFileStrings().add(r.getTargetFile());
            } else {
                GroupView groupView = new GroupView();
                groupView.setFiles(new HashSet<>());
                groupView.setCommit_id(r.getCommit_id());
                groupView.setGroupId(groupid);
                groupView.setProjectName(r.getProjectName());
                groupView.setRepoUuid(r.getRepoUuid());
                groupView.setRepoName(r.getRepoName());
                groupView.setCommit_id(r.getCommit_id());
                groupView.setFileStrings(new HashSet<>());
                map.put(groupid, groupView);
            }
        }
        ArrayList<GroupView> groups = new ArrayList(map.values());
        groupData.setRows(pageGroup(ps, page, groups));
        groupData.setTotal(groups.size());
        groupData.setPage(Integer.valueOf(page));
        groupData.setRecords(groups.size());
        return groupData;
    }

    public List<GroupView> pageGroup(String ps, String page, List<GroupView> res) {
        List<GroupView> reslist;
        res.sort((o1, o2) -> {
            if (o1.getProjectName().equals(o2.getProjectName())) {
                if (o1.getRepoUuid().equals(o2.getRepoUuid())) {
                    int o1groupId = Integer.valueOf(o1.getGroupId().substring(o1.getGroupId().lastIndexOf('-') + 1));
                    int o2groupId = Integer.valueOf(o2.getGroupId().substring(o2.getGroupId().lastIndexOf('-') + 1));
                    return o1groupId - o2groupId;
                } else {
                    return o1.getRepoUuid().compareTo(o2.getRepoUuid());
                }
            } else {

                return o1.getRepoName().compareTo(o2.getRepoName());
            }
        });
        AtomicInteger id = new AtomicInteger(1);
        res.stream().forEach(e -> e.setId(id.getAndIncrement()));
        int intpage = Integer.valueOf(page);
        int intps = Integer.valueOf(ps);
        int start = intps * (intpage - 1);
        if (start + intps > res.size()) {
            reslist = res.subList(start, res.size());
        } else {
            reslist = res.subList(start, start + intps);
        }
        return reslist;
    }

    @Override
    public JSONObject getGroupDetail(String groupId, String commitId, String repoUuid) {
        int intgroupId = Integer.valueOf(groupId.substring(groupId.lastIndexOf('-') + 1));
        List<RelationView> res = relationDao.getRelationsdInGroup(repoUuid, commitId, intgroupId);
        GroupView groupView = new GroupView(res, groupId);
        return getMyJson(groupView);
    }

    public JSONObject getMyJson(GroupView groupView) {
        JSONObject result = new JSONObject();
        JSONArray nodesJson = new JSONArray();
        JSONArray edgesJson = new JSONArray();
        JSONArray smellsJson = new JSONArray();
        List<RelationView> relationViews = groupView.getRelationViews();
        List<FileNode> fileNodes = new ArrayList<>();
        for (int i = 0; i < relationViews.size(); i++) {
            RelationView re = relationViews.get(i);
            re.setDependsOnTypes();
            FileNode source = new FileNode(re.getSourceFile());
            FileNode target = new FileNode(re.getTargetFile());
            if (!fileNodes.contains(source)) {
                fileNodes.add(source);
                source.setId(fileNodes.size());
            } else {
                for (FileNode f : fileNodes) {
                    if (f.getFilePath().equals(source.getFilePath())) {
                        source.setId(f.getId());
                        break;
                    }
                }
            }
            if (!fileNodes.contains(target)) {
                fileNodes.add(target);
                target.setId(fileNodes.size());
            } else {
                for (FileNode f : fileNodes) {
                    if (f.getFilePath().equals(target.getFilePath())) {
                        target.setId(f.getId());
                        break;
                    }
                }
            }

            JSONObject edgeJson = new JSONObject();
            edgeJson.put("id", i);
            edgeJson.put("source", source.getId());
            edgeJson.put("target", target.getId());
            edgeJson.put("source_name", source.getFileName());
            edgeJson.put("target_name", target.getFileName());
            edgeJson.put("source_label", source.getId());
            edgeJson.put("target_label", target.getId());
            edgeJson.put("times", re.getTimes());
            edgeJson.put("dependsOnTypes", re.getDependsOnTypes());
            edgesJson.add(edgeJson);
        }
        for (FileNode fileNode : fileNodes) {
            JSONObject nodeJson = new JSONObject();
            nodeJson.put("id", fileNode.getId());
            nodeJson.put("name", fileNode.getFileName());
            nodeJson.put("path", fileNode.getFilePath());
            nodeJson.put("label", fileNode.getId());
            nodeJson.put("size", 30);
            nodesJson.add(nodeJson);
        }
        JSONArray smellFilesJson = new JSONArray();
        JSONObject smellJson = new JSONObject();
        for (FileNode smellFile : fileNodes) {

            JSONObject smellFileJson = new JSONObject();
            smellFileJson.put("index", smellFile.getId());
            smellFileJson.put("path", smellFile.getFilePath());
            smellFilesJson.add(smellFileJson);
        }
        smellJson.put("nodes", smellFilesJson);
        smellsJson.add(smellJson);
        result.put("smellType", "CYCLIC_DEPENDENCY");
        result.put("coreNode", "0");
        result.put("nodes", nodesJson);
        result.put("edges", edgesJson);
        result.put("smells", smellsJson);
        return result;
    }

    public JSONObject getFileCyclicDependencyJson(GroupView groupView) {
        JSONObject result = new JSONObject();
        JSONArray nodesJson = new JSONArray();
        JSONArray edgesJson = new JSONArray();
        JSONArray smellsJson = new JSONArray();
        List<FileNode> files = new ArrayList<>();
        if (groupView != null) {
            String key = groupView.getGroupId();
//            if (cache.get(getClass(), key) != null) {
//                return cache.get(getClass(), key);
//            }
            JSONObject smellJson = new JSONObject();
            smellJson.put("name", groupView.getGroupId());
            Set<FileNode> smellFiles = new HashSet<>(groupView.getFiles());
            JSONArray smellFilesJson = new JSONArray();
            for (FileNode smellFile : smellFiles) {
                if (!files.contains(smellFile)) {
                    files.add(smellFile);
                }
                JSONObject smellFileJson = new JSONObject();
                smellFileJson.put("index", files.indexOf(smellFile) + 1);
                smellFileJson.put("path", smellFile.getFilePath());
                smellFilesJson.add(smellFileJson);
            }
            smellJson.put("nodes", smellFilesJson);
            smellsJson.add(smellJson);
        }
        int length = files.size();
        for (int i = 0; i < length; i++) {
            FileNode sourceFile = files.get(i);
            JSONObject nodeJson = new JSONObject();
            nodeJson.put("id", sourceFile.getId());
            nodeJson.put("name", sourceFile.getFileName());
            nodeJson.put("path", sourceFile.getFilePath());
            nodeJson.put("label", i + 1);
            nodeJson.put("size", 30);
            nodesJson.add(nodeJson);
//            for (int j = 0 ; j < length; j ++) {
//                FileNode targetFile = files.get(j);
//                if (i != j) {
//                    RelationNode dependsOn = dependsOnRepository.findDependsOnBetweenFiles(sourceFile.getId(), targetFile.getId());
//                    if (dependsOn != null) {
//                        JSONObject edgeJson = new JSONObject();
//                        edgeJson.put("id", dependsOn.getId().toString());
//                        edgeJson.put("source", sourceFile.getId().toString());
//                        edgeJson.put("target", targetFile.getId().toString());
//                        edgeJson.put("source_name", sourceFile.getName());
//                        edgeJson.put("target_name", targetFile.getName());
//                        edgeJson.put("source_label", i + 1);
//                        edgeJson.put("target_label", j + 1);
//                        edgeJson.put("times", dependsOn.getTimes());
//                        edgeJson.put("dependsOnTypes", dependsOn.getDependsOnTypes());
//                        edgesJson.add(edgeJson);
//                    }
//                }
//            }
        }
        addEdgeJson(edgesJson, groupView.getRelationViews());
        result.put("smellType", "CYCLIC_DEPENDENCY");
        result.put("coreNode", "0");
        result.put("nodes", nodesJson);
        result.put("edges", edgesJson);
        result.put("smells", smellsJson);
        return result;
    }

    private void addEdgeJson(JSONArray edgesJson, List<RelationView> relationViews) {
        for (RelationView relationView : relationViews) {
            JSONObject edgeJson = new JSONObject();
            edgesJson.add(edgeJson);

        }
    }


}
