package cn.edu.fudan.measureservice.portrait2;


import cn.edu.fudan.measureservice.portrait.Formula;
import lombok.*;

import java.io.Serializable;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-18 21:40
 **/
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quality implements Formula, Serializable {
    // 基础数据
    private int developerStandardIssueCount;//个人引入规范类问题数
    private int totalStandardIssueCount;//团队引入规范类问题数
    private int developerNewIssueCount;//个人引入问题数
    private int totalNewIssueCount;//团队引入问题数
    private int developerLOC;//个人addLines+delLines
    private int developerValidCommitCount;//个人提交的commit总数(不包含merge)
    private int developerJiraCount;//个人提交的commit当中 关联有jira的个数
    private int developerJiraBugCount;//个人jira任务中属于bug类型的数量
    private int totalJiraBugCount;//团队jira任务中属于bug类型的数量

    // 以下各个评分由具体的公式计算出来,在getter方法中实现

    private static double defaultScore = 0;


    private double codeStandard = defaultScore;//代码规范性：个人规范类问题数量/团队规范类问题数量
    private double commitStandard = defaultScore;//提交规范性：个人jira关联数/个人提交数
    private double jiraBugRate = defaultScore;//个人bug/团队bug
    private double issueRate = defaultScore;//个人引入问题/团队引入问题
    private double issueDensity = defaultScore;//平均每100行代码，产生的问题数：个人新增问题数*100/（个人总新增代码物理行数+个人总删除代码物理行数）

    private double codeStandardLevel = defaultScore;
    private double commitStandardLevel = defaultScore;
    private double jiraBugRateLevel = defaultScore;
    private double issueRateLevel = defaultScore;
    private double issueDensityLevel = defaultScore;

    private double levelScore = defaultScore;
    private double level = defaultScore;

    @Override
    public double cal() {
        levelScore = getLevelScore();
        if (levelScore >= 0 && levelScore <= 0.002){
            return 5;
        }
        if (levelScore > 0.002 && levelScore <= 0.004){
            return 4;
        }
        if (levelScore > 0.004&& levelScore <= 0.007){
            return 3;
        }
        if (levelScore > 0.007 && levelScore <= 1.0){
            return 2;
        }
        if (levelScore > 1.0){
            return 1;
        }
        return 0;
    }

    /**
     * getter
     */

    public double getLevel(){
        if (level != defaultScore) {
            return level;
        }
        level = cal();
        return level;
    }

    public double getLevelScore() {
        if (levelScore != defaultScore) {
            return levelScore;
        }
        levelScore = issueDensity;
        return levelScore;
    }

    public double getCodeStandard () {
            if (defaultScore != codeStandard) {
                return codeStandard;
            }
            //  具体的计算方式
            if (totalStandardIssueCount != 0) {
                codeStandard = developerStandardIssueCount * (1.0) / totalStandardIssueCount;
                return codeStandard;
            }
            return codeStandard;
        }

    public double getCommitStandard () {
            if (defaultScore != commitStandard) {
                return commitStandard;
            }
            //  具体的计算方式
            if (developerValidCommitCount != 0) {
                commitStandard = developerJiraCount * (1.0) / developerValidCommitCount;
                return commitStandard;
            }
            return commitStandard;
        }

    public double getJiraBugRate () {
            if (defaultScore != jiraBugRate) {
                return jiraBugRate;
            }
            //  具体的计算方式
            if (totalJiraBugCount != 0) {
                jiraBugRate = developerJiraBugCount * (1.0) / totalJiraBugCount;
                return jiraBugRate;
            }
            return jiraBugRate;
        }

    public double getIssueRate () {
            if (defaultScore != issueRate) {
                return issueRate;
            }
            //  具体的计算方式
            if (totalNewIssueCount != 0) {
                issueRate = developerNewIssueCount * (1.0) / totalNewIssueCount;
                return issueRate;
            }
            return issueRate;
        }

    public double getIssueDensity () {
            if (defaultScore != issueDensity) {
                return issueDensity;
            }
            //  具体的计算方式
            if (developerLOC != 0) {
                issueDensity = developerNewIssueCount * 100.0 / developerLOC;
                return issueDensity;
            }
            return issueDensity;
        }

}