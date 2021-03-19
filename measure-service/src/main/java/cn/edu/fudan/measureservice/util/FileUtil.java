package cn.edu.fudan.measureservice.util;

import java.io.File;

/**
 * @author wjzho
 */
public class FileUtil {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String WINDOWS_SEPARATOR = "\\\\";
    private static final String LINUX_SEPARATOR = "/";
    public static String pathJoint(String prefix, String postfix) {
        return new StringBuilder().append(getExecutablePath(prefix)).append(postfix).toString();
    }

    public static String getRelativePath(String repoPath, String source) {
        return source.replace(getExecutablePath(repoPath),"");
    }

    public static String getExecutablePath(String repoPath) {
        String separator = IS_WINDOWS ? WINDOWS_SEPARATOR : LINUX_SEPARATOR;
        if (!repoPath.endsWith(separator)) {
            return new StringBuilder().append(repoPath).append(separator).toString();
        }else {
            return repoPath;
        }
    }

    public static String systemAvailablePath(String source) {
        if(!IS_WINDOWS && source.contains(WINDOWS_SEPARATOR)) {
            return source.replace(WINDOWS_SEPARATOR,LINUX_SEPARATOR);
        }
        if (IS_WINDOWS && source.contains(LINUX_SEPARATOR)) {
            return source.replace(LINUX_SEPARATOR,WINDOWS_SEPARATOR);
        }
        return source;
    }



}
