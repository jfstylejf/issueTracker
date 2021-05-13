//package cn.edu.fudan.common.jgit;
//
//import lombok.NoArgsConstructor;
//import lombok.SneakyThrows;
//import lombok.extern.slf4j.Slf4j;
//import org.eclipse.jgit.api.CheckoutCommand;
//import org.eclipse.jgit.api.Git;
//import org.eclipse.jgit.api.ResetCommand;
//import org.eclipse.jgit.api.errors.GitAPIException;
//import org.eclipse.jgit.diff.DiffEntry;
//import org.eclipse.jgit.diff.DiffFormatter;
//import org.eclipse.jgit.diff.RenameDetector;
//import org.eclipse.jgit.lib.*;
//import org.eclipse.jgit.revwalk.RevCommit;
//import org.eclipse.jgit.revwalk.RevWalk;
//import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
//import org.eclipse.jgit.treewalk.CanonicalTreeParser;
//import org.eclipse.jgit.treewalk.TreeWalk;
//import org.eclipse.jgit.util.io.DisabledOutputStream;
//
//import java.io.Closeable;
//import java.io.File;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.stream.Stream;
//
///**
// * @description: 所有Jgit操作的父类  子类的功能基于该类做补充
// * @author: fancying
// * @create: 2019-06-05 17:16
// **/
//@SuppressWarnings("Duplicates")
//@Slf4j
//@NoArgsConstructor
//public class JGitHelper implements Closeable {
//
//    protected static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
//
//    protected static final int MERGE_WITH_CONFLICT = -1;
//
//    protected static final int MERGE_WITHOUT_CONFLICT = 2;
//
//    protected static final int NOT_MERGE = 1;
//
//    protected Repository repository;
//
//    protected RevWalk revWalk;
//
//    protected Git git;
//
//    protected final String format = "yyyy-MM-dd HH:mm:ss";
//
//    protected final long toMillisecond = 1000L;
//
//    /**
//     * 通用:JGit构造方法
//     * @param repoPath 代码库路径
//     */
//    public JGitHelper(String repoPath) {
//        String gitDir =  IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
//        FileRepositoryBuilder builder = new FileRepositoryBuilder();
//        try {
//            repository = builder.setGitDir(new File(gitDir))
//                    .readEnvironment() // cn.edu.fudan.common.scan environment GIT_* variables
//                    .findGitDir() // cn.edu.fudan.common.scan up the file system tree
//                    .build();
//            git = new Git(repository);
//            revWalk = new RevWalk(repository);
//        } catch (IOException e) {
//            log.error(e.getMessage());
//        }
//    }
//
//    /**
//     * 通用:判断merge点是否有冲突
//     * @return merge conflict value
//     */
//    public static Integer getConflictValue() {
//        return MERGE_WITH_CONFLICT;
//    }
//
//    public static Integer getWithoutConflictValue() {
//        return MERGE_WITHOUT_CONFLICT;
//    }
//
//    /**
//     * 通用:checkout到指定commit
//     * @param commit commit id
//     */
//    public void checkout(String commit) {
//        try {
//            if(commit == null){
//                commit = repository.getBranch();
//            }
//            git.reset().setMode(ResetCommand.ResetType.HARD).call();
//            CheckoutCommand checkoutCommand = git.checkout();
//            checkoutCommand.setName(commit).call();
//        } catch (Exception e) {
//            log.error("JGitHelper checkout error:{} ", e.getMessage());
//        }
//    }
//
//    /**
//     * 通用:获取commit的author name
//     * @param commit commit id
//     * @return author name
//     */
//    public String getAuthorName(String commit) {
//        String authorName = null;
//        try {
//            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
//            authorName = revCommit.getAuthorIdent().getName();
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//        return authorName;
//    }
//
//    /**
//     * 通用:获取commit time
//     * @param commit commit id
//     * @return (Date)commit time
//     */
//    @SneakyThrows
//    public Date getCommitDateTime(String commit) {
//        return new SimpleDateFormat(format).parse(getCommitTime(commit));
//    }
//
//    /**
//     * 通用:获取commit time
//     * @param commit commit id
//     * @return (String)commit time
//     */
//    public String getCommitTime(String commit) {
//        String time = null;
//        try {
//            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
//            int t = revCommit.getCommitTime() ;
//            long timestamp = Long.parseLong(String.valueOf(t)) * toMillisecond;
//            Date date = new java.util.Date(timestamp);
//            Calendar calendar = Calendar.getInstance();
//            calendar.setTime (date);
//            calendar.add (Calendar.HOUR_OF_DAY, -8);
//            time = new java.text.SimpleDateFormat(format).format(calendar.getTime ());
//        }catch (Exception e) {
//            e.printStackTrace();
//        }
//        return time;
//    }
//
//    /**
//     * 通用:关闭资源
//     */
//    @Override
//    public void close() {
//        if (repository != null) {
//            repository.close();
//        }
//    }
//
//    /**
//     * todo clone-service fix beginScan
//     * 通用:根据策略获取扫描列表
//     * @param branch branch
//     * @param beginCommit begin commit 包含beginCommit
//     * @return commit list
//     */
//    public List<String> getScanCommitListByBranchAndBeginCommit(String branch, String beginCommit) {
//        //checkout to the branch
//        checkout(branch);
//        //new result set
//        Map<String, Set<String>> commitMap = new HashMap<>(512);
//        Map<String, Boolean> commitCheckMap = new HashMap<>(512);
//        List<String> scanCommitQueue = new ArrayList<>();
//        //get the start commit time
//        Long start = getLongCommitTime(beginCommit);
//        //init commitMap:key->commitId,value->set<String> parentsCommitId
//        try {
//            Iterable<RevCommit> commits = git.log().call();
//            commits.forEach(commit -> {
//                if(commit.getCommitTime() * toMillisecond >= start) {
//                    Set<String> parents = new HashSet<>();
//                    List<RevCommit> parentsCommit = Arrays.asList(commit.getParents());
//                    parentsCommit.forEach(parentCommit -> parents.add(parentCommit.getName()));
//                    commitMap.put(commit.getName(), parents);
//                    commitCheckMap.put(commit.getName(), false);
//                }
//            });
//        } catch (GitAPIException e) {
//            log.error(e.getMessage());
//        }
//        //init scanCommitQueue
//        scanCommitQueue.add(beginCommit);
//        commitCheckMap.put(beginCommit, true);
//        //get the commitList
//        while(scanCommitQueue.size() != commitMap.size()){
//            for(Map.Entry<String, Set<String>> entry : commitMap.entrySet()){
//                //if parent in commitMap but not in scanCommitQueue, should not add to queue.
//                boolean shouldAddToQueue = shouldAddToQueue(entry.getValue(), scanCommitQueue, commitMap);
//                if(shouldAddToQueue && !commitCheckMap.get(entry.getKey())){
//                    scanCommitQueue.add(entry.getKey());
//                    commitCheckMap.put(entry.getKey(), true);
//                    break;
//                }
//            }
//        }
//        return scanCommitQueue;
//    }
//
//    /**
//     * 通用:获取commit time
//     * @param commit commit id
//     * @return (Long)commit time
//     */
//    private Long getLongCommitTime(String commit) {
//        try {
//            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
//            return revCommit.getCommitTime() * toMillisecond;
//        }catch (Exception e) {
//            log.error(e.getMessage());
//            return 0L;
//        }
//    }
//
//    /**
//     * 通用:判断是否应该添加到扫描队列
//     * @param parents 该commit的所有parent commit id
//     * @param scanCommitQueue 扫描队列
//     * @param commitMap 未经过扫描策略排序的所有commit
//     * @return true or false
//     */
//    private boolean shouldAddToQueue(Set<String> parents, List<String> scanCommitQueue, Map<String, Set<String>> commitMap) {
//        for(String parent : parents){
//            if(commitMap.containsKey(parent) && !scanCommitQueue.contains(parent)){
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 由小到大排序
//     * st.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).forEach(e -> result.put(e.getKey(), e.getValue()));
//     * 默认由大到小排序
//     * 类型 V 必须实现 Comparable 接口，并且这个接口的类型是 V 或 V 的任一父类。这样声明后，V 的实例之间，V 的实例和它的父类的实例之间，可以相互比较大小。
//     */
//    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
//        Map<K, V> result = new LinkedHashMap<>();
//        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
//        st.sorted(Map.Entry.comparingByValue()).forEach(e -> result.put(e.getKey(), e.getValue()));
//        return result;
//    }
//
//    /**
//     * 通用:获取commit parents
//     * @param commit commit id
//     * @return commit parents id
//     */
//    public String[] getCommitParents(String commit) {
//        try {
//            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
//            RevCommit[] parentCommits = revCommit.getParents();
//            String[] result = new String[parentCommits.length];
//            for (int i = 0; i < parentCommits.length; i++) {
//                result[i] = parentCommits[i].getName();
//            }
//            return result;
//        }catch (Exception e) {
//            log.error(e.getMessage());
//        }
//        return null;
//    }
//
//    public Map<String, List<DiffEntry>> getMappedFileList(String commit) {
//        Map<String, List<DiffEntry>> result = new HashMap<>(8);
//        try {
//            RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
//            RevCommit[] parentCommits = currCommit.getParents();
//            for (RevCommit p : parentCommits) {
//                RevCommit parentCommit = revWalk.parseCommit(ObjectId.fromString(p.getName()));
//                ObjectReader reader = git.getRepository().newObjectReader();
//                CanonicalTreeParser currTreeIter = new CanonicalTreeParser();
//                currTreeIter.reset(reader, currCommit.getTree().getId());
//
//                CanonicalTreeParser parentTreeIter = new CanonicalTreeParser();
//                parentTreeIter.reset(reader, parentCommit.getTree().getId());
//                DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
//                diffFormatter.setRepository(git.getRepository());
//                List<DiffEntry> entries = diffFormatter.scan(currTreeIter, parentTreeIter);
//                result.put(parentCommit.getName(), entries);
//            }
//            return result;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    @SneakyThrows
//    private List<DiffEntry> getDiffEntry(RevCommit parentCommit, RevCommit currCommit, int score) {
//        // 不可少 否则parentCommit的 tree为null
//        parentCommit = revWalk.parseCommit(ObjectId.fromString(parentCommit.getName()));
//        TreeWalk tw = new TreeWalk(repository);
//        tw.addTree(parentCommit.getTree());
//        tw.addTree(currCommit.getTree());
//        tw.setRecursive(true);
//        RenameDetector rd = new RenameDetector(repository);
//        rd.addAll(DiffEntry.scan(tw));
//        rd.setRenameScore(score);
//        return rd.compute();
//    }
//
//    /**
//     * 默认rename阈值为60
//     * @return
//     */
//    private List<DiffEntry> getDiffEntry(RevCommit parentCommit, RevCommit currCommit){
//        return getDiffEntry(parentCommit, currCommit, 60);
//    }
//
//
//    /**
//     * 判断是否为merge点
//     * @param commit commit
//     * @return merge value
//     */
//    public int mergeJudgment(String commit) {
//        Map<String, List<DiffEntry>> diffList = getMappedFileList(commit);
//        if (diffList.keySet().size() == NOT_MERGE) {
//            return NOT_MERGE;
//        }
//        Set<String> stringSet = new HashSet<>();
//        boolean isFirst = true;
//        for (List<DiffEntry> diffEntryList : diffList.values()) {
//            for (DiffEntry diffEntry : diffEntryList) {
//                if (isFirst) {
//                    stringSet.add(diffEntry.getOldPath());
//                } else if (stringSet.contains(diffEntry.getOldPath())){
//                    return MERGE_WITH_CONFLICT;
//                }
//            }
//            isFirst = false;
//        }
//        return MERGE_WITHOUT_CONFLICT;
//    }
//}

package cn.edu.fudan.common.jgit;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
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

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * @description:
 * @author: fancying
 * @create: 2019-06-05 17:16:
 **/
@SuppressWarnings("Duplicates")
@Slf4j
public class JGitHelper {

    protected static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    protected String REPO_PATH;

    protected Repository repository;

    protected RevWalk revWalk;

    protected Git git;

    protected static final String FORMAT = "yyyy-MM-dd HH:mm:ss";

    protected static final long TO_MILLISECOND = 1000L;

    public JGitHelper(String repoPath) {
        REPO_PATH = repoPath;
        String gitDir = IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
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
//        log.info(" now git branch :"+git.branchRename().);
    }

    public String getRepoPath() {
        return REPO_PATH;
    }

    public void checkout(String commit) {
        try {
            if (commit == null) {
                commit = repository.getBranch();
            }
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            CheckoutCommand checkoutCommand = git.checkout();
            checkoutCommand.setName(commit).call();
        } catch (Exception e) {
            log.info("before error commit=: " + commit);
            log.error("JGitHelper checkout error:{} ", e.getMessage());
        }
    }

    public String getAuthorName(String commit) {
        String authorName = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            authorName = revCommit.getAuthorIdent().getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return authorName;
    }

    //    @SneakyThrows
    public Date getCommitDateTime(String commit) {
        Date res;
        if (getCommitTime(commit) == null) {
            log.error("getCommitTime(commit)==null");
            return null;
        } else {
            try {
                res = new SimpleDateFormat(FORMAT).parse(getCommitTime(commit));

            } catch (ParseException e) {
                res = null;
                log.error("ParseException:" + e.getMessage());
            }

        }
        return res;
    }

    public String getCommitTime(String commit) {
        String time = null;
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            int t = revCommit.getCommitTime();
            long timestamp = Long.parseLong(String.valueOf(t)) * TO_MILLISECOND;
            Date date = new java.util.Date(timestamp);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(Calendar.HOUR_OF_DAY, -8);
            time = new java.text.SimpleDateFormat(FORMAT).format(calendar.getTime());
        } catch (Exception e) {
            log.error("error in revWalk.parseCommit(ObjectId.fromString(commit)):");
            log.error(e.getMessage());
        }
        return time;
    }

    /**
     * @param branch, date is timestamp that unit is s
     * @return commitid
     * @Description get the closest commit near and before date
     * @author shaoxi
     */
    public String gettoScanCommit(String branch, int date) {
        checkout(branch);
        String resCommit = null;
        int latest = 0;
        try {
            Iterable<RevCommit> commits = git.log().call();
            Iterator<RevCommit> iterator = commits.iterator();
            while (iterator.hasNext()) {
                RevCommit oneCommt = iterator.next();
                int thisCommitTime = oneCommt.getCommitTime();
                if (thisCommitTime < date) {
                    if (thisCommitTime > latest) {
                        resCommit = oneCommt.getName();
                        latest = thisCommitTime;
                    }
                }
            }
        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
        log.info("to scan time : "+getCommitDateTime(resCommit));
        return resCommit;

    }
    /**
     * @param branch
     * @return commitid
     * @Description get the latest commit of the branch
     * @author shaoxi
     */
    public String getLatestCommitByBranch(String branch) {
        checkout(branch);
        String latestCommit = null;
        int latest = 0;
        try {
            Iterable<RevCommit> commits = git.log().call();
            for (RevCommit oneCommit : commits) {
                if (oneCommit.getCommitTime() > latest) {
                    latestCommit = oneCommit.getName();
                    latest = oneCommit.getCommitTime();
                }

            }

        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
        return latestCommit;

    }

    /**
     * 根据策略获取扫描列表
     *
     * @param branch        branch
     * @param beginCommit   begin commit
     * @param scannedCommit scannedCommit
     * @return commit list
     */
    public List<String> getScanCommitListByBranchAndBeginCommit(String branch, String beginCommit, List<String> scannedCommit) {
        //checkout to the branch
        checkout(branch);
        //new result set
        Map<String, Set<String>> commitMap = new HashMap<>(512);
        Map<String, Boolean> commitCheckMap = new HashMap<>(512);
        List<String> scanCommitQueue = new LinkedList<>();
        //get the start commit time
        Long start = getLongCommitTime(beginCommit);
        //init commitMap:key->commitId,value->set<String> parentsCommitId
        try {
            Iterable<RevCommit> commits = git.log().call();
            commits.forEach(commit -> {
                if (commit.getCommitTime() * TO_MILLISECOND >= start) {
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
        while (scanCommitQueue.size() != commitMap.size()) {
            for (Map.Entry<String, Set<String>> entry : commitMap.entrySet()) {
                //if parent in commitMap but not in scanCommitQueue, should not add to queue.
                boolean shouldAddToQueue = shouldAddToQueue(entry.getValue(), scanCommitQueue, commitMap);
                boolean isInScanCommitQueue = commitCheckMap.get(entry.getKey());
                if (shouldAddToQueue && !isInScanCommitQueue) {
                    scanCommitQueue.add(entry.getKey());
                    commitCheckMap.put(entry.getKey(), true);
                    break;
                }
            }
        }
        scannedCommit.forEach(commit -> scanCommitQueue.removeIf(r -> r.equals(commit)));
        return scanCommitQueue;
    }

    public Long getLongCommitTime(String version) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            return revCommit.getCommitTime() * TO_MILLISECOND;
        } catch (Exception e) {
            log.error("error in getLongCommitTime:" + e.getMessage());
            return 0L;
        }
    }

    private boolean shouldAddToQueue(Set<String> parents, List<String> scanCommitQueue, Map<String, Set<String>> commitMap) {
        for (String parent : parents) {
            if (commitMap.containsKey(parent) && !scanCommitQueue.contains(parent)) {
                return false;
            }
        }
        return true;
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
        st.sorted(Map.Entry.comparingByValue()).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
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
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return new String[0];
    }

    public Map<String, List<DiffEntry>> getMappedFileList(String commit) {
        Map<String, List<DiffEntry>> result = new HashMap<>(8);
        try {
            RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
            RevCommit[] parentCommits = currCommit.getParents();
            for (RevCommit p : parentCommits) {
                RevCommit parentCommit = revWalk.parseCommit(ObjectId.fromString(p.getName()));
                try (ObjectReader reader = git.getRepository().newObjectReader()) {
                    CanonicalTreeParser currTreeIter = new CanonicalTreeParser();
                    currTreeIter.reset(reader, currCommit.getTree().getId());

                    CanonicalTreeParser parentTreeIter = new CanonicalTreeParser();
                    parentTreeIter.reset(reader, parentCommit.getTree().getId());
                    DiffFormatter diffFormatter = new DiffFormatter(DisabledOutputStream.INSTANCE);
                    diffFormatter.setRepository(git.getRepository());
                    List<DiffEntry> entries = diffFormatter.scan(currTreeIter, parentTreeIter);
                    result.put(parentCommit.getName(), entries);
                }
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据两个commit id 来diff两个
     *
     * @param preCommitId      前一个版本的commit id
     * @param commitId         当前版本的commit id
     * @param curFileToPreFile curFileToPreFile
     * @param preFileToCurFile preFileToCurFile
     * @return add : ,a delete: a,   change a,a   英文逗号 ， 区分 add delete change
     */
    public List<String> getDiffFilePair(String preCommitId, String commitId, Map<String, String> preFileToCurFile, Map<String, String> curFileToPreFile) {
        //new result list
        List<String> result = new ArrayList<>();
        //init git diff
        CanonicalTreeParser oldTreeDiff = new CanonicalTreeParser();
        CanonicalTreeParser newTreeDiff = new CanonicalTreeParser();
        try (ObjectReader reader = repository.newObjectReader()) {
            //get diff tree
            oldTreeDiff.reset(reader, repository.resolve(preCommitId + "^{tree}"));
            newTreeDiff.reset(reader, repository.resolve(commitId + "^{tree}"));
            //call git diff command
            List<DiffEntry> diffs = getDiffEntry(getRevCommit(preCommitId), getRevCommit(commitId), 60);
            String separation = ",";
            for (DiffEntry diff : diffs) {
                switch (diff.getChangeType()) {
                    case ADD:
                        result.add(separation + diff.getNewPath());
                        break;
                    case DELETE:
                        result.add(diff.getOldPath() + separation);
                        break;
                    default:
                        result.add(diff.getOldPath() + separation + diff.getNewPath());
                        preFileToCurFile.put(diff.getOldPath(), diff.getNewPath());
                        curFileToPreFile.put(diff.getNewPath(), diff.getOldPath());
                }
            }
        } catch (Exception e) {
            log.error("get diff file failed!pre commit is: {}, cur commit is: {}", preCommitId, commitId);
        }
        return result;
    }

    /**
     * 如 0 -》 1 》 2  那 getAllCommitParents（2） = {2，1，0}
     * 得到这个commit所有的parent（包含此commit）
     **/
    public List<String> getAllCommitParents(String commit) {
        List<String> parentCommitList = new ArrayList<>();
        Queue<String> parentCommitQueue = new LinkedList<>();
        parentCommitQueue.offer(commit);
        while (!parentCommitQueue.isEmpty()) {
            String indexCommit = parentCommitQueue.poll();
            parentCommitList.add(indexCommit);
            RevCommit[] parents = getRevCommit(indexCommit).getParents();
            for (RevCommit parent : parents) {
                if (!parentCommitList.contains(parent.getName()) && !parentCommitQueue.contains(parent.getName())) {
                    parentCommitQueue.offer(parent.getName());
                }
            }
        }
        return parentCommitList;
    }

    @SneakyThrows
    public RevCommit getRevCommit(String commitId) {
        return revWalk.parseCommit(repository.resolve(commitId));
    }

    @SneakyThrows
    public List<DiffEntry> getDiffEntry(RevCommit parentCommit, RevCommit currCommit, int score) {
        // 不可少 否则parentCommit的 tree为null
        parentCommit = revWalk.parseCommit(ObjectId.fromString(parentCommit.getName()));
        TreeWalk tw = new TreeWalk(repository);
        tw.addTree(parentCommit.getTree());
        tw.addTree(currCommit.getTree());
        tw.setRecursive(true);
        RenameDetector rd = new RenameDetector(repository);
        rd.addAll(DiffEntry.scan(tw));
        rd.setRenameScore(score);
        return rd.compute();
    }
}
