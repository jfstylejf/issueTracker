package cn.edu.fudan.measureservice.util;


/**
 * @author wjzho
 */
public class FileUtil {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String WINDOWS_SEPARATOR = "\\\\";
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
        if (IS_WINDOWS && source.contains(LINUX_SEPARATOR)) {
            return source.replace(LINUX_SEPARATOR,WINDOWS_SEPARATOR);
        }
        return source;
    }



}