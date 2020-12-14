package cn.edu.fudan.cloneservice.util;

import cn.edu.fudan.cloneservice.domain.CommitChange;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by njzhan
 * <p>
 * Date :2019-08-22
 * <p>
 * Description :
 * <p>
 * Version :1.0
 */
public class JGitUtil {

    private Logger logger = LoggerFactory.getLogger(JGitUtil.class);

    private static final boolean IS_WINDOWS = System.getProperty("os.accountName").toLowerCase().contains("win");
    private static Repository repository;
    private RevWalk revWalk;
    private Git git;

    /**
     *
     * repoPath 加上了 .git 目录
     *
     */
    public JGitUtil(String repoPath) {
        String gitDir =  IS_WINDOWS ? repoPath + "\\.git" : repoPath + "/.git";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        try {
            repository = builder.setGitDir(new File(gitDir))
                    .readEnvironment() // cn.edu.fudan.common.scan environment GIT_* variables
                    .findGitDir() // cn.edu.fudan.common.scan up the file system tree
                    .build();
            git = new Git(repository);
            revWalk = new RevWalk(repository);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public Long getLongCommitTime(String version) {
        try {
            RevCommit revCommit = revWalk.parseCommit(ObjectId.fromString(version));
            return revCommit.getCommitTime() * 1000L;
        }catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        Map<K, V> result = new LinkedHashMap<>();
        Stream<Map.Entry<K, V>> st = map.entrySet().stream();
        st.sorted(Comparator.comparing(Map.Entry::getValue)).forEach(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    public List<String> getCommitListByBranchAndBeginCommit(String beginCommit) {
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

    private boolean isMerge(RevCommit revCommit){
        RevCommit[] parents = revCommit.getParents();
        return parents.length == 2;
    }

    public List<String> getCommitList(String repoPath, String startDate, String endDate, String developer) {

        Map<String, Long> commitMap = new HashMap<>(512);

        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        Repository repository;
        try {
            repository = repositoryBuilder.setGitDir(new File(repoPath + "/.git"))
                    .readEnvironment() // cn.edu.fudan.common.scan environment GIT_* variables
                    .findGitDir() // cn.edu.fudan.common.scan up the file system tree
                    .setMustExist(true)
                    .build();
            Git git = new Git(repository);
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                long start = dateFormat.parse(startDate).getTime();
                long end = dateFormat.parse(endDate).getTime();
                Iterable<RevCommit> commits = git.log().call();
                for (RevCommit commit : commits) {
                    if(isMerge(commit)){
                        continue;
                    }
                    long commitTime = commit.getCommitTime() * 1000L;
                    if(developer != null){
                        if (commitTime <= end && commitTime >= start && commit.getAuthorIdent().getName().equals(developer)) {
                            commitMap.put(commit.getName(), commit.getCommitTime() * 1000L);
                        }
                    }else {
                        if (commitTime <= end && commitTime >= start) {
                            commitMap.put(commit.getName(), commit.getCommitTime() * 1000L);
                        }
                    }

                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (NoHeadException e) {
                e.printStackTrace();
            } catch (GitAPIException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        // find the current commit id
        return new ArrayList<>(sortByValue(commitMap).keySet());
    }

    public static boolean isSameDeveloperClone(String repoPath, String commitId, String filePath, String nums){

        boolean isSameDeveloperClone = false;

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setMustExist(true);
        builder.addCeilingDirectory(new File(repoPath));
        builder.findGitDir(new File(repoPath));
        String[] num = nums.split(",");
        int start = Integer.parseInt(num[0]);
        int end = Integer.parseInt(num[1]);
        int sameDeveloperCloneLines = 0;
        try {
            Repository repository = builder.build();
            RevCommit revCommit = getCurrentRevCommit(repoPath,commitId);
            String developerName = revCommit.getAuthorIdent().getName();
            ObjectId curCommitId = repository.resolve(commitId);
            BlameCommand blamer = new BlameCommand(repository);
            blamer.setStartCommit(curCommitId);
            blamer.setFilePath(filePath);
            try {
                BlameResult blame = blamer.call();
                for(int i = start; i <= end; i++){
                    PersonIdent personIdent =  blame.getSourceAuthor(i);
                    if(personIdent.getName().equals(developerName)){
                        sameDeveloperCloneLines++;
                    }
                }

                if(sameDeveloperCloneLines >= (end - start)/2){
                    isSameDeveloperClone = true;
                }

            } catch (GitAPIException e) {
                e.printStackTrace();
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

        return isSameDeveloperClone;

    }

    public static Set<String> getDeveloperList(String repoPath){
        Set<String> spi = new HashSet<>();
        try {
            FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
            Repository repository = repositoryBuilder.setGitDir(new File(repoPath + "/.git"))
                    .readEnvironment() // cn.edu.fudan.common.scan environment GIT_* variables
                    .findGitDir() // cn.edu.fudan.common.scan up the file system tree
                    .setMustExist(true)
                    .build();
            // find the current commit id
            Git git = new Git(repository);
            try {
                Iterable<RevCommit> log = git.log().call();
                for(RevCommit revCommit:log){
                    spi.add(revCommit.getAuthorIdent().getName()) ;
                }
            } catch (GitAPIException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return spi;
    }

    private static CommitChange getNewlyIncreasedLinesNum(String repoPath, List<DiffEntry> diffEntryList) throws IOException{

        CommitChange commitChange = new CommitChange();

        Map<String, String> map = new HashMap<>();
        int addLines = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);

        //如果加上这句，就是在比较的时候不计算空格，WS的意思是White Space
        df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        df.setRepository(repository);

        //以下循环是针对每一个有变动的文件
        for (DiffEntry entry : diffEntryList) {
            df.format(entry);
            String fileName = entry.toString().split(" ")[1];
            fileName = fileName.substring(0, fileName.length() - 1);
            FileHeader fileHeader = df.toFileHeader(entry);
            List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
            for(HunkHeader hunkHeader:hunks){
                EditList editList = hunkHeader.toEditList();
                for(Edit edit : editList){
                    addLines += edit.getEndB() - edit.getBeginB();
                    for(int i = edit.getBeginB()+1; i <= edit.getEndB(); i++){
                        //先判断对应文件的行是否是空行或者注释
                        if(isEmpty(repoPath, fileName, i)){
                            continue;
                        }
                        map.merge(fileName, i+"", (v1, v2) -> v1 + "," + v2);
                    }

                }
            }
        }
        commitChange.setAddLines(addLines);
        commitChange.setAddMap(map);

        return commitChange;
    }

    private static boolean isEmpty(String repoPath, String filePath, int num){
        int line = 0;
        String s;
        filePath = repoPath + "/" + filePath;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            while ((s = bufferedReader.readLine()) != null) {
                line++;
                if (line == num) {
                    if(!s.isEmpty() && !s.trim().startsWith("//")
                            && !s.trim().startsWith("/**")
                            && !s.trim().startsWith("*")){
                        return true;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 获取某次commit的修改文件列表(只统计java文件，并且去除test文件)
     * @param revCommit
     * @param repo
     * @return
     */
    public static List<DiffEntry> getChangedFileList(RevCommit revCommit, Repository repo) {
        List<DiffEntry> returnDiffs = null;

        try {
            //默认比较时间轴上最近的那个commit
            RevCommit previousCommit=getPrevHash(revCommit,repo);

            String currentName = revCommit.getAuthorIdent().getName();
            RevCommit[] parents = revCommit.getParents();
            //merge的情况，获取与当前commit开发者相同的那个commit
            if (parents.length == 2){
                if (parents[0].getAuthorIdent().getName().equals(currentName) && !parents[1].getAuthorIdent().getName().equals(currentName)){
                    previousCommit=parents[0];
                }else if (!parents[0].getAuthorIdent().getName().equals(currentName) && parents[1].getAuthorIdent().getName().equals(currentName)){
                    previousCommit=parents[1];
                }
            }
            if(previousCommit==null){
                return null;
            }

            ObjectId head=revCommit.getTree().getId();

            ObjectId oldHead=previousCommit.getTree().getId();

            // prepare the two iterators to compute the diff between
            try (ObjectReader reader = repo.newObjectReader()) {
                CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
                oldTreeIter.reset(reader, oldHead);
                CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
                newTreeIter.reset(reader, head);

                // finally get the list of changed files
                try (Git git = new Git(repo)) {
                    List<DiffEntry> diffs= git.diff()
                            .setNewTree(newTreeIter)
                            .setOldTree(oldTreeIter)
                            .call();
                    for(int i = diffs.size() - 1; i >= 0; i--){
                        String fullName = diffs.get(i).getNewPath().toLowerCase();
                        String shortName = fullName.substring(fullName.lastIndexOf('/') + 1);
                        //只统计java文件
                        if (fullName.endsWith(".java")){
                            //并且去除其中的test文件
                            if (fullName.contains("/test/") ||
                                    shortName.endsWith("test.java") ||
                                    shortName.endsWith("tests.java") ||
                                    shortName.startsWith("test")){
                                //去除其中的test文件
                                diffs.remove(i);
                            }
                        }else {
                            //去除非java文件
                            diffs.remove(i);
                        }
                    }
                    returnDiffs=diffs;
                } catch (GitAPIException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return returnDiffs;
    }

    /**
     * 获取上一次commit（时间轴上最近的那次commit）
     * @param commit
     * @param repo
     * @return
     * @throws IOException
     */
    private static RevCommit getPrevHash(RevCommit commit, Repository repo)  throws  IOException {
        try (RevWalk walk = new RevWalk(repo)) {
            // Starting point
            walk.markStart(commit);
            int count = 0;
            for (RevCommit rev : walk) {
                // got the previous commit.
                if (count == 1) {
                    return rev;
                }
                count++;
            }
            walk.dispose();
        }
        //Reached end and no previous commits.
        return null;
    }

    private static RevCommit getCurrentRevCommit(String repoPath, String commitId) throws IOException{
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setMustExist(true);
        builder.addCeilingDirectory(new File(repoPath));
        builder.findGitDir(new File(repoPath));
        repository=builder.build();
        RevWalk walk = new RevWalk(repository);
        ObjectId versionId=repository.resolve(commitId);
        return walk.parseCommit(versionId);

    }

    public static String getPreCommitId(String repoPath, String commitId){
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setMustExist(true);
        builder.addCeilingDirectory(new File(repoPath));
        builder.findGitDir(new File(repoPath));
        String commit = null;
        try {
            Repository repository = builder.build();
            RevCommit revCommit = getCurrentRevCommit(repoPath,commitId);
            RevCommit preCommit = getPrevHash(revCommit, repository);
            if(preCommit != null){
                commit = preCommit.getName();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return commit;
    }

    public static CommitChange getNewlyIncreasedLines(String repoPath, String commitId){

        CommitChange commitChange = new CommitChange();

        //Map<String, String> map = new HashMap<>(512);
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setMustExist(true);
        builder.addCeilingDirectory(new File(repoPath));
        builder.findGitDir(new File(repoPath));
        try {
            Repository repository = builder.build();
            RevCommit revCommit = getCurrentRevCommit(repoPath,commitId);
            //获取变更的文件列表
            List<DiffEntry> diffFix = getChangedFileList(revCommit,repository);
            if(diffFix != null){
                commitChange = getNewlyIncreasedLinesNum(repoPath, diffFix);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return commitChange;

    }

}
