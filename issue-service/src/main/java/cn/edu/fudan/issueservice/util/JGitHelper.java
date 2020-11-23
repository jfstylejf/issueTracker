/**
 * @description:
 * @author: fancying
 * @create: 2019-06-05 17:16
 **/
package cn.edu.fudan.issueservice.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RenameDetector;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

import static cn.edu.fudan.issueservice.util.DateTimeUtil.timeTotimeStamp;

@SuppressWarnings("Duplicates")
@Slf4j
public class JGitHelper {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final int MERGE_WITH_CONFLICT = -1;
    private static final int MERGE_WITHOUT_CONFLICT = 2;
    private static final int NOT_MERGE = 1;
    private Repository repository;
    private RevWalk revWalk;
    private Git git;

    private final String format = "yyyy-MM-dd HH:mm:ss";
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

    public static Integer getConflictValue() {
        return MERGE_WITH_CONFLICT;
    }

    public static Integer getWithoutConflictValue() {
        return MERGE_WITHOUT_CONFLICT;
    }


    public void checkout(String commit) {
        try {
            if(commit == null){
                commit = repository.getBranch();
            }
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            CheckoutCommand checkoutCommand = git.checkout();
            checkoutCommand.setName(commit).call();
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
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            int t = revCommit.getCommitTime() ;
            long timestamp = Long.parseLong(String.valueOf(t)) * 1000;
            Date date = new java.util.Date(timestamp);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime (date);
            calendar.add (Calendar.HOUR_OF_DAY, -8);
            time = new java.text.SimpleDateFormat(format).format(calendar.getTime ());
        }catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    @SneakyThrows
    public Date getCommitDateTime(String commit) {
        return new SimpleDateFormat(format).parse(getCommitTime(commit));
    }

    private Long getLongCommitTime(String version) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            return revCommit.getCommitTime() * 1000L;
        }catch (Exception e) {
            log.error(e.getMessage());
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


    public void close() {
        if (repository != null) {
            repository.close();
        }
    }

    public List<String> getScanCommitListByBranchAndBeginCommit(String branch, String beginCommit) {
        //checkout to the branch
        checkout(branch);
        //new result set
        Map<String, Set<String>> commitMap = new HashMap<>(512);
        Map<String, Boolean> commitCheckMap = new HashMap<>(512);
        List<String> scanCommitQueue = new ArrayList<>();
        //get the start commit time
        Long start = getLongCommitTime(beginCommit);
        //init commitMap:key->commitId,value->set<String> parentsCommitId
        try {
            Iterable<RevCommit> commits = git.log().call();
            commits.forEach(commit -> {
                if(commit.getCommitTime() * 1000L >= start) {
                    Set<String> parents = new HashSet<>();
                    List<RevCommit> parentsCommit = Arrays.asList(commit.getParents());
                    parentsCommit.forEach(parentCommit -> parents.add(parentCommit.getName()));
                    commitMap.put(commit.getName(), parents);
                    commitCheckMap.put(commit.getName(), false);
                }
            });
        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
        //init scanCommitQueue
        scanCommitQueue.add(beginCommit);
        commitCheckMap.put(beginCommit, true);
        //get the commitList
        while(scanCommitQueue.size() != commitMap.size()){
            for(Map.Entry<String, Set<String>> entry : commitMap.entrySet()){
                //if parent in commitMap but not in scanCommitQueue, should not add to queue.
                boolean shouldAddToQueue = shouldAddToQueue(entry.getValue(), scanCommitQueue, commitMap);
                if(shouldAddToQueue && !commitCheckMap.get(entry.getKey())){
                    scanCommitQueue.add(entry.getKey());
                    commitCheckMap.put(entry.getKey(), true);
                    break;
                }
            }
        }

        return scanCommitQueue;
    }

    private boolean shouldAddToQueue(Set<String> parents, List<String> scanCommitQueue, Map<String, Set<String>> commitMap) {
        for(String parent : parents){
            if(commitMap.containsKey(parent) && !scanCommitQueue.contains(parent)){
                return false;
            }
        }
        return true;
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
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
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


    public List<RevCommit> getAggregationCommit(String startTime){
        List<RevCommit> aggregationCommits = new ArrayList<>();
        try {
            int startTimeStamp = Integer.valueOf(timeTotimeStamp(startTime));
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
                if (startTimeStamp<=revCommit.getCommitTime()&&branch==1) {aggregationCommits.add(revCommit);}
                branch += Optional.ofNullable(sonCommitsMap.get(revCommit.getName())).orElse(0)-1;
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return aggregationCommits;
    }


    /**
     * 判断是否是一个聚合点
     * @param commitId
     * @return
     */
    public boolean verifyWhetherAggregationCommit(String commitId){
        boolean result = false;
        List<RevCommit> revCommits = getAllAggregationCommit();
        for(RevCommit revCommit: revCommits){
            if(revCommit.getName().equals(commitId)){
                result = true;
                break;
            }
        }
        return result;

    }


    @SneakyThrows
    private List<DiffEntry> getDiffEntry(RevCommit parentCommit, RevCommit currCommit) {
        parentCommit = revWalk.parseCommit(ObjectId.fromString(parentCommit.getName()));
        TreeWalk tw = new TreeWalk(repository);
        tw.addTree(parentCommit.getTree());
        tw.addTree(currCommit.getTree());
        tw.setRecursive(true);
        RenameDetector rd = new RenameDetector(repository);
        rd.addAll(DiffEntry.scan(tw));
        rd.setRenameScore(100);
        return rd.compute();
    }



    @SneakyThrows
    public RevCommit getRevCommit(String commitId){
        return revWalk.parseCommit(repository.resolve(commitId));
    }

    @SneakyThrows
    public List<DiffEntry> getConflictDiffEntryList (String commit) {
        RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
        RevCommit[] parentCommits = currCommit.getParents();
        if (parentCommits.length != 2) {
            return null;
        }

        List<DiffEntry> parent1 = getDiffEntry(parentCommits[0], currCommit);
        List<DiffEntry> parent2 = getDiffEntry(parentCommits[1], currCommit);
        List<DiffEntry> result = new ArrayList<>();
        if (isParent2(parentCommits[0], parentCommits[1], currCommit)) {
            List<DiffEntry> tmp = parent1;
            parent1 = parent2;
            parent2 = tmp;
        }

        // oldPath 相同
        for (DiffEntry diffEntry1 : parent1) {
            for (DiffEntry diffEntry2 :parent2) {
                // todo 暂未考虑重命名的情况 或者无需考虑重命名的情况
                //  如 p1 a=a1  p2 a=>a2 是否冲突待验证
                boolean isSame = diffEntry1.getOldPath().equals(diffEntry2.getOldPath()) &&
                        diffEntry1.getNewPath().equals(diffEntry2.getNewPath());

                if (isSame) {
                    result.add(diffEntry1);
                }
            }
        }
        return result;
    }


    private boolean isParent2(RevCommit parent1, RevCommit parent2, RevCommit currCommit) {
        String author1 = parent1.getAuthorIdent().getName();
        String author2 = parent2.getAuthorIdent().getName();
        String author = currCommit.getAuthorIdent().getName();
        if (author.equals(author2) && !author.equals(author1)) {
            return true;
        }

        if (!author.equals(author2) && author.equals(author1)) {
            return false;
        }

        return parent2.getCommitTime() > parent1.getCommitTime();
    }

    /**
     * 得到所有的DiffEntry
     */
    @SneakyThrows
    public List<DiffEntry> getDiffEntry(String commitId) {
        RevCommit revCommit = getRevCommit(commitId);
        RevCommit[] parentCommits = revCommit.getParents();
        if (parentCommits.length == 0) {
            return new ArrayList<>(0);
        }
        List<DiffEntry> diffEntryList = new ArrayList<>();
        for (RevCommit p : parentCommits) {
            diffEntryList.addAll(getDiffEntry(p, revCommit));
        }
        return diffEntryList;
    }

    @SneakyThrows
    public RevCommit getBlameCommit(String commitId, String filePath, int start, int end ){
        RevCommit revCommit = getRevCommit(commitId);
        ObjectId curCommitId = repository.resolve(commitId);
        BlameCommand blamer = new BlameCommand(repository);
        blamer.setStartCommit(curCommitId);
        blamer.setFilePath(filePath);
        BlameResult blame = blamer.call();
        RevCommit result = revCommit;
        int time = 0;
        for(int i = start; i <= end; i++){
            RevCommit sourceCommit = blame.getSourceCommit(i);
            if (time < sourceCommit.getCommitTime()) {
                time = sourceCommit.getCommitTime();
                result = sourceCommit;
            }
        }
        return result;
    }

    /**
     * 获取指定文件在指定的两个commit之间的改动次数  不含首，含尾
     * @param startCommitId 开始的commit id  如果startCommitId有改动该文件，不计入改动次数
     * @param endCommitId 终止的commit id  如果endCommitId有改动该文件，计入改动次数
     * @param filePath 此处的filePath 表示该文件在项目中的相对路径，且文件路径一定要使用 / 进行分隔
     * @return
     */
    @SneakyThrows
    public int getFileChangedCount(String startCommitId, String endCommitId, String filePath){
        int result = 0;
        ObjectId startCommit = repository.resolve(startCommitId);
        ObjectId endCommit = repository.resolve(endCommitId);
        Iterable<RevCommit> revCommits = git.log ().addPath(filePath).addRange (startCommit, endCommit).call ();
        Iterator<RevCommit> iterator = revCommits.iterator ();
        while (iterator.hasNext ()) {
            iterator.next ();
            result++;
        }
        return result;
    }

}