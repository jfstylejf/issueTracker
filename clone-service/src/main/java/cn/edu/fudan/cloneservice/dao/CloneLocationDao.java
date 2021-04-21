package cn.edu.fudan.cloneservice.dao;

import cn.edu.fudan.cloneservice.domain.CloneOverallView;
import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import cn.edu.fudan.cloneservice.mapper.CloneLocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zyh
 * @date 2020/5/27
 */
@Repository
public class CloneLocationDao {

    private CloneLocationMapper cloneLocationMapper;

    @Autowired
    private void setCloneLocationMapper(CloneLocationMapper cloneLocationMapper) {
        this.cloneLocationMapper = cloneLocationMapper;
    }

    public int getCloneLocationGroupSum(List<String> repoUuids, String until) {
        int res = 0;
        for (String repoUuid : repoUuids) {
            if (repoUuid != null) {
                String commitId = cloneLocationMapper.getLatestCommitId(repoUuid, "", until);
                if (commitId != null) {
                    res = res + cloneLocationMapper.getGroupCount(cloneLocationMapper.getLatestCommitId(repoUuid, "", until));
                }
            }
        }
        return res;
    }

    public List<CloneOverallView> getCloneOverall(List<String> repoUuids, String initDate, String projectId, String projectName){
        List<CloneOverallView> result = new ArrayList<>();
        for(String repoUuid : repoUuids){
            if(repoUuid != null){
                String commitId = cloneLocationMapper.getLatestCommitId(repoUuid, "", initDate);
                if(commitId != null){
                    int caseSum = cloneLocationMapper.getGroupCount(commitId);
                    int fileSum = cloneLocationMapper.getFileCount(commitId);
                    int codeLengthsSize = 0;
                    int codeLengthsSum = 0;
                    int codeLengthsAverage = 0;
                    List<String> codeLengths = cloneLocationMapper.getCloneNum(commitId);
                    if(!codeLengths.isEmpty()){
                        codeLengthsSize = codeLengths.size();
                        for(String codeLength: codeLengths){
                            codeLengthsSum += codeLength.split(",").length;
                        }
                        codeLengthsAverage = codeLengthsSum/codeLengthsSize;
                    }

                    // FIXME: 2021/4/20 cloneType之后要改
                    int cloneType = 1;
                    CloneOverallView cloneOverallView = new CloneOverallView(projectName, projectId, initDate, repoUuid, caseSum, fileSum, codeLengthsAverage, cloneType);
                    result.add(cloneOverallView);
                }
            }
        }
        return result;
    }

    public void insertCloneLocations(List<CloneLocation> cloneLocations) {
        cloneLocationMapper.insertCloneLocationList(cloneLocations);
    }

    public List<CloneLocation> getCloneLocations(String repoId, String commitId) {
        return cloneLocationMapper.getCloneLocations(repoId, commitId);
    }

    public List<CloneLocation> getCloneLocationsTest(String repoId, String commitId) {
        return cloneLocationMapper.getCloneLocationsTest(repoId, commitId);
    }

    public void deleteCloneLocations(String repoId) {
        cloneLocationMapper.deleteCloneLocations(repoId);
    }

}
