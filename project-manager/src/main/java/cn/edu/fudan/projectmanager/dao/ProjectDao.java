package cn.edu.fudan.projectmanager.dao;

import cn.edu.fudan.projectmanager.domain.Project;
import cn.edu.fudan.projectmanager.domain.RepoBasicInfo;
import cn.edu.fudan.projectmanager.mapper.ProjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public class ProjectDao {


    private ProjectMapper projectMapper;

    @Autowired
    public void setProjectMapper(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public void addOneProject(Project project) {
        projectMapper.addOneProject(project);
    }

    public List<Project> getProjectByAccountId(String accountId) {
        return projectMapper.getProjectByAccountId(accountId);
    }

    public List<Project> getProjectByKeyWordAndAccountId(String account_id, String keyWord, String module, String type) {
        return projectMapper.getProjectByKeyWordAndAccountId(account_id, keyWord, module, type);
    }

    public List<Project> getAllProjectByKeyWord(String keyWord, String module, String type) {
        return projectMapper.getAllProjectByKeyWord(keyWord, module, type);
    }

    public List<Project> getProjectByRepoId(String repo_id){
        return projectMapper.getProjectByRepoId(repo_id);
    }

    public List<Project> getProjectList(String account_id,String type){
        return projectMapper.getProjectList(account_id, type);
    }

    //jeff
    public List<Project> getProjectListByModule(String account_id,String type, String module){
        return projectMapper.getProjectListByModule(account_id,type, module);
    }

    public Project getProjectByID(String projectId) {
        return projectMapper.getProjectByID(projectId);
    }

    public boolean hasBeenAdded(String account_id, String url, String type, String branch) {
        return projectMapper.getProjectByURLTypeAndAccountIdBranch(account_id, url, type ,branch) != null;
    }

    public void updateProjectStatus(Project project) {
        projectMapper.updateProjectStatus(project);
    }

    public void remove(String projectId) {
        projectMapper.remove(projectId);
    }

    public String getRepoId(String projectId) {
        return projectMapper.getRepoId(projectId);
    }


    public boolean existOtherProjectWithThisRepoIdAndType(String repoId,String type) {
        return projectMapper.getProjectIdsByRepoIdAndType(repoId,type).size() >= 2;
    }

    public boolean existProjectWithThisRepoIdAndTypeAndScannedSign(String repoId,String type,int scannedSign){
        return projectMapper.existProjectWithThisRepoIdAndTypeAndScannedSign(repoId, type,scannedSign)>0;
    }

//    public boolean existProjectWithThisRepoIdAndType(String repoId,String type){
//        return !projectMapper.getProjectIdsByRepoIdAndType(repoId, type).isEmpty();
//    }

    public List<Project> getProjectsByURLAndTypeBranch(String url,String type, String branch){
        return projectMapper.getProjectsByURLAndTypeBranch(url, type , branch);
    }

    public List<String> getRepoIdsByAccountIdAndType(String account_id,String type){
        return projectMapper.getRepoIdsByAccountIdAndType(account_id,type);
    }

    public void updateProjectFirstAutoScan( String repo_id, String type){
        projectMapper.updateProjectFirstAutoScan(repo_id, type);
    }

    public Project getProjectByRepoIdAndCategory( String account_id, String repoId,String category){
        return projectMapper.getProjectByRepoIdAndCategory(account_id, repoId,category);
    }

    public List<Project> getProjectsByCondition( String account_id, String category, String name, String module){
        return projectMapper.getProjectsByCondition(account_id, category,name,module);
    }

    public List<Project> getAllProjects(){
        return projectMapper.getProjects();
    }

    public Date getLatestCommitTime(String repoId){
        return projectMapper.getLatestCommitTime(repoId);
    }

    public List<Map<String, Object>> getProjectInfoByAccountName(String accountName) {
        return projectMapper.getProjectInfoByAccountName(accountName);
    }
}
