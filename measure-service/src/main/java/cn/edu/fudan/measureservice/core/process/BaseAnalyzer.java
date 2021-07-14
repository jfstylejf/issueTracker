package cn.edu.fudan.measureservice.core.process;

import cn.edu.fudan.measureservice.domain.Measure;
import cn.edu.fudan.measureservice.domain.dto.FileInfo;
import cn.edu.fudan.measureservice.domain.dto.ScanCommitInfoDto;
import lombok.Data;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

/**
 * @author wjzho
 */
@Data
public abstract class BaseAnalyzer {

    protected String binHome;

    protected String libHome;

    protected String repoPath;

    protected ScanCommitInfoDto scanCommitInfoDto;

    protected Measure analyzedResult;

    /**
     * 调用扫描工具
     * @return 返回工具调用是否成功
     */
    public abstract boolean invoke();

    /**
     * 解析扫描结果
     * @return boolean 返回解析是否成功
     */
    public abstract boolean analyze();




    public Measure getAnalyzedResult() {
        return this.analyzedResult;
    }

}
