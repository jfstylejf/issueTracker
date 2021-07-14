package cn.edu.fudan.measureservice.util;


import cn.edu.fudan.measureservice.filter.FileFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wjzho
 */
public class FileUtil {

    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String WINDOWS_SEPARATOR = "\\\\";
    private static final String SINGLE_WINDOWS_SEPARATOR = "\\";
    private static final String LINUX_SEPARATOR = "/";
    public static String pathJoint(String prefix, String postfix) {
        return getExecutablePath(prefix) + postfix;
    }

    public static String getRelativePath(String repoPath, String source) {
        return source.replace(getExecutablePath(repoPath),"");
    }

    public static String getAbsolutePath(String repoPath,String fileName) {
        return getExecutablePath(repoPath) + fileName;
    }

    public static String getExecutablePath(String repoPath) {
        String separator = IS_WINDOWS ? WINDOWS_SEPARATOR : LINUX_SEPARATOR;
        if (!repoPath.endsWith(separator)) {
            return repoPath + separator;
        }else {
            return repoPath;
        }
    }

    public static String systemAvailablePath(String source) {
        if(!IS_WINDOWS && source.contains(WINDOWS_SEPARATOR)) {
            return source.replace(WINDOWS_SEPARATOR,LINUX_SEPARATOR);
        }
        if(!IS_WINDOWS && source.contains(SINGLE_WINDOWS_SEPARATOR)) {
            return source.replace(SINGLE_WINDOWS_SEPARATOR,LINUX_SEPARATOR);
        }

        if (IS_WINDOWS && !source.contains(WINDOWS_SEPARATOR) && source.contains(LINUX_SEPARATOR)) {
            return source.replace(LINUX_SEPARATOR,WINDOWS_SEPARATOR);
        }
        if(IS_WINDOWS && !source.contains(WINDOWS_SEPARATOR) && source.contains(SINGLE_WINDOWS_SEPARATOR)) {
            return source.replace(SINGLE_WINDOWS_SEPARATOR,WINDOWS_SEPARATOR);
        }
        return source;
    }

    public static List<String> getFilenames(File file, FileFilter filter) {
        List<String> files = new ArrayList<String>();
        getFilenames_(file, files);
        // 根据语言筛选文件
        files.removeIf(filter::fileFilter);
        return files;
    }

    public static void getFilenames_(File f, List<String> files){
        // 若是目录，则遍历其下所有文件
        if (f.isDirectory()) {
            String[] fList = f.list();
            for(int i=0; i < fList.length; i++) {
                getFilenames_(new File(f, fList[i]), files);
            }
        }
        files.add(f.getAbsolutePath());
    }

}
