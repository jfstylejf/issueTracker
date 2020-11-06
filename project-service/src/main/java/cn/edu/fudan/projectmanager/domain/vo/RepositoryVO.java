package cn.edu.fudan.projectmanager.domain.vo;

import cn.edu.fudan.projectmanager.domain.SubRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * description: 前端展示的Project信息
 *
 * @author fancying
 * create: 2020-09-27 14:54
 **/
@Data
@AllArgsConstructor
public class RepositoryVO implements Serializable {

    private String uuid;
    private String repoUuid;
    private String url;
    private String language;
    private String branch;
    private String downloadStatus ;
    private Date latestCommitTime;
    private String projectName;
    private Date scanStart;

    private String repoName;

    public RepositoryVO() {

    }

    public RepositoryVO (SubRepository repository) {
        uuid = repository.getUuid();
        repoUuid = repository.getRepoUuid();
        url = repository.getUrl();
        language = repository.getLanguage();
        branch = repository.getBranch();
        downloadStatus  = repository.getDownloadStatus();
        latestCommitTime = repository.getLatestCommitTime();
        projectName = repository.getProjectName();
        scanStart = repository.getScanStart();
        repoName = StringUtils.isEmpty(repository.getRepoName()) ?
                url.substring(url.lastIndexOf("/")).replace("/", "") :
                repository.getRepoName();
    }


}