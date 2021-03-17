package cn.edu.fudan.measureservice.core.process;

import cn.edu.fudan.measureservice.analyzer.JavaNcss;

/**
 * @ClassName: JavaCodeAnalyzer
 * @Description:
 * @Author wjzho
 * @Date 2021/3/11
 */

public class JavaCodeAnalyzer extends BaseAnalyzer{
    @Override
    public boolean invoke() {
        return true;
    }

    @Override
    public boolean analyze() {
        try {
            analyzedResult = JavaNcss.analyse(repoPath);
            return true;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
