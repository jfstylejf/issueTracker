package cn.edu.fudan.issueservice.core.analyzer;

import cn.edu.fudan.common.jgit.JGitHelper;
import cn.edu.fudan.issueservice.dao.CommitDao;
import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.dto.XmlError;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.util.AstUtil;
import cn.edu.fudan.issueservice.util.FileUtil;
import cn.edu.fudan.issueservice.util.ShUtil;
import cn.edu.fudan.issueservice.util.XmlUtil;
import cn.edu.fudan.issueservice.domain.enums.IssuePriorityEnums.CppIssuePriorityEnum;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author beethoven
 * @date 2021-07-01 11:11:46
 */
@Slf4j
@Component
@Scope("prototype")
public class TscanCodeAnalyzer extends BaseAnalyzer {

    private CommitDao commitDao;

    @Value("${binHome}")
    private String binHome;

    @Value("${TscanCodeLogHome}")
    private String logHome;

    @Override
    public boolean invoke(String repoUuid, String repoPath, String commit) {
        return ShUtil.executeCommand(binHome + "executeTscanCode.sh " + repoPath + " " + repoUuid + " " + commit, 300);
    }

    @Override
    public boolean analyze(String repoPath, String repoUuid, String commit) {
        //read log  -> return String
        String fileName = logHome + repoUuid + "_" + commit;
        try {
            List<XmlError> errors = XmlUtil.getError(fileName);
            return xmlErrors2RawIssues(errors, commit, repoUuid, repoPath);
        } catch (IOException | SAXException | JDOMException e) {
            log.error("parse xml file failed, fileName is: {}", fileName);
            log.error("exception msg is: {}", e.getMessage());
            return false;
        }
    }

    private boolean xmlErrors2RawIssues(List<XmlError> errors, String commit, String repoUuid, String repoPath) {

        List<RawIssue> rawIssues = new ArrayList<>();

        try {

            JGitHelper jGitInvoker = new JGitHelper(repoPath);
            String developerUniqueName = jGitInvoker.getAuthorName(commit);
            Map<String, Object> commitViewInfo = commitDao.getCommitViewInfoByCommitId(repoUuid, commit);
            if (commitViewInfo != null) {
                developerUniqueName = commitViewInfo.get("developer_unique_name") == null ? developerUniqueName : (String) commitViewInfo.get("developer_unique_name");
            }
            jGitInvoker.close();

            for (XmlError error : errors) {
                String uuid = UUID.randomUUID().toString();
                RawIssue rawIssue = RawIssue.builder()
                        .uuid(uuid)
                        .type(error.getId() + "-" + error.getSubId())
                        .tool(getToolName())
                        .detail(error.getMsg())
                        .fileName(error.getFile())
                        .scanId(getToolName())
                        .commitId(commit)
                        .repoId(repoUuid)
                        .codeLines(error.getLine())
                        .locations(parseLocations(error.getFile(), error.getFuncInfo(), error.getLine(), uuid))
                        .developerName(developerUniqueName)
                        .priority(CppIssuePriorityEnum.getRankByPriority(error.getSeverity()))
                        .build();
                rawIssues.add(rawIssue);
            }
            resultRawIssues.addAll(rawIssues);
            return true;
        } catch (Exception e) {
            log.error("raw issue parse from xml error failed, msg: {}", e.getMessage());
        }

        return false;
    }

    private List<Location> parseLocations(String file, String funcInfo, int line, String uuid) {
        String code = AstUtil.getCode(line, line, file);
        Location location = Location.builder()
                .uuid(UUID.randomUUID().toString())
                .startLine(line)
                .endLine(line)
                .bugLines(line + "-" + line)
                .filePath(FileUtil.handleFileNameToRelativePath(file))
                .methodName(funcInfo)
                .rawIssueId(uuid)
                .code(code)
                .build();

        return new ArrayList<>() {{
            add(location);
        }};
    }

    @Override
    public String getToolName() {
        return ToolEnum.TSCANCODE.getType();
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue) {
        return null;
    }

    @Autowired
    public void setCommitDao(CommitDao commitDao) {
        this.commitDao = commitDao;
    }
}
