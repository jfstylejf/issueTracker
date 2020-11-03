package cn.edu.fudan.measureservice.portrait;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * description: 记录开发人员整体的画像
 *
 * @author fancying
 * create: 2020-06-28 22:35
 **/
@Data
@Slf4j
@AllArgsConstructor
public class DeveloperPortrait implements Serializable {

    private String firstCommitDate;
    private int totalStatement;
    private int dayAverageStatement;
    private int totalCommitCount;
    private String developerName;
    private String developerType;


    private List<DeveloperMetrics> developerMetricsList;


    private static double defaultLevel = -1;
    private double level = defaultLevel;
    private double value = defaultLevel;
    private double quality = defaultLevel;
    private double efficiency = defaultLevel;

    private double commitFrequencyLevel = defaultLevel;
    private double workLoadLevel = defaultLevel;
    private double newLogicLineLevel = defaultLevel;
    private double delLogicLineLevel = defaultLevel;
    private double validStatementLevel = defaultLevel;

    private double standardLevel = defaultLevel;
    private double securityLevel = defaultLevel;
    private double issueRateLevel = defaultLevel;
    private double issueDensityLevel = defaultLevel;

    private double nonRepetitiveCodeRateLevel = defaultLevel;
    private double nonSelfRepetitiveCodeRateLevel = defaultLevel;
    private double eliminateDuplicateCodeRateLevel = defaultLevel;
    private double oldCodeModificationLevel = defaultLevel;



    public DeveloperPortrait(String firstCommitDate, int totalStatement, int dayAverageStatement, int totalCommitCount, String developerName, String developerType, List<DeveloperMetrics> developerMetricsList) {
        this.firstCommitDate = firstCommitDate;
        this.totalStatement = totalStatement;
        this.dayAverageStatement = dayAverageStatement;
        this.totalCommitCount = totalCommitCount;
        this.developerName = developerName;
        this.developerType = developerType;
        this.developerMetricsList = developerMetricsList;
    }

    public double getLevel() {
        if (defaultLevel != level) {
            return level;
        }
        //  具体的计算方式
        level = (getValue() + getQuality() + getEfficiency()) / 3;
        return level;
    }

    public double getValue() {
        if (defaultLevel != value) {
            return value;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getCompetence().getLevel();
            log.info(developerMetrics.toString());
            log.info("" + totalLevel);
        }
        value = totalLevel*1.0/developerMetricsList.size();
        return value;
    }

    public double getQuality() {
        if (defaultLevel != quality) {
            return quality;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getQuality().getLevel();
            log.info(developerMetrics.toString());
            log.info("" + totalLevel);
        }
        quality = totalLevel*1.0/developerMetricsList.size();
        return quality;
    }

    public double getEfficiency() {
        if (defaultLevel != efficiency) {
            return efficiency;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getEfficiency().getLevel();
            log.info(developerMetrics.toString());
            log.info("" + totalLevel);
        }
        efficiency = totalLevel*1.0/developerMetricsList.size();
        return efficiency;
    }

    public double getCommitFrequencyLevel() {
        if (defaultLevel != commitFrequencyLevel) {
            return commitFrequencyLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getEfficiency().getCommitFrequencyLevel();
        }
        commitFrequencyLevel = totalLevel*1.0/developerMetricsList.size();
        return commitFrequencyLevel;
    }

    public double getWorkLoadLevel() {
        if (defaultLevel != workLoadLevel) {
            return workLoadLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getEfficiency().getWorkLoadLevel();
        }
        workLoadLevel = totalLevel*1.0/developerMetricsList.size();
        return workLoadLevel;
    }

    public double getNewLogicLineLevel() {
        if (defaultLevel != newLogicLineLevel) {
            return newLogicLineLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getEfficiency().getNewLogicLineLevel();
        }
        newLogicLineLevel = totalLevel*1.0/developerMetricsList.size();
        return newLogicLineLevel;
    }

    public double getDelLogicLineLevel() {
        if (defaultLevel != delLogicLineLevel) {
            return delLogicLineLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getEfficiency().getDelLogicLineLevel();
        }
        delLogicLineLevel = totalLevel*1.0/developerMetricsList.size();
        return delLogicLineLevel;
    }

    public double getValidStatementLevel() {
        if (defaultLevel != validStatementLevel) {
            return validStatementLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getEfficiency().getValidStatementLevel();
        }
        validStatementLevel = totalLevel*1.0/developerMetricsList.size();
        return validStatementLevel;
    }

    public double getStandardLevel() {
        if (defaultLevel != standardLevel) {
            return standardLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getQuality().getStandardLevel();
        }
        standardLevel = totalLevel*1.0/developerMetricsList.size();
        return standardLevel;
    }

    public double getSecurityLevel() {
        if (defaultLevel != securityLevel) {
            return securityLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getQuality().getSecurityLevel();
        }
        securityLevel = totalLevel*1.0/developerMetricsList.size();
        return securityLevel;
    }

    public double getIssueRateLevel() {
        if (defaultLevel != issueRateLevel) {
            return issueRateLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getQuality().getIssueRateLevel();
        }
        issueRateLevel = totalLevel*1.0/developerMetricsList.size();
        return issueRateLevel;
    }

    public double getIssueDensityLevel() {
        if (defaultLevel != issueDensityLevel) {
            return issueDensityLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getQuality().getIssueDensityLevel();
        }
        issueDensityLevel = totalLevel*1.0/developerMetricsList.size();
        return issueDensityLevel;
    }

    public double getNonRepetitiveCodeRateLevel() {
        if (defaultLevel != nonRepetitiveCodeRateLevel) {
            return nonRepetitiveCodeRateLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getCompetence().getNonRepetitiveCodeRateLevel();
        }
        nonRepetitiveCodeRateLevel = totalLevel*1.0/developerMetricsList.size();
        return nonRepetitiveCodeRateLevel;
    }

    public double getNonSelfRepetitiveCodeRateLevel() {
        if (defaultLevel != nonSelfRepetitiveCodeRateLevel) {
            return nonSelfRepetitiveCodeRateLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getCompetence().getNonSelfRepetitiveCodeRateLevel();
        }
        nonSelfRepetitiveCodeRateLevel = totalLevel*1.0/developerMetricsList.size();
        return nonSelfRepetitiveCodeRateLevel;
    }

    public double getEliminateDuplicateCodeRateLevel() {
        if (defaultLevel != eliminateDuplicateCodeRateLevel) {
            return eliminateDuplicateCodeRateLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getCompetence().getEliminateDuplicateCodeRateLevel();
        }
        eliminateDuplicateCodeRateLevel = totalLevel*1.0/developerMetricsList.size();
        return eliminateDuplicateCodeRateLevel;
    }

    public double getOldCodeModificationLevel() {
        if (defaultLevel != oldCodeModificationLevel) {
            return oldCodeModificationLevel;
        }
        //  具体的计算方式
        int totalLevel = 0;
        for (int i = 0; i < developerMetricsList.size(); i++){
            DeveloperMetrics developerMetrics = developerMetricsList.get(i);
            totalLevel += developerMetrics.getCompetence().getOldCodeModificationLevel();
        }
        oldCodeModificationLevel = totalLevel*1.0/developerMetricsList.size();
        return oldCodeModificationLevel;
    }
}