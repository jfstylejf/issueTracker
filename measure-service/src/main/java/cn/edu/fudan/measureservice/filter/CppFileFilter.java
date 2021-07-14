package cn.edu.fudan.measureservice.filter;

/**
 * @ClassName: CppFileFilter
 * @Description: c++文件筛选
 * @Author wjzho
 * @Date 2021/7/6
 */

public class CppFileFilter extends FileFilter{


    @Override
    public Boolean fileFilter(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return true;
        }
        // todo 完善筛选逻辑
        return !filePath.toLowerCase().endsWith(".cpp");
    }
}
