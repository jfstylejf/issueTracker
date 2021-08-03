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
import cn.edu.fudan.measureservice.core.process.CppCodeAnalyzer;
import cn.edu.fudan.measureservice.domain.dto.FileInfo;
import cn.edu.fudan.measureservice.domain.dto.MethodInfo;
import cn.edu.fudan.measureservice.domain.dto.ParameterPair;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.*;

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

    private Map<String, Map<String, int[]>> method2Line = new HashMap<>();

    @Override
    public boolean invoke(String repoUuid, String repoPath, String commit) {
        return ShUtil.executeCommand(binHome + "executeTscanCode.sh " + repoPath + " " + repoUuid + " " + commit, 300);
    }

    @Override
    public boolean analyze(String repoPath, String repoUuid, String commit) {

        String errFile = logHome + "err-" + repoUuid + "_" + commit + ".xml";
        String infoFile = logHome + "info-" + repoUuid + "_" + commit + ".txt";

        try {
            List<XmlError> errors = XmlUtil.getError(errFile);
            ShUtil.executeCommand(binHome + "deleteScanResult.sh " + errFile + " " + infoFile, 20);
            return xmlErrors2RawIssues(errors, commit, repoUuid, repoPath);
        } catch (IOException | SAXException | JDOMException e) {
            log.error("parse xml file failed, fileName is: {}", errFile);
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

            Set<String> files = new HashSet<>();
            errors.forEach(error -> files.add(error.getFile()));
            parseMethodAndField(files);

            for (XmlError error : errors) {
                String uuid = UUID.randomUUID().toString();
                String file = FileUtil.handleFileNameToRelativePath(error.getFile());

                RawIssue rawIssue = new RawIssue();
                rawIssue.setUuid(uuid);
                rawIssue.setType(error.getId() + "-" + error.getSubId());
                rawIssue.setTool(getToolName());
                rawIssue.setDetail(error.getMsg());
                rawIssue.setFileName(file);
                rawIssue.setScanId(getToolName());
                rawIssue.setCommitId(commit);
                rawIssue.setRepoId(repoUuid);
                rawIssue.setCodeLines(error.getLine());
                rawIssue.setLocations(parseLocations(error.getFile(), error.getLine(), uuid));
                rawIssue.setDeveloperName(developerUniqueName);
                rawIssue.setPriority(CppIssuePriorityEnum.getRankByPriority(error.getSeverity()));

                rawIssues.add(rawIssue);
            }

            resultRawIssues.addAll(rawIssues);

            return true;
        } catch (Exception e) {
            log.error("raw issue parse from xml error failed, msg: {}", e.getMessage());
        }

        return false;
    }

    private void parseMethodAndField(Set<String> files) throws IOException {
        for (String file : files) {
            Set<String> methodAndField = new HashSet<>();
            FileInfo fileInfo = CppCodeAnalyzer.parseFile(file);

            for (MethodInfo methodInfo : fileInfo.getMethodInfoList()) {
                String methodSignature = methodInfo.getMethodSignature();
                methodAndField.add(methodSignature);
                Map<String, int[]> map = method2Line.getOrDefault(file, new HashMap<>(16));
                map.put(methodSignature, new int[]{methodInfo.getStartPosition(), methodInfo.getEndPosition()});
                method2Line.put(file, map);
            }

            for (ParameterPair parameterPair : fileInfo.getMemberList()) {
                String fieldSignature = parameterPair.getSpecifier() + " " + parameterPair.getParameterName();
                methodAndField.add(fieldSignature);
                Map<String, int[]> map = method2Line.getOrDefault(file, new HashMap<>(16));
                map.put(fieldSignature, new int[]{parameterPair.getStartPosition(), parameterPair.getEndPosition()});
                method2Line.put(file, map);
            }

            methodsAndFieldsInFile.put(file, methodAndField);
        }
    }

    private List<Location> parseLocations(String file, int line, String uuid) {

        String code = AstUtil.getCode(line, line, file);
        Map<String, int[]> methodInfoMap = method2Line.get(file);
        String methodName = null;

        for (Map.Entry<String, int[]> entry : methodInfoMap.entrySet()) {
            if (entry.getValue()[0] <= line && entry.getValue()[1] >= line) {
                methodName = entry.getKey();
                break;
            }
        }

        Location location = Location.builder()
                .uuid(UUID.randomUUID().toString())
                .startLine(line)
                .endLine(line)
                .bugLines(line + "-" + line)
                .filePath(FileUtil.handleFileNameToRelativePath(file))
                .methodName(methodName)
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
