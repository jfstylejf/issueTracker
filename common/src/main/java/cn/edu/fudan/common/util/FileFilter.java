package cn.edu.fudan.common.util;

public final class FileFilter {
    /**
     * JPMS 模块
     */
    private static final String JPMS = "module-info.java";
    /**
     * true: 过滤
     * false： 不过滤
     */
    public static boolean filenameFilter(String filePath) {
        String path = filePath.replace("\\","/").toLowerCase();
        String[] strs = path.split("/");
        String str = strs[strs.length-1].toLowerCase();
        return  !(str.endsWith(".java") || str.endsWith(".js")) ||
                path.contains("/test/") ||
                path.contains("/.mvn/") ||
                path.contains("lib/") ||
                path.contains("node_modules/") ||
                path.contains("target/") ||
                path.contains("build/") ||
                path.contains("dist/") ||
                str.endsWith("test.java") ||
                str.endsWith("tests.java") ||
                str.startsWith("test") ||
                str.endsWith("enum.java") ||
                str.endsWith("test.js") ||
                str.endsWith("tests.js") ||
                str.startsWith(".") ||
                path.contains(JPMS);
    }

}
