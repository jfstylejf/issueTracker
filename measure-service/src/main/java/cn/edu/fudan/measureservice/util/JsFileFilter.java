package cn.edu.fudan.measureservice.util;


import java.io.File;

/**
 * @ClassName: JsFileFilter
 * @Description:
 * @Author wjzho
 * @Date 2021/3/11
 */

public class JsFileFilter extends FileFilter{


    @Override
    public Boolean fileFilter(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return true;
        }
        filePath = FileUtil.systemAvailablePath(filePath);
        String[] strs = filePath.split(File.pathSeparator);
        String str = strs[strs.length-1];
        return  !str.toLowerCase().endsWith(".js") ||
                str.startsWith(".") ||
                filePath.contains(".test.js") ||
                filePath.contains(".tests.js") ||
                filePath.contains("/test/") ||
                filePath.contains("/tests/");
    }

}
