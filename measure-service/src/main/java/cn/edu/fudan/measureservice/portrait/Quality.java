package cn.edu.fudan.measureservice.portrait;


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
    @Setter
    private int developerNumber;
    @Setter
    private int developerStandardIssueCount;
    @Setter
    private int developerSecurityIssueCount;
    @Setter
    private int totalIssueCount;
    @Setter
    private int developerNewIssueCount;
    @Setter
    private int totalNewIssueCount;
    @Setter
    private int totalLOC;

    // 以下各个评分由具体的公式计算出来,在getter方法中实现

    private static double defaultScore = 0;

    private double standard = defaultScore;
    private double security = defaultScore;
    private double issueRate = defaultScore;
    /**
     * 平均每100行代码，产生的缺陷数
     */
    private double issueDensity = defaultScore;
    private double standardLevel = defaultScore;
    private double securityLevel = defaultScore;
    private double issueRateLevel = defaultScore;
    private double issueDensityLevel = defaultScore;
    private double levelScore = defaultScore;
    private double level = defaultScore;



    @Override
    public double cal() {
        levelScore = getLevelScore();
        if (levelScore >= 0 && levelScore <= 0.5/developerNumber){
            return 5;
        }
        if (levelScore > 0.5/developerNumber && levelScore <= 1.0/developerNumber){
            return 4;
        }
        if (levelScore > 1.0/developerNumber && levelScore <= 2.0/developerNumber){
            return 3;
        }
        if (levelScore > 2.0/developerNumber && levelScore <= 4.0/developerNumber){
            return 2;
        }
        if (levelScore > 4.0/developerNumber){
            return 1;
        }
        return 0;
    }

    /**
     * getter
     */

    public double getLevel(){
        if (((Double)defaultScore).equals(level)) {
            level = cal();
        }
        return level;
    }

    public double getLevelScore() {
        if (((Double)defaultScore).equals(levelScore)) {
            levelScore = 0.25*standard + 0.25*security + 0.25*issueRate + 0.25*issueDensity;
        }
        return levelScore;
    }

    public double getStandard() {
        if (defaultScore != standard) {
            return standard;
        }
        //  具体的计算方式
        if (totalIssueCount != 0){
            standard = developerStandardIssueCount*(1.0)/totalIssueCount;
            return standard;
        }
        return standard;
    }

    public double getSecurity() {
        if (defaultScore != security) {
            return security;
        }
        //  具体的计算方式
        if (totalIssueCount != 0){
            security = developerSecurityIssueCount*(1.0)/totalIssueCount;
            return security;
        }
        return security;
    }

    public double getIssueRate() {
        if (defaultScore != issueRate) {
            return issueRate;
        }
        //  具体的计算方式
        if (totalIssueCount != 0){
            issueRate = developerNewIssueCount*(1.0)/totalNewIssueCount;
            return issueRate;
        }
        return issueRate;
    }

    public double getIssueDensity() {
        if (defaultScore != issueDensity) {
            return issueDensity;
        }
        //  具体的计算方式
        if (totalLOC != 0){
            issueDensity = developerNewIssueCount*100.0/totalLOC;
            return issueDensity;
        }
        return issueDensity;
    }

    public double getStandardLevel() {
        if (defaultScore != standardLevel) {
            return standardLevel;
        }
        //具体的计算方式
        if (standard >= 0 && standard <= 0.5/developerNumber){
            standardLevel = 5;
            return standardLevel;
        }
        if (standard > 0.5/developerNumber && standard <= 0.67/developerNumber){
            standardLevel = 4;
            return standardLevel;
        }
        if (standard > 0.67/developerNumber && standard <= 1.0/developerNumber){
            standardLevel = 3;
            return standardLevel;
        }
        if (standard > 1.0/developerNumber && standard <= 1.5/developerNumber){
            standardLevel = 2;
            return standardLevel;
        }
        if (standard > 1.5/developerNumber){
            standardLevel = 1;
            return standardLevel;
        }
        return standardLevel;
    }

    public double getSecurityLevel() {
        if (defaultScore != securityLevel) {
            return securityLevel;
        }
        //具体的计算方式
        if (security >= 0 && security <= 0.5/developerNumber){
            securityLevel = 5;
            return securityLevel;
        }
        if (security > 0.5/developerNumber && security <= 0.67/developerNumber){
            securityLevel = 4;
            return securityLevel;
        }
        if (security > 0.67/developerNumber && security <= 1.0/developerNumber){
            securityLevel = 3;
            return securityLevel;
        }
        if (security > 1.0/developerNumber && security <= 1.5/developerNumber){
            securityLevel = 2;
            return securityLevel;
        }
        if (security > 1.5/developerNumber){
            securityLevel = 1;
            return securityLevel;
        }
        return securityLevel;
    }

    public double getIssueRateLevel() {
        if (defaultScore != issueRateLevel) {
            return issueRateLevel;
        }
        //具体的计算方式
        if (issueRate >= 0 && issueRate <= 0.5/developerNumber){
            issueRateLevel = 5;
            return issueRateLevel;
        }
        if (issueRate > 0.5/developerNumber && issueRate <= 0.67/developerNumber){
            issueRateLevel = 4;
            return issueRateLevel;
        }
        if (issueRate > 0.67/developerNumber && issueRate <= 1.0/developerNumber){
            issueRateLevel = 3;
            return issueRateLevel;
        }
        if (issueRate > 1.0/developerNumber && issueRate <= 1.5/developerNumber){
            issueRateLevel = 2;
            return issueRateLevel;
        }
        if (issueRate > 1.5/developerNumber){
            issueRateLevel = 1;
            return issueRateLevel;
        }
        return issueRateLevel;
    }

    public double getIssueDensityLevel() {
        if (defaultScore != issueDensityLevel) {
            return issueDensityLevel;
        }
        //具体的计算方式
        if (1/issueDensity >= 0 && 1/issueDensity <= 100){
            issueDensityLevel = 1;
            return issueDensityLevel;
        }
        if (1/issueDensity > 100 && 1/issueDensity <= 150){
            issueDensityLevel = 2;
            return issueDensityLevel;
        }
        if (issueDensity > 150 && issueDensity <= 200){
            issueDensityLevel = 3;
            return issueDensityLevel;
        }
        if (1/issueDensity > 200 && 1/issueDensity <= 300){
            issueDensityLevel = 4;
            return issueDensityLevel;
        }
        if (1/issueDensity > 300){
            issueDensityLevel = 5;
            return issueDensityLevel;
        }
        return issueDensityLevel;
    }
}