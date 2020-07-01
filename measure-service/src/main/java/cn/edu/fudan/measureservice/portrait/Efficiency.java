package cn.edu.fudan.measureservice.portrait;


import lombok.*;

/**
 * description: 开发人员效率 某段时间内完成的工作量
 *
 * @author fancying
 * create: 2020-05-18 21:40
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Efficiency implements Formula{


    // 下面是基础数据 工作量
    @Setter
    private int developerNumber;
    @Setter
    private int developerCommitCount;
    @Setter
    private int totalCommitCount;
    @Setter
    private int developerLOC;
    @Setter
    private int totalLOC;
    @Setter
    private int developerAddStatement;
    @Setter
    private int totalAddStatement;
    @Setter
    private int developerDelStatement;
    @Setter
    private int totalDelStatement;
    @Setter
    private int developerValidLine;
    @Setter
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


    private double level = defaultScore;


    /**
     * todo 具体的计算方式
     * @return
     */
    @Override
    public double cal() {
        double efficiencyValue = 0.25*commitFrequency + 0.25*workLoad + 0.25*newLogicLine + 0.25*delLogicLine;

        if (efficiencyValue >= 0 && efficiencyValue <= 0.5/developerNumber){
            return 1;
        }
        if (efficiencyValue > 0.5/developerNumber && efficiencyValue <= 0.67/developerNumber){
            return 2;
        }
        if (efficiencyValue > 0.67/developerNumber && efficiencyValue <= 1.0/developerNumber){
            return 3;
        }
        if (efficiencyValue > 1.0/developerNumber && efficiencyValue <= 1.5/developerNumber){
            return 4;
        }
        if (efficiencyValue > 1.5/developerNumber){
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

    public double getCommitFrequency() {
        if (!((Double)defaultScore).equals(commitFrequency)) {
            return commitFrequency;
        }
        //  具体的计算方式
        commitFrequency = (double)developerCommitCount / totalCommitCount;

        return commitFrequency;
    }

    public double getWorkLoad() {
        if (!((Double)defaultScore).equals(workLoad)) {
            return workLoad;
        }
        // 具体的计算方式
        workLoad = (double)developerLOC / totalLOC;
        return workLoad;
    }

    public double getNewLogicLine() {
        if (!((Double)defaultScore).equals(newLogicLine)) {
            return newLogicLine;
        }
        //  具体的计算方式
        newLogicLine = developerAddStatement*(1.0)/totalAddStatement;
        return newLogicLine;
    }

    public double getDelLogicLine() {
        if (!((Double)defaultScore).equals(delLogicLine)) {
            return delLogicLine;
        }
        // 具体的计算方式
        delLogicLine = (totalDelStatement == 0) ? -1 : developerDelStatement*(1.0)/totalDelStatement;
        return delLogicLine;
    }

    public double getValidStatement() {
        if (!((Double)defaultScore).equals(validStatement)) {
            return validStatement;
        }
        //具体的计算方式
        validStatement = (totalValidLine == 0) ? -1 : developerValidLine*(1.0)/totalValidLine;
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
        if (defaultScore != newLogicLineLevel) {
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
        if (defaultScore != delLogicLineLevel) {
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
        if (defaultScore != validStatementLevel) {
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