package cn.edu.fudan.issueservice.util;

import java.util.regex.Pattern;

/**
 * description:
 *
 * @author fancying
 * create: 2020-01-06 14:08
 **/
public final class FileFilter {
    /**
     * JPMS 模块
     */
    private static final String JPMS = "module-info.java";

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    /**
     * true: 过滤
     * false： 不过滤
     */
    public  static boolean javaFilenameFilter(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
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

    public static boolean jsFileFilter(String filePath) {
        return IS_WINDOWS ? Pattern.matches(".*\\\\(build|dist)\\\\.*", filePath) : Pattern.matches(".*/(build|dist)/.*", filePath);
    }

    public static void main(String[] args) {
        System.out.println(jsFileFilter("C:\\home\\fdse\\static\\js\\main.2c207907.js"));
    }
}