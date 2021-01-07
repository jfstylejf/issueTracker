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

    private final static String TARGET_STR = "target";

    public interface Filter {
        boolean filter(int level, String path, File file);
    }

    public interface FileHandler {
        void handle(int level, String path, File file);
    }

    private final Filter filter;
    private final FileHandler fileHandler;

    public DirExplorer(Filter filter, FileHandler fileHandler) {
        this.filter = filter;
        this.fileHandler = fileHandler;
    }

    public void explore(File root) {
        explore(0, "", root);
    }

    private void explore(int level, String path, File file) {
        if (file.isDirectory() && file.listFiles() != null && Objects.requireNonNull(file.listFiles()).length > 0) {
            for (File child : Objects.requireNonNull(file.listFiles())) {
                explore(level + 1, path + "/" + child.getName(), child);
            }
        } else {
            if (filter.filter(level, path, file)) {
                fileHandler.handle(level, path, file);
            }
        }
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
            if(file.getAbsolutePath().endsWith(TARGET_STR)){
                String filePath = file.getAbsolutePath();
                String pomPath = filePath.substring (0, filePath.indexOf (TARGET_STR))  + "pom.xml";
                String srcPath = filePath.substring (0, filePath.indexOf (TARGET_STR))  + "src";
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
}