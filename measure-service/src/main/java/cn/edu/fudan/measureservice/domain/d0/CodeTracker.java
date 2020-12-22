package cn.edu.fudan.measureservice.domain.d0;

import cn.edu.fudan.measureservice.dao.CodeTrackerDao;
import cn.edu.fudan.measureservice.domain.dto.Query;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * description: codeTracker 服务基础数据
 *
 * @author fancying
 * create: 2020-09-16 10:04
 **/
@Slf4j
public class CodeTracker extends BaseData{

    private CodeTrackerDao codeTrackerDao;

    /**
     *  开发者新增代码逻辑行
     */
    private int developerAddStatement;
    /**
     *  开发者更改逻辑行
     */
    private int developerChangeStatement;
    /**
     *  开发者删除逻辑行
     */
    private int developerDeleteStatement;
    /**
     *  开发者总共逻辑行（add+del+change）
     */
    private int developerTotalStatement;
    /**
     *  开发者存活逻辑行
     */
    private int developerValidStatement;
    /**
     *  团队新增代码逻辑行
     */
    private int totalAddStatement;
    /**
     *  团队更改逻辑行
     */
    private int totalChangeStatement;
    /**
     *  团队删除逻辑行
     */
    private int totalDeleteStatement;
    /**
     *  团队总共逻辑行（add+del+change）
     */
    private int totalStatement;
    /**
     *  团队存活逻辑行
     */
    private int totalValidStatement;


    public CodeTracker(Query query) {
        super(query);
    }

    @Override
    public void dataInjection() {
        log.info("{} : in repo : {} start to get CodeTrackerInfo",query.getDeveloper(),query.getRepoUuidList());
        /**
         * key : getDeveloperStatementInfo
         * value {@link CodeTrackerDao}
         */
        Map<String,Object> developerStatementMap = codeTrackerDao.getDeveloperStatementInfo(query);
        developerAddStatement = (int) developerStatementMap.get("developerAddStatement");
        developerChangeStatement = (int) developerStatementMap.get("developerChangeStatement");
        developerDeleteStatement =(int) developerStatementMap.get("developerDeleteStatement");
        developerTotalStatement = (int) developerStatementMap.get("developerTotalStatement");
        developerValidStatement = (int) developerStatementMap.get("developerValidStatement");
        /**
         * key : getAllDeveloperStatementInfo
         * value {@link CodeTrackerDao}
         */
        Map<String,Object> allDeveloperStatementMap = codeTrackerDao.getAllDeveloperStatementInfo(query);
        totalAddStatement = (int) allDeveloperStatementMap.get("totalAddStatement");
        totalChangeStatement = (int) allDeveloperStatementMap.get("totalChangeStatement");
        totalDeleteStatement = (int) allDeveloperStatementMap.get("totalDeleteStatement");
        totalStatement = (int) allDeveloperStatementMap.get("totalStatement");
        totalValidStatement = (int) allDeveloperStatementMap.get("totalValidStatement");

        log.info("get CodeTrackerInfo success");
        
    }

    @Autowired
    public void setCodeTrackerDao(CodeTrackerDao codeTrackerDao) {this.codeTrackerDao=codeTrackerDao;}

}