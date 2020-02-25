/**
 * @description:
 * @author: fancying
 * @create: 2019-06-05 17:16
 **/
package cn.edu.fudan.measureservice.util;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static cn.edu.fudan.measureservice.util.DateTimeUtil.timeTotimeStamp;

@SuppressWarnings("Duplicates")
@Slf4j
public class JGitHelper {
    private Logger logger = LoggerFactory.getLogger(JGitHelper.class);

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final int MERGE_WITH_CONFLICT = -1;
    private static final int MERGE_WITHOUT_CONFLICT = 2;
    private static final int NOT_MERGE = 1;
    private static Repository repository;
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
            logger.error(e.getMessage());
        }
    }

    public void checkout(String commit) {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            CheckoutCommand checkoutCommand = git.checkout();
            checkoutCommand.setName(commit).call();
        } catch (Exception e) {
            logger.error("JGitHelper checkout error:{} ", e.getMessage());
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
            time = new SimpleDateFormat(format).format(new Date(timestamp));
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
            logger.error(e.getMessage());
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

    public List<String> getAggregationCommit(String startTime){
        List<String> aggregationCommits = new ArrayList<>();
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
                if (startTimeStamp<revCommit.getCommitTime()&&branch==1) {aggregationCommits.add(revCommit.getName());}
                branch += Optional.ofNullable(sonCommitsMap.get(revCommit.getName())).orElse(0)-1;
            }
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return aggregationCommits;
    }


    //打印commit时间
    static void printTime(int commitTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestampString=String.valueOf(commitTime);
        Long timestamp = Long.parseLong(timestampString) * 1000;
        String date = formatter.format(new Date(timestamp));
        System.out.println("It's commit time: "+date);
    }

    //获取某次commit修改的文件数量
    public static int getChangedFilesCount(List<DiffEntry> diffEntryList){
        return diffEntryList.size();
    }

    //    获取某次commit的修改文件列表
    public static List<DiffEntry> getChangedFileList(RevCommit revCommit, Repository repo) {
        List<DiffEntry> returnDiffs = null;

        try {
            RevCommit previsouCommit=getPrevHash(revCommit,repo);
            if(previsouCommit==null)
                return null;
            ObjectId head=revCommit.getTree().getId();

            ObjectId oldHead=previsouCommit.getTree().getId();

            System.out.println("Printing diff between the Revisions: " + revCommit.getName() + " and " + previsouCommit.getName());

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
                    for (DiffEntry entry : diffs) {
                        System.out.println("Entry: " + entry);
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

    //获取上一次commit（时间轴上最近的那次commit）
    public static RevCommit getPrevHash(RevCommit commit, Repository repo)  throws  IOException {
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

    //根据repopath 和commitid获取当前commit
    public RevCommit getCurrentRevCommit(String repo_path, String commit_id){
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setMustExist(true);
        builder.addCeilingDirectory(new File(repo_path));
        builder.findGitDir(new File(repo_path));
        try{
            repository=builder.build();

            RevWalk walk = new RevWalk(repository);
            ObjectId versionId=repository.resolve(commit_id);
            RevCommit verCommit=walk.parseCommit(versionId);
            return verCommit;

        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }

    //判断该次commit是否是merge
    public boolean isMerge(RevCommit revCommit){
        RevCommit[] parents = revCommit.getParents();
        if (parents.length == 1){
            return false;
        }else{
            return true;
        }
    }

    //获取本次commit的AddLines
    public static int getAddLines(List<DiffEntry> diffEntryList) throws IOException {
        int result = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
//			df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);//如果加上这句，就是在比较的时候不计算空格，WS的意思是White Space
        df.setRepository(repository);
        for (DiffEntry entry : diffEntryList) {

            df.format(entry);
            String diffText = out.toString("UTF-8");
//				System.out.println(diffText);

//            System.out.println(entry.getNewPath());//变更文件的路径

            FileHeader fileHeader = df.toFileHeader(entry);
            List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
            int addSize = 0;
            int subSize = 0;
            for(HunkHeader hunkHeader:hunks){
                EditList editList = hunkHeader.toEditList();
                for(Edit edit : editList){
                    subSize += edit.getEndA()-edit.getBeginA();
                    addSize += edit.getEndB()-edit.getBeginB();
                }
            }
            result = result + addSize;
//            System.out.println("addSize="+addSize);//增加和减少的代码行数统计，我和Git Log核对了一下，还挺准确的。
//            System.out.println("subSize="+subSize);
            out.reset();
        }
        return result;
    }

    //获取本次commit的DelLines
    public static int getDelLines(List<DiffEntry> diffEntryList) throws IOException {
        int result = 0;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
//			df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);//如果加上这句，就是在比较的时候不计算空格，WS的意思是White Space
        df.setRepository(repository);
        for (DiffEntry entry : diffEntryList) {

            df.format(entry);
            String diffText = out.toString("UTF-8");
//				System.out.println(diffText);

//            System.out.println(entry.getNewPath());//变更文件的路径

            FileHeader fileHeader = df.toFileHeader(entry);
            List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
            int addSize = 0;
            int subSize = 0;
            for(HunkHeader hunkHeader:hunks){
                EditList editList = hunkHeader.toEditList();
                for(Edit edit : editList){
                    subSize += edit.getEndA()-edit.getBeginA();
                    addSize += edit.getEndB()-edit.getBeginB();
                }
            }
            result = result + subSize;
//            System.out.println("addSize="+addSize);//增加和减少的代码行数统计，我和Git Log核对了一下，还挺准确的。
//            System.out.println("subSize="+subSize);
            out.reset();
        }
        return result;
    }


    public static void main(String[] args) throws ParseException {
        //gitCheckout("E:\\Lab\\project\\IssueTracker-Master", "f8263335ef380d93d6bb93b2876484e325116ac2");
        //String repoPath = "E:\\Lab\\iec-wepm-develop";
//        String repoPath = "E:\\Project\\FDSELab\\IssueTracker-Master";
////        String commitId = "75c6507e2139e9bb663abf35037b31478e44c616";
//        JGitHelper jGitHelper = new JGitHelper(repoPath);
//        List<String> list = jGitHelper.getAggregationCommit("2019-1-1 00:00:00");
//        list.stream().forEach(System.out::println);

        //String s[] = jGitHelper.getCommitParents(commitId);
//        int m = jGitHelper.mergeJudgment(commitId);
//        System.out.println(m);
//        String t = jGitHelper.getCommitTime("f61e34233aa536cf5e698b502099e12d1caf77e4");
//        for (String s : jGitHelper.getCommitListByBranchAndDuration("zhonghui20191012", "2019.10.12-2019.12.16")) {
//            System.out.println(s);
//            jGitHelper.checkout(s);
//        }
//        String versionTag="v2.6.19";//定位到某一次Commi，既可以使用Tag，也可以使用其hash
        String versionTag="d5c2a8e98e7a21881d9b16a7f56c9e0435bb8386";
        String path="E:\\Project\\FDSELab\\平台相关文档\\开源项目列表\\测试项目\\javafx-maven-plugin";
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        builder.setMustExist(true);
        builder.addCeilingDirectory(new File(path));
        builder.findGitDir(new File(path));
        try {
            repository=builder.build();

            RevWalk walk = new RevWalk(repository);
            ObjectId versionId=repository.resolve(versionTag);
            RevCommit verCommit=walk.parseCommit(versionId);
            RevCommit[] parents = verCommit.getParents();
            System.out.println("父节点的数量为：" + parents.length);

            String commitName=verCommit.getName();
            System.out.println("This version is: "+versionTag+", It hash: "+commitName);//如果通过Tag定位，可以获得其SHA-1 Hash Value
            int verCommitTime=verCommit.getCommitTime();
            printTime(verCommitTime);//打印出本次Commit的时间

            System.out.println("The author is: "+verCommit.getAuthorIdent().getEmailAddress());//获得本次Commit Author的邮箱

            List<DiffEntry> diffFix=getChangedFileList(verCommit,repository);//获取变更的文件列表

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DiffFormatter df = new DiffFormatter(out);
//			df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);//如果加上这句，就是在比较的时候不计算空格，WS的意思是White Space
            df.setRepository(repository);

            for (DiffEntry entry : diffFix) {

                df.format(entry);
                String diffText = out.toString("UTF-8");
//				System.out.println(diffText);

                System.out.println(entry.getNewPath());//变更文件的路径

                FileHeader fileHeader = df.toFileHeader(entry);
                List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
                int addSize = 0;
                int subSize = 0;
                for(HunkHeader hunkHeader:hunks){
                    EditList editList = hunkHeader.toEditList();
                    for(Edit edit : editList){
                        subSize += edit.getEndA()-edit.getBeginA();
                        addSize += edit.getEndB()-edit.getBeginB();
                    }
                }
                System.out.println("addSize="+addSize);//增加和减少的代码行数统计，我和Git Log核对了一下，还挺准确的。
                System.out.println("subSize="+subSize);
                out.reset();
            }
            System.out.println("该commit修改的文件数量为：" + getChangedFilesCount(diffFix));
            System.out.println("addLine:"+ getAddLines(diffFix));
            System.out.println("delLine:"+ getDelLines(diffFix));
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}