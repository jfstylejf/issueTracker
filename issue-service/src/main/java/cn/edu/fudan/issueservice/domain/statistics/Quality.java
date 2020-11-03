package cn.edu.fudan.issueservice.domain.statistics;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quality {

    private int newIssues;
    private int eliminateIssues;
    private double eliminateIssueQuality;
    private double addIssueQuality;

    public void setEliminateIssueQualityThroughCalculate(double eliminateIssues, double totalChangedLines) {
        //用于上汽演示暂做更改 500
        if(totalChangedLines ==0){
            this.eliminateIssueQuality = 500;
        }
        if (totalChangedLines == 0) {
            totalChangedLines = -1;
        }
        this.eliminateIssueQuality = eliminateIssues*100.0/totalChangedLines;
    }

    public void setAddIssueQualityThroughCalculate(double newIssues, double totalChangedLines) {
        //用于上汽演示暂做更改 500
        if(totalChangedLines ==0){
            this.addIssueQuality = 500;
        }
        if (totalChangedLines == 0) {
            totalChangedLines = -1;
        }
        this.addIssueQuality = newIssues*100.0/totalChangedLines;
    }
}
