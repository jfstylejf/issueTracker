package cn.edu.fudan.measureservice.portrait;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class EfficiencyOld implements Formula, Serializable {


    // 下面是基础数据 工作量
    private int developerNumber;
    private int developerCommitCount;
    private int totalCommitCount;
    private int developerLOC;
    private int totalLOC;
    private int developerAddStatement;
    private int totalAddStatement;
    private int developerDelStatement;
    private int totalDelStatement;
    private int developerValidLine;
    private int totalValidLine;


    // 以下各个评分由具体的公式计算出来,在getter方法中实现

    private static double defaultScore = 0;
    /**
     * 提交频率评分
     * todo 需要考虑排除节假日计算提交频率
     */
    private double commitFrequency = defaultScore;
    private double workLoad = defaultScore;
    private double newLogicLine = defaultScore;
    private double delLogicLine = defaultScore;
    private double validStatement = defaultScore;
    private double commitFrequencyLevel = defaultScore;
    private double workLoadLevel = defaultScore;
    private double newLogicLineLevel = defaultScore;
    private double delLogicLineLevel = defaultScore;
    private double validStatementLevel = defaultScore;

    private double levelScore = defaultScore;
    private double level = defaultScore;


    /**
     * todo 具体的计算方式
     * @return
     */
    @Override
    public double cal() {
        levelScore = getLevelScore();

        if (levelScore >= 0 && levelScore <= 0.5/developerNumber){
            return 1;
        }
        if (levelScore > 0.5/developerNumber && levelScore <= 1.0/developerNumber){
            return 2;
        }
        if (levelScore > 1.0/developerNumber && levelScore <= 1.5/developerNumber){
            return 3;
        }
        if (levelScore > 1.5/developerNumber && levelScore <= 2.0/developerNumber){
            return 4;
        }
        if (levelScore > 2.0/developerNumber){
            return 5;
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
        levelScore = 0.25*commitFrequency + 0.25*workLoad + 0.25*newLogicLine + 0.25*delLogicLine;
        return levelScore;
    }

    public double getCommitFrequency() {
        if (!((Double)defaultScore).equals(commitFrequency)) {
            return commitFrequency;
        }
        //  具体的计算方式
        commitFrequency = (totalCommitCount == 0) ? 0 : (double)developerCommitCount / totalCommitCount;

        return commitFrequency;
    }

    public double getWorkLoad() {
        if (!((Double)defaultScore).equals(workLoad)) {
            return workLoad;
        }
        // 具体的计算方式
        workLoad = (totalLOC == 0) ? 0 : (double)developerLOC / totalLOC;
        return workLoad;
    }

    public double getNewLogicLine() {
        if (!((Double)defaultScore).equals(newLogicLine)) {
            return newLogicLine;
        }
        //  具体的计算方式
        newLogicLine = (totalAddStatement == 0) ? 0 : developerAddStatement*(1.0)/totalAddStatement;
        return newLogicLine;
    }

    public double getDelLogicLine() {
        if (!((Double)defaultScore).equals(delLogicLine)) {
            return delLogicLine;
        }
        // 具体的计算方式
        delLogicLine = (totalDelStatement == 0) ? 0 : developerDelStatement*(1.0)/totalDelStatement;
        return delLogicLine;
    }

    public double getValidStatement() {
        if (!((Double)defaultScore).equals(validStatement)) {
            return validStatement;
        }
        //具体的计算方式
        validStatement = (totalValidLine == 0) ? 0 : developerValidLine*(1.0)/totalValidLine;
        return validStatement;
    }

    public double getCommitFrequencyLevel() {
        if (defaultScore != commitFrequencyLevel) {
            return commitFrequencyLevel;
        }
        //具体的计算方式
        if (commitFrequency >= 0 && commitFrequency <= 0.5/developerNumber){
            commitFrequencyLevel = 1;
            return commitFrequencyLevel;
        }
        if (commitFrequency > 0.5/developerNumber && commitFrequency <= 0.67/developerNumber){
            commitFrequencyLevel = 2;
            return commitFrequencyLevel;
        }
        if (commitFrequency > 0.67/developerNumber && commitFrequency <= 1.0/developerNumber){
            commitFrequencyLevel = 3;
            return commitFrequencyLevel;
        }
        if (commitFrequency > 1.0/developerNumber && commitFrequency <= 1.5/developerNumber){
            commitFrequencyLevel = 4;
            return commitFrequencyLevel;
        }
        if (commitFrequency > 1.5/developerNumber){
            commitFrequencyLevel = 5;
            return commitFrequencyLevel;
        }
        return commitFrequencyLevel;
    }

    public double getWorkLoadLevel() {
        if (defaultScore != workLoadLevel) {
            return workLoadLevel;
        }
        //具体的计算方式
        if (workLoad >= 0 && workLoad <= 0.5/developerNumber){
            workLoadLevel = 1;
            return workLoadLevel;
        }
        if (workLoad > 0.5/developerNumber && workLoad <= 0.67/developerNumber){
            workLoadLevel = 2;
            return workLoadLevel;
        }
        if (workLoad > 0.67/developerNumber && workLoad <= 1.0/developerNumber){
            workLoadLevel = 3;
            return workLoadLevel;
        }
        if (workLoad > 1.0/developerNumber && workLoad <= 1.5/developerNumber){
            workLoadLevel = 4;
            return workLoadLevel;
        }
        if (workLoad > 1.5/developerNumber){
            workLoadLevel = 5;
            return workLoadLevel;
        }
        return workLoadLevel;
    }

    public double getNewLogicLineLevel() {
        if (!((Double)defaultScore).equals(newLogicLineLevel)) {
            return newLogicLineLevel;
        }
        //具体的计算方式
        if (newLogicLine >= 0 && newLogicLine <= 0.5/developerNumber){
            newLogicLineLevel = 1;
            return newLogicLineLevel;
        }
        if (newLogicLine > 0.5/developerNumber && newLogicLine <= 0.67/developerNumber){
            newLogicLineLevel = 2;
            return newLogicLineLevel;
        }
        if (newLogicLine > 0.67/developerNumber && newLogicLine <= 1.0/developerNumber){
            newLogicLineLevel = 3;
            return newLogicLineLevel;
        }
        if (newLogicLine > 1.0/developerNumber && newLogicLine <= 1.5/developerNumber){
            newLogicLineLevel = 4;
            return newLogicLineLevel;
        }
        if (newLogicLine > 1.5/developerNumber){
            newLogicLineLevel = 5;
            return newLogicLineLevel;
        }
        return newLogicLineLevel;
    }

    public double getDelLogicLineLevel() {
        if (!((Double)defaultScore).equals(delLogicLineLevel)) {
            return delLogicLineLevel;
        }
        //具体的计算方式
        if (delLogicLine >= 0 && delLogicLine <= 0.5/developerNumber){
            delLogicLineLevel = 1;
            return delLogicLineLevel;
        }
        if (delLogicLine > 0.5/developerNumber && delLogicLine <= 0.67/developerNumber){
            delLogicLineLevel = 2;
            return delLogicLineLevel;
        }
        if (delLogicLine > 0.67/developerNumber && delLogicLine <= 1.0/developerNumber){
            delLogicLineLevel = 3;
            return delLogicLineLevel;
        }
        if (delLogicLine > 1.0/developerNumber && delLogicLine <= 1.5/developerNumber){
            delLogicLineLevel = 4;
            return delLogicLineLevel;
        }
        if (delLogicLine > 1.5/developerNumber){
            delLogicLineLevel = 5;
            return delLogicLineLevel;
        }
        return delLogicLineLevel;
    }

    public double getValidStatementLevel() {
        if (!((Double)defaultScore).equals(validStatementLevel)) {
            return validStatementLevel;
        }
        //具体的计算方式
        if (validStatement >= 0 && validStatement <= 0.5/developerNumber){
            validStatementLevel = 1;
            return validStatementLevel;
        }
        if (validStatement > 0.5/developerNumber && validStatement <= 0.67/developerNumber){
            validStatementLevel = 2;
            return validStatementLevel;
        }
        if (validStatement > 0.67/developerNumber && validStatement <= 1.0/developerNumber){
            validStatementLevel = 3;
            return validStatementLevel;
        }
        if (validStatement > 1.0/developerNumber && validStatement <= 1.5/developerNumber){
            validStatementLevel = 4;
            return validStatementLevel;
        }
        if (validStatement > 1.5/developerNumber){
            validStatementLevel = 5;
            return validStatementLevel;
        }
        return validStatementLevel;
    }
}