package cn.edu.fudan.measureservice.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
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
import java.util.*;
import java.util.stream.Stream;

import static cn.edu.fudan.measureservice.util.DateTimeUtil.timeTotimeStamp;

/**
 * description:
 * @author fancying
 * create: 2019-06-05 17:16
 **/
@SuppressWarnings("Duplicates")
@Slf4j
public class JGitHelper {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final int MERGE_WITH_CONFLICT = -1;
    private static final int MERGE_WITHOUT_CONFLICT = 2;
    private static final int NOT_MERGE = 1;
    private static Boolean Flag;

    private Repository repository;
    private RevWalk revWalk;
    private Git git;


    /**
     * repoPath 加上了 .git 目录
     */
    public JGitHelper(String repoPath,String projectName) {
        if("区块链".equals(projectName)) {
            Flag = true;
        }else {
            Flag = false;
        }
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
            log.error(e.getMessage());
        }
    }

    public void checkout(String commit) {
        try {
            git.reset().setMode(ResetCommand.ResetType.HARD).call();
            CheckoutCommand checkoutCommand = git.checkout();
            checkoutCommand.setName(commit).call();
        } catch (Exception e) {
            // todo org.eclipse.jgit.api.errors.JGitInternalException: Exception caught during execution of reset command. Cannot lock 删除git文件夹下面的lock文件
            e.printStackTrace();
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


    /**
     * fixme 只能根据commit date来确定需要扫描的commit list【author date会造成乱序问题】
     */
    public List<String> getCommitListByBranchAndBeginCommit(String branchName, String beginCommit, Boolean isUpdate) {
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
                if (isUpdate && commitTime > start) {
                    commitMap.put(commit.getName(), commitTime);
                    continue;
                }
                if (!isUpdate && commitTime >= start) {
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
            log.error(e.getMessage());
        }
        return null;
    }

    private Map<String, List<DiffEntry>> getMappedFileList(String commit) {
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
            int startTimeStamp = Integer.parseInt(timeTotimeStamp(startTime));
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
        } catch (GitAPIException | ParseException e) {
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

    @SneakyThrows
    public RevCommit getRevCommit(String commitId){
        return revWalk.parseCommit(repository.resolve(commitId));
    }

    //判断当前commit是否是最初始的那个commit
    public boolean isInitCommit(RevCommit revCommit){
        RevCommit[] parents = revCommit.getParents();
        return parents.length == 0;
    }

    //判断该次commit是否是merge
    public boolean isMerge(RevCommit revCommit){
        RevCommit[] parents = revCommit.getParents();
        return parents.length == 2;
    }

    //判断该次commit的提交信息message
    public String getCommitMessage(RevCommit revCommit){
        return revCommit.getShortMessage();
    }

    /**
     *获取本次commit工作量行数数据（包括新增行数、删除行数、新增注释行、删除注释行、新增空白行、删除空白行）
     */
    public  Map<String, Integer> getLinesData(List<DiffEntry> diffEntryList) throws IOException{
        Map<String,Integer> map = new HashMap<>();
        int sumAddLines = 0;
        int sumDelLines = 0;
        int sumAddCommentLines = 0;
        int sumDelCommentLines = 0;
        int sumAddWhiteLines = 0;
        int sumDelWhiteLines = 0;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);

        //如果加上这句，就是在比较的时候不计算空格，WS的意思是White Space
        df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        df.setRepository(repository);

        //以下循环是针对每一个有变动的文件
        for (DiffEntry entry : diffEntryList) {
            if(!Flag) {
                if (FileFilter.javaFilenameFilter(entry.getNewPath()) && FileFilter.javaFilenameFilter(entry.getOldPath())){
                    continue;
                }
            }
            df.format(entry);
            String diffText = out.toString("UTF-8");

//            String fullName = entry.getNewPath();
//            System.out.println("正在匹配文件：" + fullName);//变更文件的路径
//            System.out.println(diffText);
            int addWhiteLines = 0;
            int delWhiteLines = 0;
            int addCommentLines = 0;
            int delCommentLines = 0;
            String[] diffLines = diffText.split("\n");
            for (String line : diffLines){
                //若是增加的行，则执行以下筛选语句
                if (line.startsWith("+") && ! line.startsWith("+++")){
                    //去掉开头的"+"
                    line = line.substring(1);
                    //去掉头尾的空白符
                    line = line.trim();
                    if (line.matches("^[\\s]*$")){//匹配空白行
                        addWhiteLines++;
                    }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){//匹配注释行
                        addCommentLines++;
                    }
                }
                //若是删除的行，则执行以下筛选语句
                if (line.startsWith("-") && ! line.startsWith("---")){
                    //去掉开头的"-"
                    line = line.substring(1);
                    //去掉头尾的空白符
                    line = line.trim();
                    if (line.matches("^[\\s]*$")){//匹配空白行
                        delWhiteLines++;
                    }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){//匹配注释行
                        delCommentLines++;
                    }
                }
            }

            //对单个文件中的注释行数进行累加，计算到总的注释行当中去
            sumAddCommentLines = sumAddCommentLines + addCommentLines;
            sumDelCommentLines = sumDelCommentLines + delCommentLines;
            sumAddWhiteLines += addWhiteLines;
            sumDelWhiteLines += delWhiteLines;

            // 获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
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
            sumAddLines = sumAddLines + addSize - addCommentLines - addWhiteLines;
            sumDelLines = sumDelLines + subSize - delCommentLines - delWhiteLines;
            out.reset();
        }
        map.put("addLines", sumAddLines);
        map.put("delLines", sumDelLines);
        map.put("addCommentLines", sumAddCommentLines);
        map.put("delCommentLines", sumDelCommentLines);
        map.put("addWhiteLines", sumAddWhiteLines);
        map.put("delWhiteLines", sumDelWhiteLines);
        return map;
    }

    /**
     * 获取本次commit每个文件的工作量行数数据（包括新增行数、删除行数、新增注释行、删除注释行、新增空白行、删除空白行）
     */
    public List<Map<String,Object>> getFileLinesData(String commitId) throws IOException{
        List<DiffEntry> diffEntryList = getDiffEntry(commitId);
        List<Map<String,Object>> result = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        //如果加上这句，就是在比较的时候不计算空格，WS的意思是White Space
        df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        df.setRepository(repository);

        //以下循环是针对每一个有变动的文件
        for (DiffEntry entry : diffEntryList) {
            Map<String,Object> map = new HashMap<>();
            df.format(entry);
            String diffText = out.toString("UTF-8");
            String fullName = entry.getNewPath();
            map.put("filePath",fullName);
            int addWhiteLines = 0;
            int delWhiteLines = 0;
            int addCommentLines = 0;
            int delCommentLines = 0;
            String[] diffLines = diffText.split("\n");
            for (String line : diffLines){
                //若是增加的行，则执行以下筛选语句
                if (line.startsWith("+") && ! line.startsWith("+++")){
                    //去掉开头的"+"
                    line = line.substring(1);
                    //去掉头尾的空白符
                    line = line.trim();
                    if (line.matches("^[\\s]*$")){//匹配空白行
                        addWhiteLines++;
                    }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){//匹配注释行
                        addCommentLines++;
                    }
                }
                //若是删除的行，则执行以下筛选语句
                if (line.startsWith("-") && ! line.startsWith("---")){
                    //去掉开头的"-"
                    line = line.substring(1);
                    //去掉头尾的空白符
                    line = line.trim();
                    if (line.matches("^[\\s]*$")){//匹配空白行
                        delWhiteLines++;
                    }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){//匹配注释行
                        delCommentLines++;
                    }
                }
            }

            // 获取文件差异位置，从而统计差异的行数，如增加行数，减少行数
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
            int addLines = addSize - addCommentLines - addWhiteLines;
            int delLines = subSize - delCommentLines - delWhiteLines;
            map.put("addLines",addLines);
            map.put("delLines",delLines);
            result.add(map);
            out.reset();
        }

        return result;
    }

    @SneakyThrows
    public List<DiffEntry> getConflictDiffEntryList (String commit) {
        RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
        RevCommit[] parentCommits = currCommit.getParents();
        if (parentCommits.length != 2) {
            return new ArrayList<>();
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

    @SneakyThrows
    public String getSingleParent(String commit) {
        RevCommit currCommit = revWalk.parseCommit(ObjectId.fromString(commit));
        RevCommit[] parentCommits = currCommit.getParents();
        if (parentCommits.length == 0) {
            return null;
        }
        if (parentCommits.length == 1 || isParent2(parentCommits[1], parentCommits[0], currCommit)) {
            return parentCommits[0].getName();
        }

        return parentCommits[1].getName();
    }


    /**
     * @return 通过JGit获取一次commit中开发者的新增行数，删除行数，新增注释行数，删除注释行数
     */
    @SneakyThrows
    public Map<String, Integer> getLinesData(String commitId){
        RevCommit revCommit = getRevCommit(commitId);
        RevCommit[] parentCommits = revCommit.getParents();
        if (parentCommits.length == 0) {
            return null;
        }
        List<DiffEntry> diffEntries = parentCommits.length == 1 ? getDiffEntry(parentCommits[0], revCommit) : getConflictDiffEntryList(commitId);
        return getLinesData(diffEntries);
    }

    public List<DiffEntry> getDiffEntry(String commitId) {
        RevCommit revCommit = getRevCommit(commitId);
        RevCommit[] parentCommits = revCommit.getParents();
        if (parentCommits.length == 0) {
            return null;
        }
        return parentCommits.length == 1 ? getDiffEntry(parentCommits[0], revCommit) : getConflictDiffEntryList(commitId);
    }

    /**
     * fixme merge 情况可能有问题
     * 通过jgit获取修改文件的路径名 list
     */
    @SneakyThrows
    public Map<DiffEntry.ChangeType, List<String>> getDiffFilePathList(String commitId){
        RevCommit revCommit = getRevCommit(commitId);
        RevCommit[] parentCommits = revCommit.getParents();
        if (parentCommits.length == 0) {
            return new HashMap<>(0);
        }
        List<DiffEntry> diffEntries = parentCommits.length == 1 ? getDiffEntry(parentCommits[0], revCommit) : getConflictDiffEntryList(commitId);
        if (diffEntries == null){
            return new HashMap<>(0);
        }
        Map<DiffEntry.ChangeType, List<String>> result = new HashMap<>(8);
        for (DiffEntry.ChangeType c : DiffEntry.ChangeType.values()) {
            result.put(c, new ArrayList<>(8));
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(out);
        df.setRepository(repository);
        //以下循环是针对每一个有变动的文件
        // todo 暂时没考虑rename的情况
        for (DiffEntry entry : diffEntries) {
            List<String> pathList = result.get(entry.getChangeType());
            entry.getChangeType();
            df.format(entry);
            if (entry.getChangeType().equals(DiffEntry.ChangeType.DELETE) && entry.getOldPath() != null) {
                pathList.add(entry.getOldPath());
                continue;
            }
            if (entry.getNewPath() != null) {
                pathList.add(entry.getNewPath());
            }

        }
        return result;
    }






    public static void main(String[] args) throws ParseException {
        String repoPath = "D:\\Project\\FDSELab\\开源项目\\IssueTracker-Master";
        String commitId = "f6b81325edc2c26fa54b62fc63188e35ecec8620";
        JGitHelper jGitHelper = new JGitHelper(repoPath,null);
        Map<String, Integer> map = jGitHelper.getLinesData(commitId);
        System.out.println("addLines :"+map.get("addLines"));
        System.out.println("delLines :"+map.get("delLines"));
        System.out.println("addCommentLines :"+map.get("addCommentLines"));
        System.out.println("delCommentLines :"+map.get("delCommentLines"));
        System.out.println("addWhiteLines :"+map.get("addWhiteLines"));
        System.out.println("delWhiteLines :"+map.get("delWhiteLines"));

        Map<DiffEntry.ChangeType, List<String>> filePaths = jGitHelper.getDiffFilePathList(commitId);
        int changedFiles = 0;
        for (List<String> changedFilePathList : filePaths.values()){
            if (changedFilePathList != null) {
                for (String s : changedFilePathList) {
                    if (! FileFilter.javaFilenameFilter(s)){
                        changedFiles++;
                    }
                }
            }
        }
        System.out.println(changedFiles);
    }

}