package cn.edu.fudan.issueservice.util;

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

    /**
     * true: 过滤
     * false： 不过滤
     */
    public static boolean javaFilenameFilter(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        String[] strs = path.split("/");
        String str = strs[strs.length - 1];
        return !str.toLowerCase().endsWith(".java") ||
                path.toLowerCase().contains("/test/") ||
                path.toLowerCase().contains("/.mvn/") ||
                str.toLowerCase().endsWith("test.java") ||
                str.toLowerCase().endsWith("tests.java") ||
                str.toLowerCase().startsWith("test") ||
                str.toLowerCase().endsWith("enum.java") ||
                path.contains(JPMS);
    }

    public static boolean jsFileFilter(String path) {
        String[] strs = path.split("/");
        String str = strs[strs.length-1].toLowerCase();
        return  !str.endsWith(".js") ||
                path.toLowerCase().contains("/test/") ||
                path.toLowerCase().contains("/.mvn/") ||
                path.toLowerCase().contains("lib/") ||
                path.toLowerCase().contains("node_modules/") ||
                path.toLowerCase().contains("target/") ||
                path.toLowerCase().contains("build/") ||
                path.toLowerCase().contains("dist/") ||
                str.endsWith("test.java") ||
                str.endsWith("tests.java") ||
                str.startsWith("test") ||
                str.endsWith("enum.java") ||
                str.endsWith("test.js") ||
                str.endsWith("tests.js") ||
                str.startsWith(".");

    }
}