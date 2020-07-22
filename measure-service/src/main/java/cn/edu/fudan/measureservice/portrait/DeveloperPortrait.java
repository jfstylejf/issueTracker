package cn.edu.fudan.measureservice.portrait;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
@AllArgsConstructor
public class DeveloperPortrait implements Serializable {

    private LocalDate firstCommitDate;
    private int totalLine;
    private int dayAverageLine;
    private int totalCommitCount;
    private String developerName;
    private String developerType;


    private List<DeveloperMetrics> developerMetricsList;

    private static double defaultLevel = -1;
    private double level = defaultLevel;
    private double value = defaultLevel;
    private double quality = defaultLevel;
    private double efficiency = defaultLevel;

    public DeveloperPortrait(LocalDate firstCommitDate, int totalLine, int dayAverageLine, int totalCommitCount, String developerName, String developerType, List<DeveloperMetrics> developerMetricsList) {
        this.firstCommitDate = firstCommitDate;
        this.totalLine = totalLine;
        this.dayAverageLine = dayAverageLine;
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
        }
        efficiency = totalLevel*1.0/developerMetricsList.size();
        return efficiency;
    }

}