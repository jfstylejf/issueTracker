package cn.edu.fudan.issueservice.domain.statistics;

import lombok.*;

/**
 * @author fancying
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperQuality extends Quality {

    private String author;
    private String email;
    private int commitCount;
    private int addLines;
    private int delLines;
    private int changedFiles;

    public void setEliminateIssueQualityThroughCalculate(double eliminateIssues) {

        double totalChangedLines = addLines + delLines;
        //fixme 用于上汽演示暂做更改 500
        if(totalChangedLines == 0){
            super.setEliminateIssueQuality(500);
        }
        if (totalChangedLines == 0) {
            totalChangedLines = -1;
        }
        super.setEliminateIssueQuality( eliminateIssues * 100.0 / totalChangedLines);
    }

    public void setAddIssueQualityThroughCalculate(double newIssues) {
        double totalChangedLines = addLines + delLines;
        //fixme 用于上汽演示暂做更改 500
        if(totalChangedLines ==0){
            super.setAddIssueQuality(500);
        }
        if (totalChangedLines == 0) {
            totalChangedLines = -1;
        }
        super.setAddIssueQuality(newIssues * 100.0 / totalChangedLines);
    }

    @Override
    public int hashCode() {
        return author.hashCode();
    }

    @Override
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof DeveloperQuality) {
            DeveloperQuality anotherDeveloperQuality = (DeveloperQuality)anObject;
            return this.author.equals(anotherDeveloperQuality.getAuthor());
        }
        return false;
    }
}
