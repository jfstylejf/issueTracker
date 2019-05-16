/**
 * @description:
 * @author: fancying
 * @create: 2019-04-08 17:00
 **/
package cn.edu.fudan.issueservice.service.impl;

import cn.edu.fudan.issueservice.component.RestInterfaceManager;
import cn.edu.fudan.issueservice.dao.IssueDao;
import cn.edu.fudan.issueservice.dao.RawIssueDao;
import cn.edu.fudan.issueservice.service.IssueRankService;
import cn.edu.fudan.issueservice.util.ExecuteShellUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Service
@Slf4j
public class IssueRankServiceImpl implements IssueRankService {

    private RawIssueDao rawIssueDao;
    private IssueDao issueDao;
    private ExecuteShellUtil executeShellUtil;
    private RestInterfaceManager restInterfaceManager;
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setRawIssueDao(RawIssueDao rawIssueDao) {
        this.rawIssueDao = rawIssueDao;
    }

    @Autowired
    public void setIssueDao(IssueDao issueDao) {
        this.issueDao = issueDao;
    }

    @Autowired
    public void setExecuteShellUtil(ExecuteShellUtil executeShellUtil) {
        this.executeShellUtil = executeShellUtil;
    }

    @Autowired
    public void setRestInterfaceManager(RestInterfaceManager restInterfaceManager) {
        this.restInterfaceManager = restInterfaceManager;
    }

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }


    // 参与开发者最多的文件排名（？）	周，月	文件，项目	看项目的参与者的多少 ???? 单独放在另外的服务中
    @Override
    public Map rankOfFileBaseDeveloperQuantity(String repoId, String duration, String spaceType, String detail) {

        return null;
    }

    @Override
    public Object rankOfFileBaseIssueQuantity(String repoId, String commitId) {
        Map<String, String> map = new WeakHashMap<>();
        for (Map<String, String> m : rawIssueDao.getRankOfFileBaseIssueQuantity(repoId, commitId)) {
            map.put(m.get("key"), m.get("value"));
        }
        return sortByValue(map);
    }


    // 需要文件的代码行数 需要提前入库
    @Override
    public Map rankOfFileBaseDensity(String repoId, String commitId) {
        Map<String, String> map = new WeakHashMap<>();
        for (Map<String, String> m : rawIssueDao.getRankOfFileBaseDensity(repoId, commitId)) {
            map.put(m.get("key"), m.get("value"));
        }
        return sortByValue(map);
    }

    // 开发人员在某段时间内贡献的代码行数 除以 产生的新Issue数量
    // 平均每多少行代码会产生一个新的Issue
    @Override
    public Map rankOfDeveloper(String repoId, String duration) {
        // duration: 2018.01.01-2018.12.12
        if (duration.length() < 21)
            throw new RuntimeException("duration error!");
        String start = duration.substring(0,10);
        String end = duration.substring(11,21);
        String repoPath = restInterfaceManager.getRepoPath(repoId);
        //commitId 单个commit所对应的引入Issue数量
        Map<String, Integer> commitNewIssue = issueDao.getCommitNewIssue(start, end, repoId);
        // 某段时间内的commit列表以及对应的Issue数量 commitId developer-email
        // 一个开发人员可能对应多个commit
        Map<String, String> commitDeveloper = restInterfaceManager.getDeveloperByCommits(commitNewIssue.keySet());

        // 某段时间内开发人员列表
        Set<String> developers = new HashSet<>(commitDeveloper.values());

        // 开发人员 代码行数
        Map<String,Integer> usersCodeLine  = executeShellUtil.developersLinesOfCode(start, end , repoPath, developers);

        // 开发人员 issue数量
        Map<String, Integer> developerNewIssue = new ConcurrentHashMap<>(16);
        for (String commitId : commitDeveloper.keySet()) {
            String developer = commitDeveloper.get(commitId);
            int newIssue = commitNewIssue.get(commitId);
            if (developerNewIssue.containsKey(developer)) {
                newIssue += developerNewIssue.get(developer);
            }
            developerNewIssue.put(developer, newIssue);
        }

        for (String developer : developerNewIssue.keySet()) {
            usersCodeLine.put(developer, usersCodeLine.get(developer) / developerNewIssue.get(developer) );
        }

        //排序
        return sortByValue(usersCodeLine);
    }

    //基于目前最新版本的排名
    @Override
    @SuppressWarnings("unchecked")
    public Map rankOfRepoBaseDensity(String token) {
        String userId = restInterfaceManager.getAccountId(token);

        //得到用户的所有项目目前的代码函数
        JSONArray jsonArray = restInterfaceManager.getProjectList(userId);
        Map<String, String> repoIDName = new ConcurrentHashMap<>(10);
        for(Object json : jsonArray ) {
            Map<String, String> map = (LinkedHashMap)json;
            repoIDName.put(map.get("repo_id"), map.get("name"));
        }
        Map repoCommit = restInterfaceManager.getRepoAndLatestCommit(repoIDName.keySet());
        Map<String, Integer> repoCodeLine = restInterfaceManager.getRepoAndCodeLine(repoCommit);

        //得到项目所对应的剩余issue数量
        Map<String, Integer> repoIssueNum = rawIssueDao.getRepoAndIssueNum(repoCommit);
        try {
            for (String repoId : repoCodeLine.keySet()) {
                int codeLines = repoCodeLine.get(repoId);
                int newIssue = repoIssueNum.get(repoId);
                // 平均多少行代码会出现一个bug（取整）
                repoIssueNum.put(repoId, codeLines/newIssue);
            }
        }catch (NullPointerException e) {
            throw new RuntimeException(e.getMessage());
        }

        return repoIssueNum;
    }

    // 默认由大到小排序
    //类型 V 必须实现 Comparable 接口，并且这个接口的类型是 V 或 V 的任一父类。这样声明后，V 的实例之间，V 的实例和它的父类的实例之间，可以相互比较大小。
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
/*        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, (o1,o2) -> (o2.getValue()).compareTo(o1.getValue()));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }*/
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        //st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> result.put(e.getKey(), e.getValue()));
        st.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

}