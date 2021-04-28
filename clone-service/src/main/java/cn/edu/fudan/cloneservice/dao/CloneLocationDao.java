package cn.edu.fudan.cloneservice.dao;

import cn.edu.fudan.cloneservice.domain.CloneDetail;
import cn.edu.fudan.cloneservice.domain.CloneDetailOverall;
import cn.edu.fudan.cloneservice.domain.CloneOverallView;
import cn.edu.fudan.cloneservice.domain.clone.CloneLocation;
import cn.edu.fudan.cloneservice.mapper.CloneLocationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    public List<CloneOverallView> getCloneOverall(List<String> repoUuids, String initDate, String projectId, String projectName) {
        List<CloneOverallView> result = new ArrayList<>();
        for (String repoUuid : repoUuids) {
            if (repoUuid != null) {
                String commitId = cloneLocationMapper.getLatestCommitId(repoUuid, "", initDate);
                if (commitId != null) {
                    int groupSum = cloneLocationMapper.getGroupCount(commitId);
                    int caseSum = cloneLocationMapper.getCaseCount(commitId, "");
                    int fileSum = cloneLocationMapper.getFileCount(commitId, "");
                    int codeLengthsSize = 0;
                    int codeLengthsSum = 0;
                    int codeLengthsAverage = 0;
                    List<String> codeLengths = cloneLocationMapper.getCloneNum(commitId, "");
                    if (!codeLengths.isEmpty()) {
                        codeLengthsSize = codeLengths.size();
                        for (String codeLength : codeLengths) {
                            codeLengthsSum += codeLength.split(",").length;
                        }
                        codeLengthsAverage = codeLengthsSum / codeLengthsSize;
                    }

                    // FIXME: 2021/4/20 cloneType之后要改
                    CloneOverallView cloneOverallView = new CloneOverallView(projectName, projectId, initDate, repoUuid, caseSum, fileSum, groupSum, codeLengthsAverage, commitId);
                    result.add(cloneOverallView);
                }
            }
        }
        return result;
    }

    public List<CloneDetailOverall> getCloneDetailOverall(String projectId, String projectName, List<String> repoUuids, String commitId, String initDate) {
        List<CloneDetailOverall> result = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        if (!StringUtils.isEmpty(commitId)) {
            String repoUuid = cloneLocationMapper.getRepoIdByCommitId(commitId);
            groupIds = cloneLocationMapper.getGroupIds(commitId);
            for (String groupId : groupIds) {
                String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
                int cloneType = 2;
                int caseSum = cloneLocationMapper.getCaseCount(commitId, groupId);
                int fileSum = cloneLocationMapper.getFileCount(commitId, groupId);
                int codeLengthsSize = 0;
                int codeLengthsSum = 0;
                int codeLengthsAverage = 0;
                List<String> codeLengths = cloneLocationMapper.getCloneNum(commitId, groupId);
                if (!codeLengths.isEmpty()) {
                    codeLengthsSize = codeLengths.size();
                    for (String codeLength : codeLengths) {
                        codeLengthsSum += codeLength.split(",").length;
                    }
                    codeLengthsAverage = codeLengthsSum / codeLengthsSize;
                }

                CloneDetailOverall cloneDetailOverall = new CloneDetailOverall(uuid, projectName, projectId, repoUuid, commitId, Integer.parseInt(groupId), cloneType, caseSum, fileSum, codeLengthsAverage);
                result.add(cloneDetailOverall);
            }
        }else {
            if (!repoUuids.isEmpty()) {
                for (String repoUuid : repoUuids) {
                    String commitId1 = cloneLocationMapper.getLatestCommitId(repoUuid, "", initDate);
                    groupIds.addAll(cloneLocationMapper.getGroupIds(commitId1));
                    for (String groupId : groupIds) {
                        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
                        int cloneType = 2;
                        int caseSum = cloneLocationMapper.getCaseCount(commitId1, groupId);
                        int fileSum = cloneLocationMapper.getFileCount(commitId1, groupId);
                        int codeLengthsSize = 0;
                        int codeLengthsSum = 0;
                        int codeLengthsAverage = 0;
                        List<String> codeLengths = cloneLocationMapper.getCloneNum(commitId1, groupId);
                        if (!codeLengths.isEmpty()) {
                            codeLengthsSize = codeLengths.size();
                            for (String codeLength : codeLengths) {
                                codeLengthsSum += codeLength.split(",").length;
                            }
                            codeLengthsAverage = codeLengthsSum / codeLengthsSize;
                        }

                        CloneDetailOverall cloneDetailOverall = new CloneDetailOverall(uuid, projectName, projectId, repoUuid, commitId1, Integer.parseInt(groupId), cloneType, caseSum, fileSum, codeLengthsAverage);
                        result.add(cloneDetailOverall);
                    }
                }
            }
        }
        return result;
    }

    public List<CloneDetail> getCloneDetail(String repoUuid, String projectId, String groupIdPara, String projectName, String commitId) {
        List<CloneDetail> result = new ArrayList<>();
        if (!StringUtils.isEmpty(commitId)) {
            if (!StringUtils.isEmpty(commitId)) {
                List<CloneLocation> cloneLocations = cloneLocationMapper.getCloneLocations(repoUuid, groupIdPara, commitId);
                for (CloneLocation cloneLocation : cloneLocations) {
                    String cloneLines = cloneLocation.getCloneLines();
                    int startLine = Integer.parseInt(cloneLines.split(",")[0]);
                    int endLine = Integer.parseInt(cloneLines.split(",")[1]);
                    int groupId = Integer.parseInt(cloneLocation.getCategory());
                    // FIXME: 2021/4/22
                    int cloneType = 1;
                    int lineCount = cloneLocation.getNum().split(",").length;
                    String uuid = cloneLocation.getUuid();
                    String detail = cloneLocation.getNum();
                    String className = cloneLocation.getClassName();
                    String filePath = cloneLocation.getFilePath();
                    CloneDetail cloneDetail = new CloneDetail(uuid, projectName, projectId, repoUuid, commitId, filePath, groupId, className, startLine, endLine, lineCount, detail, cloneType);
                    result.add(cloneDetail);
                }
            }
        }
        return result;
    }

    public void insertCloneLocations(List<CloneLocation> cloneLocations) {
        cloneLocationMapper.insertCloneLocationList(cloneLocations);
    }

    public List<CloneLocation> getCloneLocations(String repoId, String commitId) {
        return cloneLocationMapper.getCloneLocations(repoId, "", commitId);
    }

    public List<CloneLocation> getCloneLocationsTest(String repoId, String commitId) {
        return cloneLocationMapper.getCloneLocationsTest(repoId, commitId);
    }

    public void deleteCloneLocations(String repoId) {
        cloneLocationMapper.deleteCloneLocations(repoId);
    }

}
