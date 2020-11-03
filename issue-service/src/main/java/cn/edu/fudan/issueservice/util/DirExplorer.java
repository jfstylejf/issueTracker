package cn.edu.fudan.issueservice.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * description: 遍历得到所有的文件
 * @author fancying
 * create: 2019-05-24 11:52
 **/
public class DirExplorer {

    public interface Filter {
        boolean filter(int level, String path, File file);
    }

    public interface FileHandler {
        void handle(int level, String path, File file);
    }

    private Filter filter;
    private FileHandler fileHandler;

    public DirExplorer(Filter filter, FileHandler fileHandler) {
        this.filter = filter;
        this.fileHandler = fileHandler;
    }

    public void explore(File root) {
        explore(0, "", root);
    }

    private void explore(int level, String path, File file) {
        if (file.isDirectory() && file.listFiles() != null && file.listFiles().length > 0) {
            for (File child : file.listFiles()) {
                explore(level + 1, path + "/" + child.getName(), child);
            }
        } else {
            if (filter.filter(level, path, file)) {
                fileHandler.handle(level, path, file);
            }
        }
    }

    /**
     * 给一个文件名，输出某个目录下所有同名文件的全路径
     * @param fileName 文件名 需要带上文件的后缀 如 a.java b.xml
     * @param dir 文件路径
     * @return 与fileName同名的所有文件路径
     */
    public static List<String> findSameNameFile(String fileName, String dir) {
        File fileDir = new File(dir);
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> path.endsWith(fileName),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(fileDir);
        return pathList;
    }

    public void exploreDir(File root) {
        exploreDir(0, "", root);
    }

    private void exploreDir(int level, String path, File file) {
        if (file.isDirectory()) {
            if (filter.filter(level, path, file)) {
                fileHandler.handle(level, path, file);
            }
            if(file.listFiles() != null){
                for (File child : Objects.requireNonNull(file.listFiles())) {
                    exploreDir(level + 1, path + "/" + child.getName(), child);
                }
            }

        }

    }

    public static void deleteRedundantTarget(String repoPath) {
        new DirExplorer ((level, path, file) ->
        {
            if(file.getAbsolutePath().endsWith("target")){
                String filePath = file.getAbsolutePath();
                String pomPath = filePath.substring (0, filePath.indexOf ("target"))  + "pom.xml";
                String srcPath = filePath.substring (0, filePath.indexOf ("target"))  + "src";
                File pomFile = new File (pomPath);
                File srcFile = new File (srcPath);
                return !pomFile.exists () || !srcFile.exists ();
            }
            return false;
        },
                (level, path, file) -> {
                    deleteAllByPath(file);
                    file.delete();
                }).exploreDir(new File(repoPath));
    }

    private static void deleteAllByPath(File rootFilePath) {
        File[] needToDeleteFiles = rootFilePath.listFiles();
        if (needToDeleteFiles == null) {
            return;
        }
        for (int i = 0; i < needToDeleteFiles.length; i++) {
            if (needToDeleteFiles[i].isDirectory()) {
                deleteAllByPath(needToDeleteFiles[i]);
            }
            try {
                Files.delete(needToDeleteFiles[i].toPath());
            } catch (IOException e) {
                System.out.println ("Delete temp directory or file failed." + e.getMessage());
            }
        }
    }


    public static void main(String[] args) {
        deleteRedundantTarget("E:\\school\\laboratory\\IssueTracker-main\\IssueTracker-Master");
    }

}