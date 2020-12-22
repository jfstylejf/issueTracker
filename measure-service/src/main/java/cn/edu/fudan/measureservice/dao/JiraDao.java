package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.Query;
import cn.edu.fudan.measureservice.mapper.JiraMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author wjzho
 */
@Slf4j
@Repository
public class JiraDao {

    /**
     * JiraDao层：jira数据处理层
     */
    private RestInterfaceManager restInterface;

    private JiraMapper jiraMapper;


    public int getDeveloperJiraCommitCount(Query query) {
        return jiraMapper.getJiraCountByCondition(query.getRepoUuidList(),query.getSince(),query.getUntil(),query.getDeveloper());
    }

    /**
     * 根据commit message 返回 对应的 jira 单号
     * @param commitMsg commit查询信息
     * @return {jira ID} or "noJiraID"
     */
    public String getJiraIDFromCommitMsg(String commitMsg){
        // 使用Pattern类的compile方法，传入jira单号的正则表达式，得到一个Pattern对象
        Pattern pattern = Pattern.compile("[A-Z][A-Z0-9]*-[0-9]+");
        // 调用pattern对象的matcher方法，传入需要匹配的字符串， 得到一个匹配器对象
        Matcher matcher = pattern.matcher(commitMsg);

        // 从字符串开头，返回匹配到的第一个字符串
        if (matcher.find()) {
            // 输出第一次匹配的内容
            log.info("jira ID is : {}",matcher.group());
            return matcher.group();
        }
        return "noJiraID" ;
    }



    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface){this.restInterface=restInterface;}

    @Autowired
    public void setJiraMapper(JiraMapper jiraMapper) {this.jiraMapper=jiraMapper;}

}
