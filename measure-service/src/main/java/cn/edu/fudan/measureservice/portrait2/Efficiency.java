package cn.edu.fudan.measureservice.portrait2;


import cn.edu.fudan.measureservice.portrait.Formula;
import lombok.*;

import java.io.Serializable;

/**
 * description: 开发人员效率 某段时间内完成的工作量
 *
 * @author fancying
 * create: 2020-05-18 21:40
 **/
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Efficiency implements Formula, Serializable {

    // 以下各个评分由具体的公式计算出来,在getter方法中实现

    private static double defaultScore = 0;

    private int jiraBug;
    private int jiraFeature;
    private int solvedSonarIssue;
    private int days;
    // 解决jira任务提交的commit次数
    private int commitNum;
    //完成的jira任务数量
    private int completedJiraNum;

    private double jiraBugPerDay = defaultScore;
    private double jiraFeaturePerDay = defaultScore;
    private double solvedSonarIssuePerDay = defaultScore;
    private double commitPerJira = defaultScore;

    private double jiraBugPerDayLevel = defaultScore;
    private double jiraFeaturePerDayLevel = defaultScore;
    private double solvedSonarIssuePerDayLevel = defaultScore;
    private double commitPerJiraLevel = defaultScore;

    private double levelScore = defaultScore;
    private double level = defaultScore;


    /**
     * todo 具体的计算方式
     * @return
     */
    @Override
    public double cal() {
        levelScore = getLevelScore();
        if (levelScore == 0){
            return 3;
        }
        if (levelScore > 0 && levelScore <= 2){
            return 5;
        }
        if (levelScore > 2 && levelScore <= 4){
            return 4;
        }
        if (levelScore > 4){
            return 3;
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
        levelScore = getCommitPerJira();
        return levelScore;
    }

    public double getJiraBugPerDay() {
        if(jiraBugPerDay!=defaultScore){
            return jiraBugPerDay;
        }
        if (days!=0){
            jiraBugPerDay = jiraBug*1.0/days;
            return jiraBugPerDay;
        }
        return jiraBugPerDay;
    }

    public double getJiraFeaturePerDay() {
        if(jiraFeaturePerDay!=defaultScore){
            return jiraFeaturePerDay;
        }
        if (days!=0){
            jiraFeaturePerDay = jiraFeature*1.0/days;
            return jiraFeaturePerDay;
        }
        return jiraFeaturePerDay;
    }

    public double getSolvedSonarIssuePerDay() {
        if(solvedSonarIssuePerDay!=defaultScore){
            return solvedSonarIssuePerDay;
        }
        if (days!=0){
            solvedSonarIssuePerDay = solvedSonarIssue*1.0/days;
            return solvedSonarIssuePerDay;
        }
        return solvedSonarIssuePerDay;
    }

    public double getCommitPerJira() {
        if(commitPerJira!=defaultScore){
            return commitPerJira;
        }
        if (completedJiraNum!=0){
            commitPerJira = commitNum*1.0/completedJiraNum;
            return commitPerJira;
        }else {
            return 0;
        }
    }


    public double getJiraBugPerDayLevel() {
        return jiraBugPerDayLevel;
    }

    public double getJiraFeaturePerDayLevel() {
        return jiraFeaturePerDayLevel;
    }

    public double getSolvedSonarIssuePerDayLevel() {
        return solvedSonarIssuePerDayLevel;
    }

}