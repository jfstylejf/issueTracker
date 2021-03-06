package cn.edu.fudan.issueservice.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * @description:
 * @author: fancying
 * @create: 2019-06-05 17:16
 **/
@SuppressWarnings("Duplicates")
@Slf4j
public class JGitHelper {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private String REPO_PATH;

    private Repository repository;

    private RevWalk revWalk;

    private Git git;

    private static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static final long TO_MILLISECOND = 1000L;

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

    @SneakyThrows
    public Date getCommitDateTime(String commit) {
        return new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).parse(getCommitTime(commit));
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
            time = new java.text.SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

    /**
     * ??????????????????????????????
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
            log.error(e.getMessage());
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
     * ??????????????????
     * st.sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue())).forEach(e -> result.put(e.getKey(), e.getValue()));
     * ????????????????????????
     * ?????? V ???????????? Comparable ??????????????????????????????????????? V ??? V ????????????????????????????????????V ??????????????????V ?????????????????????????????????????????????????????????????????????
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
     * ????????????commit id ???diff??????
     *
     * @param preCommitId      ??????????????????commit id
     * @param commitId         ???????????????commit id
     * @param curFileToPreFile curFileToPreFile
     * @param preFileToCurFile preFileToCurFile
     * @return add : ,a delete: a,   change a,a   ???????????? ??? ?????? add delete change
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
     * ??? 0 -??? 1 ??? 2  ??? getAllCommitParents???2??? = {2???1???0}
     * ????????????commit?????????parent????????????commit???
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
    private List<DiffEntry> getDiffEntry(RevCommit parentCommit, RevCommit currCommit, int score) {
        // ????????? ??????parentCommit??? tree???null
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
