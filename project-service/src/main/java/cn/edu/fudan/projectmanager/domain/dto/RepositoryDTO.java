package cn.edu.fudan.projectmanager.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;

/**
 * description: 前端传入的project信息
 *
 * @author fancying
 * create: 2020-09-27 14:55
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepositoryDTO implements Serializable {

    private String url;
    private String repoSource;
    private String branch;

    private Boolean privateRepo;
    private String username;
    private String password;

    private String repoName;
    private String projectName;

}