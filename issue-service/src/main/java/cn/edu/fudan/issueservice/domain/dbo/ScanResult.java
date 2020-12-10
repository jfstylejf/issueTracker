package cn.edu.fudan.issueservice.domain.dbo;

import lombok.Data;

import java.util.Date;

/**
 * @author WZY
 * @version 1.0
 **/
@Data
public class ScanResult {

    private int id;
    private String category;
    private String repo_id;
    private Date scan_date;
    private String commit_id;
    private Date commit_date;
    private String developer;
    private int new_count;
    private int eliminated_count;
    private int remaining_count;

    public ScanResult() {
    }

    public ScanResult(String category, String repo_id, Date scan_date, String commit_id, Date commit_date, String developer, int new_count, int eliminated_count, int remaining_count) {
        this.category = category;
        this.repo_id = repo_id;
        this.scan_date = scan_date;
        this.commit_id = commit_id;
        this.commit_date = commit_date;
        this.developer = developer;
        this.new_count = new_count;
        this.eliminated_count = eliminated_count;
        this.remaining_count = remaining_count;
    }

}
