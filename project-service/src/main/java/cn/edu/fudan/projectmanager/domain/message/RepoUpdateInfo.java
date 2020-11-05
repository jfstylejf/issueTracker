package cn.edu.fudan.projectmanager.domain.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * description:
 *
 * @author fancying
 * create: 2020-11-05 17:08
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepoUpdateInfo {

    String repoId;
    String branch;
    Boolean isUpdate;
}