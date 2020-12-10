package cn.edu.fudan.common.jgit;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class ScanJGitHelper extends JGitHelper {

    /**
     * 通用:JGit构造方法
     *
     * @param repoPath 代码库路径
     */
    public ScanJGitHelper(String repoPath) {
        super(repoPath);
    }

    public List<RevCommit> getAllAggregationCommit(){
        List<RevCommit> aggregationCommits = new ArrayList<>();
        try {

            int branch = 0;
            Iterable<RevCommit> commits = git.log().call();
            List<RevCommit> commitList = new ArrayList<>();
            Map<String,Integer> sonCommitsMap = new HashMap<>();
            for (RevCommit revCommit: commits) {
                commitList.add(revCommit);
                RevCommit[] parents = revCommit.getParents();
                for (RevCommit parentCommit : parents) {
                    int sonCount = Optional.ofNullable(sonCommitsMap.get(parentCommit.getName())).orElse(0);
                    sonCommitsMap.put(parentCommit.getName(),++sonCount);
                }
            }
            commitList.sort(Comparator.comparingInt(RevCommit::getCommitTime));

            for (RevCommit revCommit : commitList) {
                branch -= revCommit.getParentCount()-1;
                if (branch==1) {aggregationCommits.add(revCommit);}
                branch += Optional.ofNullable(sonCommitsMap.get(revCommit.getName())).orElse(0)-1;
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return aggregationCommits;
    }

//    public List<RevCommit> getAggregationCommit(LocalDateTime startTime){
//        List<RevCommit> aggregationCommits = new ArrayList<>();
//        String startTimeString = DateTimeUtil.localDateTimeToString(startTime);
//        try {
//            int startTimeStamp = Integer.valueOf(timeTotimeStamp(startTimeString));
//            int branch = 0;
//            Iterable<RevCommit> commits = git.log().call();
//            List<RevCommit> commitList = new ArrayList<>();
//            Map<String,Integer> sonCommitsMap = new HashMap<>();
//            for (RevCommit revCommit: commits) {
//                commitList.add(revCommit);
//                RevCommit[] parents = revCommit.getParents();
//                for (RevCommit parentCommit : parents) {
//                    int sonCount = Optional.ofNullable(sonCommitsMap.get(parentCommit.getName())).orElse(0);
//                    sonCommitsMap.put(parentCommit.getName(),++sonCount);
//                }
//            }
//            commitList.sort(Comparator.comparingInt(RevCommit::getCommitTime));
//
//            for (RevCommit revCommit : commitList) {
//                branch -= revCommit.getParentCount()-1;
//                if (startTimeStamp<revCommit.getCommitTime()&&branch==1) {aggregationCommits.add(revCommit);}
//                branch += Optional.ofNullable(sonCommitsMap.get(revCommit.getName())).orElse(0)-1;
//            }
//        } catch (GitAPIException e) {
//            e.printStackTrace();
//        }
//
//        return aggregationCommits;
//    }


    public String getLatestCommitId(String branchName){
        try {
            checkout(branchName);
            ObjectId obj = repository.resolve("HEAD");
            if(obj != null){
                return obj.getName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getFirstCommitId(){
        List<RevCommit> commitList = new ArrayList<>();
        try {
            Iterable<RevCommit> commits = git.log().call();

            Map<String,Integer> sonCommitsMap = new HashMap<>();
            for (RevCommit revCommit: commits) {
                commitList.add(revCommit);
                RevCommit[] parents = revCommit.getParents();
                for (RevCommit parentCommit : parents) {
                    int sonCount = Optional.ofNullable(sonCommitsMap.get(parentCommit.getName())).orElse(0);
                    sonCommitsMap.put(parentCommit.getName(),++sonCount);
                }
            }
            commitList.sort(Comparator.comparingInt(RevCommit::getCommitTime));


        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return commitList.get(0).getName();

    }

}
