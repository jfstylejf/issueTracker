package cn.edu.fudan.common.jgit;

import lombok.SneakyThrows;
import org.eclipse.jgit.diff.*;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author Beethoven
 */
public class MeasureJGitHelper extends JGitHelper {

    /**
     * 通用:JGit构造方法
     * @param repoPath 代码库路径
     */
    public MeasureJGitHelper(String repoPath) {
        super(repoPath);
    }

    private static final String JPMS = "module-info.java";

    /**
     * measure-service:
     * @param commit commit id
     * @return RevCommit
     */
    @SneakyThrows
    public RevCommit getRevCommit(String commit){
        return revWalk.parseCommit(repository.resolve(commit));
    }

    public  static boolean javaFilenameFilter(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        path = path.replace("\\","/");
        String[] strs = path.split("/");
        String str = strs[strs.length-1];
        return  !str.toLowerCase().endsWith(".java") ||
                path.toLowerCase().contains("/test/") ||
                path.toLowerCase().contains("/.mvn/") ||
                str.toLowerCase().endsWith("test.java") ||
                str.toLowerCase().endsWith("tests.java") ||
                str.toLowerCase().startsWith("test") ||
                str.toLowerCase().endsWith("enum.java") ||
                path.contains(JPMS);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Integer> getLinesData(List<DiffEntry> diffEntryList) throws IOException {
        Map<String,Integer> map = new HashMap<>(16);
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
            if (javaFilenameFilter(entry.getNewPath()) && javaFilenameFilter(entry.getOldPath())){
                continue;
            }
            df.format(entry);
            String diffText = out.toString("UTF-8");

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
                    //匹配空白行 or 匹配注释行
                    if (line.matches("^[\\s]*$")){
                        addWhiteLines++;
                    }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){
                        addCommentLines++;
                    }
                }
                //若是删除的行，则执行以下筛选语句
                if (line.startsWith("-") && ! line.startsWith("---")){
                    //去掉开头的"-"
                    line = line.substring(1);
                    //去掉头尾的空白符
                    line = line.trim();
                    if (line.matches("^[\\s]*$")){
                        delWhiteLines++;
                    }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){
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
    @SuppressWarnings("unchecked")
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
            Map<String,Object> map = new HashMap<>(8);
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
                    if (line.matches("^[\\s]*$")){
                        addWhiteLines++;
                    }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){
                        addCommentLines++;
                    }
                }
                //若是删除的行，则执行以下筛选语句
                if (line.startsWith("-") && ! line.startsWith("---")){
                    //去掉开头的"-"
                    line = line.substring(1);
                    //去掉头尾的空白符
                    line = line.trim();
                    if (line.matches("^[\\s]*$")){
                        delWhiteLines++;
                    }else if(line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/")){
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

}
