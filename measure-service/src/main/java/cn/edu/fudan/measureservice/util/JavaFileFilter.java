package cn.edu.fudan.measureservice.util;


import java.io.File;

/**
 * @ClassName: JavaFileFilter
 * @Description: 对java文件的过滤
 * @Author wjzho
 * @Date 2021/3/11
 */

public class JavaFileFilter extends FileFilter{
    /**
     * JPMS 模块
     */
    private static final String JPMS = "module-info.java";

    /**
     * true: 过滤
     * false： 不过滤
     */
    @Override
    public  Boolean fileFilter(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        return  !path.toLowerCase().endsWith(".java") ||
                path.toLowerCase().contains("/test/") ||
                path.toLowerCase().contains("/.mvn/") ||
                path.toLowerCase().endsWith("test.java") ||
                path.toLowerCase().endsWith("tests.java") ||
                path.toLowerCase().startsWith("test") ||
                path.toLowerCase().endsWith("enum.java") ||
                path.contains(JPMS);
    }



}
