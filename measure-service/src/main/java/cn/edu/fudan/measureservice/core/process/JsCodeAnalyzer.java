package cn.edu.fudan.measureservice.core.process;

import cn.edu.fudan.measureservice.domain.*;
import cn.edu.fudan.measureservice.domain.dto.FileInfo;
import cn.edu.fudan.measureservice.domain.dto.MethodInfo;
import cn.edu.fudan.measureservice.util.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;


import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author wjzho
 */
@Slf4j
public class JsCodeAnalyzer extends BaseAnalyzer{

    private static final String jsResultFileHome = "/home/fdse/codeWisdom/service/measure/log/JsResultLog";
    private static final String jsCcnLog = "jsCcn.log";
    private static final String jsLineLog = "jsLine.log";
    private static final String jsComplexity = "excuteJsComplexity.sh";
    private static final String jsLine = "excuteJsLine.sh";



    @Override
    public boolean invoke() {
        if(getJsCodeComplexity(repoPath) && getJsCodeLine(repoPath)) {
            log.info("invoke tool success!\n");
            return true;
        }
        return false;
    }

    @Override
    public boolean analyze() {
        try {
            JSONArray jsCcnResult = readJsParseFile(FileUtil.pathJoint(jsResultFileHome,jsCcnLog));
            JSONArray jsLineResult = readJsParseFile(FileUtil.pathJoint(jsResultFileHome,jsLineLog));
            if (jsCcnResult == null || jsLineResult == null) {
                return false;
            }
            analyzedResult = dataTypeProcess(getJsFileInfo(jsCcnResult,jsLineResult));
            return true;
        }catch (Exception e) {
            log.error("readJsResult failed!\n");
            return false;
        }
    }


    public Boolean getJsCodeLine(String path){
        try {
            Runtime rt = Runtime.getRuntime();
            String command = binHome + jsLine + " " + path;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(200L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("run jsComplexity script timeout ! (100s),file: {}", path);
                return false;
            }
            if(process.exitValue() != 0){

            }
        }catch (Exception e) {
            log.error("invoke jsLine failed!");
        }
        return true;
    }

    public Boolean getJsCodeComplexity(String path) {
        try {
            Runtime rt = Runtime.getRuntime();
            String command = binHome + jsComplexity + " " + path;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(200L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("run jsComplexity script timeout ! (100s),file: {}", path);
                return false;
            }
            if(process.exitValue() != 0){

            }
        }catch (Exception e) {
            log.error("invoke jsComplexity failed!");
        }
        return true;
    }

    //todo 读写文件异常处理细化判断
    private JSONArray readJsParseFile(String filePath) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))){
            String line;
            while((line = bufferedReader.readLine())!=null) {
                stringBuilder.append(line);
            }
            return JSONArray.parseArray(stringBuilder.toString());
        } catch (IOException e) {
            log.error("read jsComplexityResultFile : {} failed ", filePath);
        }
        return null;
    }

    private List<FileInfo> getJsFileInfo(JSONArray jsCcnResult,JSONArray jsLineResult) {
        List<FileInfo> fileInfos = new ArrayList<>();
        Map<String,List<MethodInfo>> map = new HashMap<>();
        for (int i = 0; i < jsCcnResult.size(); i++) {
            JSONObject method = (JSONObject) jsCcnResult.get(i);
            String fileName = FileUtil.systemAvailablePath(method.getString("fileName"));
            if (!map.containsKey(fileName)) {
                map.put(fileName,new ArrayList<>());
            }
            map.get(fileName).add(MethodInfo.builder()
                    .methodName(method.getString("funcName"))
                    .absoluteFilePath(fileName)
                    .methodCcn(method.getIntValue("complexity"))
                    .position(method.getString("position"))
                    .build()
            );
        }
        for (int i = 0; i < jsLineResult.size(); i++) {
            JSONObject line = (JSONObject) jsLineResult.get(i);
            String fileName = FileUtil.systemAvailablePath(line.getString("file"));
            if(!map.containsKey(fileName)) {
                //过滤掉一些是数据的js文件
                continue;
            }
            FileInfo fileInfo = FileInfo.builder()
                                    .absolutePath(fileName)
                                    .relativePath(FileUtil.getRelativePath(repoPath,fileName))
                                    .codeLines(line.getIntValue("codeLine"))
                                    .blankLines(line.getIntValue("blankLine"))
                                    .totalLines(line.getIntValue("allLine"))
                                    .methodInfoList(map.get(fileName))
                                    .build();
            fileInfo.calFileCcn();
            fileInfos.add(fileInfo);

        }
        return fileInfos;
    }

    private Measure dataTypeProcess(List<FileInfo> fileInfos) {
        List<OObject> objects = new ArrayList<>();
        List<Function> functions = new ArrayList<>();

        int totalFunctions = 0;
        int totalCcn = 0;
        for (FileInfo fileInfo : fileInfos) {
            totalFunctions += fileInfo.getMethodInfoList().size();
            totalCcn += fileInfo.getFileCcn();
            for (MethodInfo methodInfo : fileInfo.getMethodInfoList()) {
                functions.add(Function.builder()
                        .name(methodInfo.getMethodName())
                        .ccn(methodInfo.getMethodCcn())
                        .build());
            }
            objects.add(OObject.builder()
                    .ccn(fileInfo.getFileCcn())
                    .functions(fileInfo.getMethodInfoList().size())
                    .path(fileInfo.getRelativePath())
                    // fixme totalLines 是代码行还是包括空白行
                    .totalLines(fileInfo.getCodeLines())
                    .build());
        }
        Total total = Total.builder()
                .files(fileInfos.size())
                .functions(totalFunctions).build();
        return Measure.builder()
                .total(total)
                .functions(Functions.builder().functions(functions).functionAverage(FunctionAverage.builder().ccn(totalCcn*1.0/totalFunctions).build()).build())
                .objects(Objects.builder().objects(objects).build())
                .build();
    }


    public  FileInfo getFileInfo(String filePath) {
        if(!getJsCodeComplexity(filePath)) {
            return null;
        }
        try {
            JSONArray jsComplexityResult = readJsParseFile(FileUtil.pathJoint(jsResultFileHome , jsCcnLog));
            if (jsComplexityResult == null) {
                return null;
            }
            List<MethodInfo> methodInfos = new ArrayList<>();
            for (int i = 0; i < jsComplexityResult.size(); i++) {
                JSONObject method = (JSONObject) jsComplexityResult.get(i);
                String fileName = FileUtil.systemAvailablePath(method.getString("fileName"));
                methodInfos.add(MethodInfo.builder()
                        .methodName(method.getString("funcName"))
                        .absoluteFilePath(fileName)
                        .methodCcn(method.getIntValue("complexity"))
                        .position(method.getString("position"))
                        .build()
                );
            }
            FileInfo fileInfo = FileInfo.builder()
                    .absolutePath(filePath)
                    .relativePath(FileUtil.getRelativePath(repoPath,filePath))
                    .methodInfoList(methodInfos)
                    .build();
            fileInfo.calFileCcn();
            return fileInfo;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] args) {

    }


}
