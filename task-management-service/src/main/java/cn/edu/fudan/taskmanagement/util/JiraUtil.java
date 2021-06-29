package cn.edu.fudan.taskmanagement.util;

import cn.edu.fudan.taskmanagement.JiraDao.UserInfoDTO;
import cn.edu.fudan.taskmanagement.component.JiraAPI;
import cn.edu.fudan.taskmanagement.mapper.JiraMapper;
import cn.edu.fudan.taskmanagement.mapper.RepoCommitMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JiraUtil {

    @Autowired
    private RepoCommitMapper repoCommitMapper;
    @Autowired
    private JiraMapper jiraMapper;
    @Autowired
    private JiraAPI jiraAPI;
    public String getJiraIdFromCommitMsg(String commitMsg) {

        // 使用Pattern类的compile方法，传入jira单号的正则表达式，得到一个Pattern对象
        Pattern pattern = Pattern.compile("[A-Z][A-Z0-9]*-[0-9]+");
        // 调用pattern对象的matcher方法，传入需要匹配的字符串， 得到一个匹配器对象
        Matcher matcher = pattern.matcher(commitMsg);

        // 从字符串开头，返回匹配到的第一个字符串
        if (matcher.find()) {
            // 输出第一次匹配的内容
            // log.info("jira ID is : {}",matcher.group());
            return matcher.group();
        }
        return "noJiraUuid";

    }

    public <T> List<T> castJsonArrayToList(JSONArray jsonArray) {
        List<T> list = new ArrayList<T>();
        for (Object o : jsonArray) {
            list.add((T) o);
        }
        return list;
    }

    public List<String> splitRepoUuid(String repoUuid, String developer) {

        List<String> repoUuids = new ArrayList<>();
        String trim = ",";
        String[] targetRepos = new String[0];
        if (repoUuid!=null) {
            if (repoUuid.contains(trim)) {
                targetRepos = repoUuid.split(trim);
                repoUuid = null;
            }
        }
        repoUuids.add(repoUuid);
        if (StringUtils.isEmpty(repoUuid)) {
            repoUuids = castJsonArrayToList(JSONArray.parseArray(JSON.toJSONString(jiraMapper.getRepoUuidByDeveloper(developer))));
        }
        if (targetRepos.length != 0) {
            repoUuids = Arrays.asList(targetRepos);
        }

        return repoUuids;
    }

    public List<String> splitJiraUuid(List<String> repoUuids, String developer, String since, String until) {

        // 使用Pattern类的compile方法，传入jira单号的正则表达式，得到一个Pattern对象
        Pattern pattern = Pattern.compile("[A-Z][A-Z0-9]*-[0-9]+");

        //get jira uuid from database
        List<String> jiraUuidList = new ArrayList<>();
        for (String repoId : repoUuids) {
            List<String> individualJiraList = jiraMapper.getJiraIdList(repoId, since, until, developer);
            if (individualJiraList.isEmpty()) continue;
            for (String jiraUuid : individualJiraList) {
                //make sure jira uuid matching the pattern
                Matcher matcher = pattern.matcher(jiraUuid);
                if (!jiraUuidList.contains(jiraUuid) && matcher.find()) {
                    jiraUuidList.add(jiraUuid);
                }
            }
        }
        return jiraUuidList;
    }


//    public Repository union(List<Repository> repositories) {
//
//        List<BaseData> unionBaseDataList = new ArrayList<>();
//        unionBaseDataList.add(new Measure());
//        unionBaseDataList.add(new Issue());
//        unionBaseDataList.add(new CodeTracker());
//        for(Repository repository : repositories) {
//            for(BaseData unionBaseData : unionBaseDataList) {
//                for(Field field : unionBaseData.getClass().getDeclaredFields()) {
//                    String sourceName = field.getName();
//                    try {
//                        if (field.get(unionBaseData) instanceof Integer) {
//                            BaseData targetBaseData = findBaseDataByFieldName(sourceName,repository.getBaseDataList());
//                            if(targetBaseData == null) {
//                                log.info("can not inject field {}", sourceName);
//                                continue;
//                            }
//                            Field targetField = targetBaseData.getClass().getDeclaredField(sourceName);
//                            targetField.setAccessible(true);
//                            field.set(unionBaseData,(int) targetField.get(targetBaseData) + (int) field.get(unionBaseData));
//                        }
//                    }catch (Exception e) {
//                        log.error("exception is {}; message is {}", e.getClass(), e.getMessage());
//                    }
//                }
//            }
//        }
//        return Repository.builder()
//                .queryCondition(null).repoName(null).baseDataList(unionBaseDataList)
//                .build();
//    }
//
//    public BaseData findBaseDataByFieldName(String sourceName, List<BaseData> baseDataList) {
//        for (BaseData baseData : baseDataList) {
//            for (Field field : baseData.getClass().getFields()) {
//                if (sourceName.equals(field.getName())) {
//                    return baseData;
//                }
//            }
//        }
//        return null;
//    }
public List<Integer> getVisibleProjectByToken(String token) {
    UserInfoDTO userInfoDTO = null;
    try {
        userInfoDTO = jiraAPI.getUserInfoByToken(token);
    }catch (Exception e) {
    }
    if(userInfoDTO == null) {
        return new ArrayList<>();
    }
    //用户权限为admin时 查询所有的repo
    if (userInfoDTO.getRight().equals(0)) {
        List<Integer> list = new ArrayList<>();
        list.addAll(repoCommitMapper.getProjectIds());
        return list;
    }else {
        String userUuid = userInfoDTO.getUuid();
        return jiraMapper.getProjectByAccountId(userUuid);
    }
}
}
