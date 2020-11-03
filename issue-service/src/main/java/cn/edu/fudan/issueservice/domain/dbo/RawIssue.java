package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.dto.RawIssueMatchResult;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * fixme 修改RawIssue中不符合规范的field命名
 * @author fancying
 */
@Data
public class RawIssue {

    private String uuid;
    private String type;
    private String tool;
    private String detail;
    private String file_name;
    private String scan_id;
    private String issue_id;
    private String commit_id;
    private String repo_id;
    private int code_lines;
    private String status;
    private List<Location> locations;
    private Date commit_time;
    private String developer_email;
    private Issue issue;
    /**
     * 开发者聚合后的唯一姓名
     */
    private String developerName;

    private List<RawIssueMatchResult> rawIssueMatchResults = new ArrayList<>(0);
    private int matchResultDTOIndex = -1;
    private boolean mapped = false;
    private boolean realEliminate = false;

    /**
     * 最后真正匹配上的RawIssue
     */
    private RawIssue mappedRawIssue = null;

    public void addRawIssueMappedResult(RawIssue rawIssue, double matchDegree) {
        mapped = true;
        rawIssueMatchResults.add( RawIssueMatchResult.newInstance(rawIssue, matchDegree));
    }

    public Date getCommit_time() {
        if(commit_time == null){
            return null;
        }
        return (Date) commit_time.clone();
    }

    public void setCommit_time(Date commit_time) {
        if(commit_time == null){
            this.commit_time = null;
        }else {
            this.commit_time = (Date) commit_time.clone();
        }
    }

    /**
     * 因为在bugMapping中被作为key，故不可以随意删除,且不可加入mapped
     */
    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + uuid.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + detail.hashCode();
        result = 31 * result + scan_id.hashCode();
        result = 31 * result + commit_id.hashCode();
        return result;
    }

    /**
     * 因为在bugMapping中被作为key，故不可以随意删除，且不可加入mapped
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RawIssue)) {
            return false;
        }
        RawIssue rawIssue = (RawIssue) obj;

        return rawIssue.getUuid().equals(((RawIssue) obj).getUuid());
    }

    @Override
    public String toString() {
        return "{uuid="+uuid+",type="+type+",tool="+tool+",detail="+detail+"}";
    }
}