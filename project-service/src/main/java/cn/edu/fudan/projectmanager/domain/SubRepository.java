package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author fancying
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubRepository {

    public static final String DOWNLOADING = "Downloading";
    public static final String DOWNLOADED = "Downloaded";

    public static final int EMPTY = 1;
    public static final int RESERVATIONS = 0;
    public static final int ALL = 2;

    private String uuid;
    private String url;
    private String language;
    private String branch;
    private String repoSource;
    private String downloadStatus;
    private Date latestCommitTime;
    private String repoUuid;
    private String projectName;
    private int recycled;
    private String importAccountUuid;
    private Date scanStart;

    private String repoName;
    private List<Map<String, String>> leaders;
}
