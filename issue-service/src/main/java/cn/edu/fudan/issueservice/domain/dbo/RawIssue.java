package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.dto.RawIssueMatchResult;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * fixme 修改RawIssue中不符合规范的field命名
 * scan_id detail 多余 重复 ？
 *
 * @author fancying
 */
@Data
public class RawIssue {

    private String uuid;
    private String type;
    private String tool;
    private String detail;
    private String fileName;
    private String scanId;
    private String commitId;
    private String repoId;
    private int codeLines;
    private Date commitTime;
    private List<Location> locations;
    private int version = 1;
    private String developerEmail;
    private int priority;
    /**
     * 开发者聚合后的唯一姓名
     */
    private String developerName;
    /**
     * 下面为 raw issue 的 匹配信息
     */
    private String status = RawIssueStatus.ADD.getType();
    private String issueId;
    private List<RawIssueMatchResult> rawIssueMatchResults = new ArrayList<>(0);
    private int matchResultDTOIndex = -1;
    /**
     * 根据 mapped 来决定最后是否映射上一个issue
     **/
    private boolean mapped = false;

    /**
     * 最后真正匹配上的RawIssue
     */
    private RawIssue mappedRawIssue = null;
    private double matchDegree;

    public void resetMappedInfo() {
        mapped = false;

        status = RawIssueStatus.ADD.getType();
        rawIssueMatchResults = new ArrayList<>(0);
        matchResultDTOIndex = -1;
        mappedRawIssue = null;
        matchDegree = 0.0;
    }

    @Deprecated
    private Issue issue;
    @Deprecated
    private boolean realEliminate = false;

    // TODO: 2021/1/6 新增的也有matchPairs
    private List<RawIssueMatchInfo> matchInfos = new ArrayList<>(8);

    public void addRawIssueMappedResult(RawIssue rawIssue, double matchDegree) {
        mapped = true;
        rawIssueMatchResults.add(RawIssueMatchResult.newInstance(rawIssue, matchDegree));
    }

    public RawIssueMatchInfo generateRawIssueMatchInfo(String preCommitId) {
        String preRawIssueUuid = RawIssueMatchInfo.EMPTY;
        preCommitId = preCommitId == null ? RawIssueMatchInfo.EMPTY : preCommitId;
        if (mapped) {
            preRawIssueUuid = mappedRawIssue.getUuid();
        }

        return RawIssueMatchInfo.builder()
                .uuid(UUID.randomUUID().toString())
                .curRawIssueUuid(uuid).curCommitId(commitId)
                .preRawIssueUuid(preRawIssueUuid).preCommitId(preCommitId)
                .status(status)
                .issueUuid(issueId)
                .matchDegree(matchDegree)
                .build();
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
        return "{uuid=" + uuid + ",type=" + type + ",tool=" + tool + ",detail=" + detail + "}";
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
        result = 31 * result + scanId.hashCode();
        result = 31 * result + commitId.hashCode();
        return result;
    }

    /**
     * 表示 status 是 default情况  在任何一个匹配中该rawIssue没有改变的情况
     * true: not change  false: change
     **/
    boolean notChange = false;

    /**
     * 多分支匹配的情况下记录是否有分支匹配上过
     **/
    boolean onceMapped = false;

    public static RawIssue valueOf(String ignoreRecord) {
        JSONObject ignoreRawIssue = JSONObject.parseObject(ignoreRecord);
        RawIssue rawIssue = new RawIssue();
        rawIssue.setUuid(UUID.randomUUID().toString());
        rawIssue.setType(ignoreRawIssue.getString("type"));
        rawIssue.setDetail(ignoreRawIssue.getString("detail"));
        rawIssue.setCommitId(ignoreRawIssue.getString("commit_id"));
        rawIssue.setCodeLines(ignoreRawIssue.getIntValue("code_lines"));
        rawIssue.setLocations(Location.valueOf(ignoreRawIssue.getJSONArray("locations")));
        return rawIssue;
    }

    public static RawIssue copyOf(RawIssue rawIssue) {
        RawIssue rawIssueCopy = new RawIssue();
        rawIssueCopy.setUuid(rawIssue.getUuid());
        rawIssueCopy.setType(rawIssue.getType());
        rawIssueCopy.setTool(rawIssue.getTool());
        rawIssueCopy.setDetail(rawIssue.getDetail());
        rawIssueCopy.setFileName(rawIssue.getFileName());
        rawIssueCopy.setScanId(rawIssue.getScanId());
        rawIssueCopy.setCommitId(rawIssue.getCommitId());
        rawIssueCopy.setRepoId(rawIssue.getRepoId());
        rawIssueCopy.setCodeLines(rawIssue.getCodeLines());
        rawIssueCopy.setCommitTime(rawIssue.getCommitTime());
        rawIssueCopy.setLocations(rawIssue.getLocations());
        rawIssueCopy.setVersion(rawIssue.getVersion());
        rawIssueCopy.setPriority(rawIssue.getPriority());
        rawIssueCopy.setIssueId(rawIssue.getIssueId());
        return rawIssueCopy;
    }
}