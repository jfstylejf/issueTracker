/**
 * @description:
 * @author: fancying
 * @create: 2019-06-05 17:16
 **/
package cn.edu.fudan.scanservice.util;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static cn.edu.fudan.scanservice.util.DateTimeUtil.timeTotimeStamp;

@SuppressWarnings("Duplicates")
@Slf4j
public class JGitHelper implements Closeable {



    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final int MERGE_WITH_CONFLICT = -1;
    private static final int MERGE_WITHOUT_CONFLICT = 2;
    private static final int NOT_MERGE = 1;
    private Repository repository;
    private RevWalk revWalk;
    private Git git;

    /**
     *
     * repoPath 加上了 .git 目录
     *
     */
    public JGitHelper(String repoPath) {
        String gitDir =  IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(gitDir))
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir() // scan up the file system tree
                    .build();
            git = new Git(repository);
            revWalk = new RevWalk(repository);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void checkout(String commit) {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            CheckoutCommand checkoutCommand = git.checkout();
            checkoutCommand.setName(commit).call();

        } catch (Exception e) {
            log.error("JGitHelper checkout error:{} ", e.getMessage());
        }
    }

    public void checkoutToLatestCommit(String branchName) {
        try {
            checkout(branchName);
        } catch (Exception e) {
            log.error("JGitHelper checkout error:{} ", e.getMessage());
        }
    }


    public String getAuthorName(String commit) {
        String authorName = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            authorName = revCommit.getAuthorIdent().getName();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return authorName;
    }

    public String getCommitTime(String commit) {
        String time = null;
        final String format = "yyyy-MM-dd HH:mm:ss";
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            int t = revCommit.getCommitTime() ;
            long timestamp = Long.parseLong(String.valueOf(t)) * 1000;
            time = new java.text.SimpleDateFormat(format).format(new java.util.Date(timestamp));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    private Long getLongCommitTime(String version) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            return revCommit.getCommitTime() * 1000L;
        }catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }


    public String getMess(String commit) {
        String message = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            message = revCommit.getFullMessage();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return message;
    }

    @Override
    public void close() {
        if (repository != null) {
            repository.close();
        }
    }


    public List<String> getCommitListByBranchAndBeginCommit(String branchName, String beginCommit) {
        checkout(branchName);
        Map<String, Long> commitMap = new HashMap<>(512);
        Long start = getLongCommitTime(beginCommit);
        if (start == 0) {
            throw new RuntimeException("beginCommit Error!");
        }
        try {
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit commit : commits) {
                Long commitTime = commit.getCommitTime() * 1000L;
                if (commitTime >= start) {
                    commitMap.put(commit.getName(), commitTime);
                }
            }
        } catch (GitAPIException e) {
            e.getMessage();
        }
        return new ArrayList<>(sortByValue(commitMap).keySet());
    }


    /**
     *
     *  getCommitTime return second not millisecond
     */
    public List<String> getCommitListByBranchAndDuration(String branchName, String duration) {
        if(branchName == null){
            try {
                branchName = repository.getBranch();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        checkout(branchName);
        final int durationLength = 21;
        Map<String, Long> commitMap = new HashMap<>(512);
        if (duration.length() < durationLength) {
            throw new RuntimeException("duration error!");
        }
        long start =  getTime(duration.substring(0,10));
        long end = getTime(duration.substring(11,21));
        try {
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit commit : commits) {
                long commitTime = commit.getCommitTime() * 1000L;
                if (commitTime <= end && commitTime >= start) {
                    commitMap.put(commit.getName(), commitTime);
                }
            }
        } catch (GitAPIException e) {
            e.getMessage();
        }
        return new ArrayList<>(sortByValue(commitMap).keySet());
    }

    /**
     * 由小到大排序
     * st.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).forEach(e -> result.put(e.getKey(), e.getValue()));
     * 默认由大到小排序
     * 类型 V 必须实现 Comparable 接口，并且这个接口的类型是 V 或 V 的任一父类。这样声明后，V 的实例之间，V 的实例和它的父类的实例之间，可以相互比较大小。
     */
    private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    /**
     * s : 2018.01.01
     */
    private long getTime(String s) {
        s = s.replace(".","-");
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = dateFormat.parse(s);
            return  date.getTime();
        }catch (ParseException e) {
            e.getMessage();
        }
        return 0;
    }

    public String[] getCommitParents(String commit) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            RevCommit[] parentCommits = revCommit.getParents();
            String[] result = new String[parentCommits.length];
            for (int i = 0; i < parentCommits.length; i++) {
                result[i] = parentCommits[i].getName();
            }
            return result;
        }catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public Map<String, List<DiffEntry>> getMappedFileList(String commit) {
        Map<String, List<DiffEntry>> result = new HashMap<>(8);
        try {
            RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            RevCommit[] parentCommits = currCommit.getParents();
            for (RevCommit p : parentCommits) {
                RevCommit parentCommit = revWalk.parseCommit(ObjectId.fromString(p.getName()));
                ObjectReader reader = git.getRepository().newObjectReader();
                CanonicalTreeParser currTreeIter = new CanonicalTreeParser();
                currTreeIter.reset(reader, currCommit.getTree().getId());

                CanonicalTreeParser parentTreeIter = new CanonicalTreeParser();
                parentTreeIter.reset(reader, parentCommit.getTree().getId());
                DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                diffFormatter.setRepository(git.getRepository());
                List<DiffEntry> entries = diffFormatter.scan(currTreeIter, parentTreeIter);
                result.put(parentCommit.getName(), entries);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int mergeJudgment(String commit) {
        Map<String, List<DiffEntry>> diffList = getMappedFileList(commit);
        if (diffList.keySet().size() == NOT_MERGE) {
            return NOT_MERGE;
        }
        Set<String> stringSet = new HashSet<>();
        boolean isFirst = true;
        for (List<DiffEntry> diffEntryList : diffList.values()) {
            for (DiffEntry diffEntry : diffEntryList) {
                if (isFirst) {
                    stringSet.add(diffEntry.getOldPath());
                } else if (stringSet.contains(diffEntry.getOldPath())){
                    return MERGE_WITH_CONFLICT;
                }
            }
            isFirst = false;
        }
        return MERGE_WITHOUT_CONFLICT;
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

    public List<RevCommit> getAggregationCommit(LocalDateTime startTime){
        List<RevCommit> aggregationCommits = new ArrayList<>();
        String startTimeString = DateTimeUtil.localDateTimeToString(startTime);
        try {
            int startTimeStamp = Integer.valueOf(timeTotimeStamp(startTimeString));
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
                if (startTimeStamp<revCommit.getCommitTime()&&branch==1) {aggregationCommits.add(revCommit);}
                branch += Optional.ofNullable(sonCommitsMap.get(revCommit.getName())).orElse(0)-1;
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        }

        return aggregationCommits;
    }

    /**
     *
     * @param startTime 给定的日期时间 如 2020-12-01 12:00:00
     * @return 返回startTime 这个时间之后的第一个commit的commit id
     */
    public String getFirstCommitIdAfterGivenTime(LocalDateTime startTime){
        String startTimeString = DateTimeUtil.localDateTimeToString(startTime);
        List<RevCommit> commitList = new ArrayList<>();
        try {
            int startTimeStamp = Integer.valueOf(timeTotimeStamp(startTimeString));
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit revCommit: commits) {
                if (startTimeStamp<revCommit.getCommitTime()) {
                    commitList.add(revCommit);
                }
            }
            commitList.sort(Comparator.comparingInt(RevCommit::getCommitTime));
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return commitList.get(0).getName();
    }


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



    public static void main(String[] args) throws ParseException {
        //gitCheckout("E:\\Lab\\project\\IssueTracker-Master", "f8263335ef380d93d6bb93b2876484e325116ac2");
        //String repoPath = "E:\\Lab\\iec-wepm-develop";
        String repoPath = "E:\\school\\laboratory\\gitlab";
//        String commitId = "75c6507e2139e9bb663abf35037b31478e44c616";
        JGitHelper jGitHelper = new JGitHelper(repoPath);

        List<RevCommit> allAggregationCommit = jGitHelper.getAllAggregationCommit ();

        allAggregationCommit = allAggregationCommit.stream ().sorted (Comparator.comparing (RevCommit :: getCommitTime)).collect (Collectors.toList ());

        for(RevCommit a :  allAggregationCommit){
            System.out.println (a.getName () + "-----" + jGitHelper.getCommitTime(a.getName ()));
        }






        //String s[] = jGitHelper.getCommitParents(commitId);
//        int m = jGitHelper.mergeJudgment(commitId);
//        System.out.println(m);
//        String t = jGitHelper.getCommitTime("f61e34233aa536cf5e698b502099e12d1caf77e4");
//        for (String s : jGitHelper.getCommitListByBranchAndDuration("zhonghui20191012", "2019.10.12-2019.12.16")) {
//            System.out.println(s);
//            jGitHelper.checkout(s);
//        }
    }

}