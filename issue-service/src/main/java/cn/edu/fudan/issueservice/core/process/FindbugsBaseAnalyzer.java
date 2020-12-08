package cn.edu.fudan.issueservice.core.process;

import cn.edu.fudan.issueservice.domain.dbo.Location;
import cn.edu.fudan.issueservice.domain.dbo.RawIssue;
import cn.edu.fudan.issueservice.domain.RawIssueDetail;
import cn.edu.fudan.issueservice.domain.enums.RawIssueStatus;
import cn.edu.fudan.issueservice.domain.dto.FileInfo;
import cn.edu.fudan.issueservice.domain.enums.ToolEnum;
import cn.edu.fudan.issueservice.util.ASTUtil;
import com.alibaba.fastjson.JSONObject;
import edu.umd.cs.findbugs.*;
import lombok.extern.slf4j.Slf4j;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * description:
 *
 * @author fancying
 * create: 2020-05-20 15:55
 **/
@Slf4j
@Deprecated
public class FindbugsBaseAnalyzer extends BaseAnalyzer {

    private String resultFileHome;

    private String resultFileXml;

    private String resultSummary;

    public String getResultFileHome() {
        return resultFileHome;
    }

    public void setResultFileHome(String resultFileHome) {
        this.resultFileHome = resultFileHome;
    }

    @Override
    public boolean invoke(String repoId, String repoPath, String commit) {

        try {
            // TODO 注意 后续规定 所有的目录或者地址 均以 \\ 或 / 结尾
            // TODO 注意 后续规定 所有的目录或者地址 均以 \\ 或 / 结尾
//            resultFileHome = "E:\\Lab\\scanProject\\result\\";
            resultFileXml = resultFileHome + repoId  + ".xml";
            String[] args = {"-xml", "-output", resultFileXml,repoPath };
            FindBugs2.main(args);

            return true;
        }catch (Exception e) {
            log.error(e.getMessage());
        }



        return false;
    }

    @Override
    public boolean analyze(String repoPath, String repoId, String commitId) {
        SAXReader reader = new SAXReader ();
        try {
            Document doc = reader.read(new File (resultFileXml));
            Element root = doc.getRootElement();
            Element summary = root.element("FindBugsSummary");
            resultSummary = getJsonString(summary);

            Iterator<Element> iterator = root.elementIterator("BugInstance");

            while (iterator.hasNext()) {
                Element bugInstance = iterator.next();
                List<Location> locations = new ArrayList<>();
                String rawIssueUUID = UUID.randomUUID().toString();
                //解析当前bugInstance中的location
                FileInfo fileInfo = analyzeLocations(rawIssueUUID, repoPath, bugInstance, locations);
                if (fileInfo != null&&!locations.isEmpty()) {
                    //只有location解析成功并且rawIssue有location才会插入当前rawIssue
                    RawIssue rawIssue = new RawIssue();
                    rawIssue.setUuid (rawIssueUUID);
                    rawIssue.setType (bugInstance.attributeValue("type"));
                    rawIssue.setTool (ToolEnum.FINDBUGS.getType ());
                    rawIssue.setDetail (getJsonString(bugInstance));
                    rawIssue.setFile_name (fileInfo.getFileName());
                    rawIssue.setCommit_id (commitId);
                    rawIssue.setRepo_id (repoId);
                    rawIssue.setCode_lines (fileInfo.getCode_lines());
                    rawIssue.setLocations (locations);
                    rawIssue.setStatus (RawIssueStatus.DEFAULT.getType ());
                    resultRawIssues.add(rawIssue);
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getToolName() {
        return ToolEnum.FINDBUGS.getType ();
    }




    private FileInfo analyzeLocations(String rawIssueUUID, String repoPath, Element bugInstance, List<Location> locations) {
        FileInfo fileInfo = new FileInfo();
        Element sourceLineInClass = bugInstance.element("Class").element("SourceLine");
        String className = sourceLineInClass.attributeValue("classname");

        if(javaFilenameFilter(className)){
            return null;
        }


        String fileName = sourceLineInClass.attributeValue("sourcefile");
        String sourcePath=sourceLineInClass.attributeValue("sourcepath");
        String filePath=null;

        //考虑到一个项目下可能有多个微服务，多个微服务中可能又存在多个文件名相同的文件。
        String candidateFilePaths = getFileLocation(repoPath, fileName);
        if (candidateFilePaths == null) {
            log.error(sourcePath + "find 命令 找不到源文件！");
            return null;
        }else{
            String[] candidates=candidateFilePaths.split(":");
            Pattern pattern= Pattern.compile("[/A-Za-z0-9_\\-.*]*"+sourcePath);
            for(String candidate:candidates){
                Matcher matcher = pattern.matcher(candidate);
                if(matcher.matches()){
                    filePath=candidate;
                    break;
                }
            }
        }
        if(filePath==null){
            log.error(sourcePath + " 找不到匹配源文件！");
            return null;
        }

        //寻找匹配的Method节点
        Element method = null;
        Iterator<Element> methodIterator = bugInstance.elementIterator("Method");
        if(methodIterator != null){
            while (methodIterator.hasNext()) {
                Element methodInstance = methodIterator.next();
                String methodClassName = methodInstance.attributeValue("classname");
                if(methodClassName != null && methodClassName.equals (className)){
                    method = methodInstance;
                }
            }
        }


        if (method == null) {
            // todo 如果没有匹配的method节点,则再判断是不是字段的bug信息

            return null;
        }

        Iterator<Element> iterator = bugInstance.elementIterator("SourceLine");

        String methodName = method.attributeValue("name");
        Element sourceLineInMethod = method.element("SourceLine");
        int start = Integer.parseInt(sourceLineInMethod.attributeValue("start"));
        int end = Integer.parseInt(sourceLineInMethod.attributeValue("end"));
        String bugLines = null;
        String code;
        if (iterator != null) {
            Set<String> container = new HashSet<>();
            StringBuilder bugLineBuilder = new StringBuilder();
            while (iterator.hasNext()) {
                Element SourceLine = iterator.next();
                String startBugLine = SourceLine.attributeValue("start");
                if (!container.contains(startBugLine)) {
                    container.add(startBugLine);
                    bugLineBuilder.append(",");
                    bugLineBuilder.append(startBugLine);
                }
            }
            if (bugLineBuilder.length() > 0) {
                bugLines = bugLineBuilder.deleteCharAt(0).toString();
            }
            if (container.size() > 0) {
                code = ASTUtil.getCodeAtSpecificLines(container, repoPath+ "/" + filePath);
            } else {
                code = ASTUtil.getCode(start, end,  repoPath+ "/" + filePath);
            }
        } else {
            code = ASTUtil.getCode(start, end, repoPath+ "/" + filePath);
        }

        //code = code.replace(" ","");
        code = code.replaceAll("\r|\n","");
        fileInfo.setCode_lines(ASTUtil.getCodeLines(repoPath+ "/" + filePath));
        fileInfo.setFileName(filePath);
        Location location = new Location();
        location.setUuid (UUID.randomUUID().toString());
        location.setStart_line (start);
        location.setEnd_line (end);
        location.setBug_lines (bugLines);
        location.setFile_path (filePath);
        location.setClass_name (className);
        location.setMethod_name (methodName);
        location.setRawIssue_id (rawIssueUUID);
        location.setCode (code);

        locations.add(location);
        return fileInfo;
    }



    @SuppressWarnings("unchecked")
    private String getJsonString(Element element) {
        List<Attribute> attributes = (List<Attribute>) element.attributes();
        StringBuilder json = new StringBuilder();
        json.append("{");
        for (int i = 0; i < attributes.size(); i++) {
            json.append("\"");
            json.append(attributes.get(i).getName());
            json.append("\":\"");
            json.append(attributes.get(i).getValue());
            json.append("\"");
            if (i != attributes.size() - 1) {
                json.append(",");
            }
        }
        json.append("}");
        return json.toString();
    }




    public  String getFileLocation(String repoPath, String fileName) {
        try {
            Runtime rt = Runtime.getRuntime();
            String command = "find "+ repoPath + " -name " + fileName;
            //String command =  binHome + "findOneFile.sh " + repoHome + repoPath + " "+ fileName;
            Process process = rt.exec(command);
            process.waitFor();
            BufferedReader bReader = new BufferedReader(new InputStreamReader (process.getInputStream(), StandardCharsets.UTF_8));
            StringBuffer sBuffer = new StringBuffer();
            String line ;
            while ((line = bReader.readLine())!= null) {
                line = line.replace(repoPath+"/", "");
                sBuffer.append(line).append(":");
            }
            return  sBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Integer getPriorityByRawIssue(RawIssue rawIssue){
        // 映射 （1-4）1 、（5-9）2、（10-14）3 、（15 -20） 4
        Integer priority =  Integer.parseInt(JSONObject.parseObject(rawIssue.getDetail(), RawIssueDetail.class).getRank())/5 + 1 ;
        priority = priority == 5 ? 4 : priority ;
        return priority;
    }

    public static void main(String[] args){
        try {
            String resultFileXml = "E:\\school\\findbugsResult\\" + "111.xml";
            String[] findbugsArgs = {"-xml", "-output", resultFileXml,"E:\\school\\laboratory\\IssueTracker-main\\IssueTracker-Master" };
            FindBugs2.main(findbugsArgs);


            FindBugs2 findBugs = new FindBugs2();
            TextUICommandLine commandLine = new TextUICommandLine();
            FindBugs.processCommandLine(commandLine, findbugsArgs, findBugs);
            boolean justPrintConfiguration = commandLine.justPrintConfiguration();
            if (!justPrintConfiguration && !commandLine.justPrintVersion()) {
                FindBugs.runMain(findBugs, commandLine);
            } else {
                Version.printVersion(justPrintConfiguration);
            }

            BugReporter bugReporter = findBugs.getBugReporter ();
            BugCollection bugInstances =bugReporter.getBugCollection ();
            Iterator<BugInstance> bugInstanceIterator = bugInstances.iterator ();
            while (bugInstanceIterator.hasNext ()){
                BugInstance bugInstance = bugInstanceIterator.next ();
                bugInstance.getAnnotations ();
            }
        }catch (Exception e){
            e.printStackTrace ();
        }

    }

    /**
     * JPMS 模块
     */
    private static final String JPMS = "module-info";
    /**
     * true: 过滤
     * false： 不过滤
     */
    public  static boolean javaFilenameFilter(String path) {
        String[] strs = path.split("/");
        String str = strs[strs.length-1];
        return  str.toLowerCase().endsWith("test") ||
                str.toLowerCase().endsWith("tests") ||
                str.toLowerCase().startsWith("test") ||
                str.toLowerCase().endsWith("enum") ||
                path.contains(JPMS);
    }
}