package cn.edu.fudan.scanservice.component.scan;

import cn.edu.fudan.scanservice.annotation.FreeResource;
import cn.edu.fudan.scanservice.component.rest.RestInterfaceManager;
import cn.edu.fudan.scanservice.domain.dto.RepoResourceDTO;
import cn.edu.fudan.scanservice.util.CompileUtil;
import cn.edu.fudan.scanservice.util.DateTimeUtil;
import cn.edu.fudan.scanservice.util.JGitHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("TAS")
public class TimeAggregationStrategy implements  FirstScanCommitFilterStrategy {

    private RestInterfaceManager restInvoker;

    private CompileUtil compileUtil;

    @Override
    @FreeResource
    public String filter(RepoResourceDTO repoResourceDTO, String repoId, String branch, Object argument) throws RuntimeException {
        String repoPath = restInvoker.getRepoPath(repoResourceDTO.getRepoId());
        repoResourceDTO.setRepoPath(repoPath);
        if(repoPath == null){
            throw new RuntimeException("cant get repo path");
        }
        //第一步参数转换
        Integer scanStartTime =  (Integer) argument;
        log.info ("start from " + scanStartTime + "month ago.");

        //第二步获取最新的commit的信息
        JGitHelper jGitHelper = new JGitHelper(repoPath);
        String latestCommit = jGitHelper.getLatestCommitId(branch);
        String latestCommitTimeString = jGitHelper.getCommitTime(latestCommit);

        //第三步根据参数以及最新的commit，获取起始的扫描时间
        LocalDateTime latestCommitTime  = DateTimeUtil.stringToLocalDate(latestCommitTimeString);
        LocalDateTime firstScanCommitTime = latestCommitTime.minusMonths(scanStartTime);

        //第四步先判断整体的聚合点是不是为0,如果没有聚合点或者第一个聚合点在扫描时间之后则直接返回初始commit
        List<RevCommit> revCommits = jGitHelper.getAllAggregationCommit();
        if(revCommits.isEmpty ()){
            return jGitHelper.getFirstCommitId();
        }
        String firstAggregation =  revCommits.get (0).getName ();
        String firstAggregationTimeString = jGitHelper.getCommitTime(firstAggregation);
        LocalDateTime firstAggregationTime = DateTimeUtil.stringToLocalDate(firstAggregationTimeString);
        if(firstAggregationTime.isAfter (firstScanCommitTime)){
            return jGitHelper.getFirstCommitId();
        }

        //第五步开始匹配
        //firstCommitDate用作退出循环
        String firstCommit = jGitHelper.getFirstCommitId();
        String firstCommitTime = jGitHelper.getCommitTime(firstCommit);
        LocalDateTime firstCommitDate = DateTimeUtil.stringToLocalDate (firstCommitTime);

        LocalDateTime bottomCommitTime = firstScanCommitTime.minusMonths(2);
        List<String> compiledFailedCommits = new ArrayList<> ();

        String baseCommitId = null;

        while(baseCommitId == null) {

            List<RevCommit> aggregationCommits = jGitHelper.getAggregationCommit (firstScanCommitTime);

            //如果聚合点为空集,则将时间前推3个月
            if(aggregationCommits.isEmpty ()){
                if(firstScanCommitTime.isBefore (firstCommitDate)){
                    break;
                }
                firstScanCommitTime = firstScanCommitTime.minusMonths (3);
                continue;
            }


            for(RevCommit commit : aggregationCommits){
                String commitTime = jGitHelper.getCommitTime(commit.getName());
                LocalDateTime commitDate = DateTimeUtil.stringToLocalDate (commitTime);
                if(commitDate.isAfter (bottomCommitTime)){
                    break;
                }

                if(compiledFailedCommits.contains (commit.getName())){
                    break;
                }

                jGitHelper.checkout(commit.getName());
                if (compileUtil.isCompilable(repoPath)) {
                    baseCommitId =  commit.getName();
                    break;
                }else{
                    compiledFailedCommits.add (commit.getName());
                }
            }

            firstScanCommitTime = firstScanCommitTime.minusMonths (3);


        }

        if(baseCommitId == null){
            baseCommitId = jGitHelper.getFirstCommitId();
        }

        return baseCommitId;
    }


    @Autowired
    public void setRestInvoker(RestInterfaceManager restInvoker) {
        this.restInvoker = restInvoker;
    }

    @Autowired
    public void setCompileUtil(CompileUtil compileUtil) {
        this.compileUtil = compileUtil;
    }
}
