package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.Query;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wjzho
 */
@Slf4j
@Repository
public class CodeTrackerDao {


    private RestInterfaceManager restInterface;

    /**
     * 获取开发者个人逻辑行数信息
     * @param query 查询条件
     * @return Map<String,Object>Map<String,Object> key : developerTotalStatement, developerAddStatement, developerDeleteStatement, developerChangeStatement, developerValidStatement
     */
    public Map<String,Object> getDeveloperStatementInfo(Query query) {
        Map<String,Object> map = new HashMap<>(8);
        int developerAddStatement = 0;
        int developerDeleteStatement = 0;
        int developerChangeStatement = 0;
        int developerTotalStatement = 0;
        int developerValidStatement = 0;
        JSONObject developerStatements = null;
        try {
            developerStatements = restInterface.getStatements(query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),query.getDeveloper());
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        if  (developerStatements != null){
            developerTotalStatement = developerStatements.getIntValue("total");
            if(developerStatements.getJSONObject("developer")!= null && !developerStatements.getJSONObject("developer").isEmpty()) {
                developerAddStatement = developerStatements.getJSONObject("developer").getJSONObject(query.getDeveloper()).getIntValue("ADD");
                developerDeleteStatement = developerStatements.getJSONObject("developer").getJSONObject(query.getDeveloper()).getIntValue("DELETE");
                developerChangeStatement = developerStatements.getJSONObject("developer").getJSONObject(query.getDeveloper()).getIntValue("CHANGE");
                developerValidStatement = developerStatements.getJSONObject("developer").getJSONObject(query.getDeveloper()).getIntValue("CURRENT");
            }
        }
        map.put("developerTotalStatement",developerTotalStatement);
        map.put("developerAddStatement",developerAddStatement);
        map.put("developerDeleteStatement",developerDeleteStatement);
        map.put("developerChangeStatement",developerChangeStatement);
        map.put("developerValidStatement",developerValidStatement);
        return map;
    }

    /**
     * 获取所有开发者逻辑行数信息
     * @param query 查询条件
     * @return Map<String,Object> key : totalStatement, totalAddStatement, totalDeleteStatement, totalChangeStatement, totalValidStatement
     */
    public Map<String,Object> getAllDeveloperStatementInfo(Query query) {
        Map<String,Object> map = new HashMap<>(8);
        int totalAddStatement = 0;
        int totalDeleteStatement = 0;
        int totalChangeStatement = 0;
        int totalStatement = 0;
        int totalValidStatement = 0;
        String repoUuid = query.getRepoUuidList().get(0);
        JSONObject allDeveloperStatements = null;
        try {
            allDeveloperStatements = restInterface.getStatements(query.getRepoUuidList().get(0),query.getSince(),query.getUntil(),"");
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        if(allDeveloperStatements !=null ) {
            totalStatement = allDeveloperStatements.getInteger("total");
            if(allDeveloperStatements.getJSONObject("repo")!=null && !allDeveloperStatements.getJSONObject("repo").isEmpty()) {
                totalAddStatement = allDeveloperStatements.getJSONObject("repo").getJSONObject(repoUuid).getIntValue("ADD");
                totalDeleteStatement = allDeveloperStatements.getJSONObject("repo").getJSONObject(repoUuid).getIntValue("DELETE");
                totalChangeStatement = allDeveloperStatements.getJSONObject("repo").getJSONObject(repoUuid).getIntValue("CHANGE");
                totalValidStatement = allDeveloperStatements.getJSONObject("repo").getJSONObject(repoUuid).getIntValue("CURRENT");
            }
        }
        map.put("totalStatement",totalStatement);
        map.put("totalAddStatement",totalAddStatement);
        map.put("totalDeleteStatement",totalDeleteStatement);
        map.put("totalChangeStatement",totalChangeStatement);
        map.put("totalValidStatement",totalValidStatement);
        return map;
    }

    /**
     * 获取相关的修改文件个数
     * @param repoUuid
     * @param since
     * @param until
     * @param developer
     * @return  Map<String,Object> ，key: developerChangedFile,totalChangedFile
     */
    public Map<String,Object> getChangedFilesCount(String repoUuid,String since,String until,String developer) {
        Map<String,Object> map = new HashMap<>(10);
        int developerChangedFile = 0 ;
        int totalChangedFile = 0;
        JSONObject changedFileCount = restInterface.getFocusFilesCount(repoUuid,since,until,developer);
        if(changedFileCount != null) {
            developerChangedFile = changedFileCount.getJSONObject("developer").getIntValue(developer);
            totalChangedFile = changedFileCount.getIntValue("total");
        }
        map.put("developerChangedFile",developerChangedFile);
        map.put("totalChangedFile",totalChangedFile);
        return map;
    }


    @Autowired
    public void setRestInterface(RestInterfaceManager restInterface) {this.restInterface=restInterface;}
}
