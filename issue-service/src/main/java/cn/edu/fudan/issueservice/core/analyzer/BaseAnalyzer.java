package cn.edu.fudan.issueservice.core.analyzer;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.util.AstParserUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * description: 工具具体的执行流程
 *
 * @author fancying
 * create: 2020-05-20 15:53
 **/
@Getter
@Setter
public abstract class BaseAnalyzer {

    protected List<RawIssue> resultRawIssues = new ArrayList<>();

    protected Map<String, Set<String>> methodsAndFieldsInFile = new HashMap<>(32);

    /**
     * 调用工具扫描
     *
     * @param repoUuid repoUuid
     * @param repoPath repoPath
     * @param commit   commit
     * @return 调用工具是否成功
     */
    public abstract boolean invoke(String repoUuid, String repoPath, String commit);


    /**
     * 调用工具进行解析,如sonarqube结果解析成rawIssue
     *
     * @param repoPath repoPath
     * @param repoUuid repoUuid
     * @param commit   commitId
     * @return 解析是否成功
     */
    public abstract boolean analyze(String repoPath, String repoUuid, String commit);

    /**
     * 返回工具名
     *
     * @return 工具名
     */
    public abstract String getToolName();

    /**
     * 返回该缺陷的优先级
     *
     * @param rawIssue rawIssue
     * @return 缺陷优先级
     */
    public abstract Integer getPriorityByRawIssue(RawIssue rawIssue);

    /**
     * 根据文件名获取文件中的方法和变量
     *
     * @param fileName fileName
     * @return methods and fields
     */
    public Set<String> getMethodsAndFieldsInFile(String fileName) {
        if (this instanceof SonarQubeBaseAnalyzer) {
            return AstParserUtil.getAllMethodAndFieldName(fileName);
        } else {
            return methodsAndFieldsInFile.getOrDefault(fileName, new HashSet<>());
        }
    }

    public void emptyAnalyzeRawIssues() {
        resultRawIssues.clear();
    }
}
