package cn.edu.fudan.measureservice.core.process;

import cn.edu.fudan.measureservice.domain.*;
import cn.edu.fudan.measureservice.domain.Objects;
import cn.edu.fudan.measureservice.domain.dto.FileInfo;
import cn.edu.fudan.measureservice.domain.dto.MethodInfo;
import cn.edu.fudan.measureservice.domain.dto.TextInfo;
import cn.edu.fudan.measureservice.util.FileFilter;
import cn.edu.fudan.measureservice.util.FileUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wjzho
 */
@Slf4j
@Component
public class JsCodeAnalyzer extends BaseAnalyzer{

    private static final String jsResultFileHome = "/home/appuser/codeWisdom/service/measure/log/JsResultLog";
    private static final String jsCcnLog = "jsCcn.log";
    private static final String jsLineLog = "jsLine.log";
    public static final String jsScanLog = "jsScan.log";
    private static final String jsComplexity = "excuteJsComplexity.sh";
    private static final String jsLine = "excuteJsLine.sh";
    public static final String jsScan = "excuteJsScan.sh";

    @SneakyThrows
    @Override
    public boolean invoke() {
        /*if(!getJsScanResult()) {
            log.error("scan {} failed\n",repoPath);
            return false;
        }
        JSONArray jsScanResult = readJsParseFile(FileUtil.pathJoint(jsResultFileHome,jsScanLog));
        if(jsScanResult==null) {
            log.error("read scan result failed\n");
            return false;
        }
        List<String> fileList = JSONObject.parseArray(jsScanResult.toJSONString(),String.class);
        if (jsScanResult.size()==0) {
            log.warn("check the repo : {} , no js file here\n",repoPath);
            return false;
        }*/
        if (!getJsCodeComplexity(repoPath) || !getJsCodeLine(repoPath)) {
            log.error("read data failed\n");
            return false;
        }
        return true;
    }


    @Override
    public  boolean analyze() {
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


    public Boolean getJsScanResult() {
        try {
            return executeCommand(repoPath,jsScan);
        }catch (Exception e) {
            log.error("invoke jsScan failed!");
        }
        return true;
    }

    public Boolean getJsCodeLine(String filePath){
        try {
            return executeCommand(filePath,jsLine);
        }catch (Exception e) {
            log.error("invoke jsLine failed!");
        }
        return true;
    }


    public Boolean getJsCodeComplexity(String filePath) {
        try {
            return executeCommand(filePath,jsComplexity);
        }catch (Exception e) {
            log.error("invoke jsComplexity failed!");
        }
        return true;
    }


    @SneakyThrows
    private  boolean executeCommand(String path, String type){
        Runtime rt = Runtime.getRuntime();
        String command = binHome + type + " " + path;
        log.info("command -> {}", command);
        /*
            note :
             Process.waitfor 挂起 Java 线程， 等待子进程执行
             然而本地缓冲区大小有限， 如果不处理写入 buffer 的 标准输入流和标准输出流就会造成子进程阻塞，发生死锁
         */
        Process process = rt.exec(command);
        try {
            //获取进程的标准输入流
            final InputStream is1 = process.getInputStream();
            //获取进城的错误流
            final InputStream is2 = process.getErrorStream();
            //启动两个线程，一个线程负责读标准输出流，另一个负责读标准错误流
            // fixme 使用线程池
            new Thread() {
                @Override
                public void run() {
                    BufferedReader br1 = new BufferedReader(new InputStreamReader(is1));
                    try {
                        String line1;
                        while ((line1 = br1.readLine()) != null) {
                            if (line1 != null){}
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally{
                        try {
                            is1.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
            new Thread() {
                @Override
                public void  run() {
                    BufferedReader br2 = new  BufferedReader(new  InputStreamReader(is2));
                    try {
                        String line2 = null ;
                        while ((line2 = br2.readLine()) !=  null ) {
                            if (line2 != null){}
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    finally{
                        try {
                            is2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
            boolean timeout = process.waitFor(200L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("run {} script timeout ! (100s),file: {}",type, path);
                return false;
            }
            if (process.exitValue() != 0) {

            }
            return true;

        }catch (Exception e) {
            e.printStackTrace();
            try{
                process.getErrorStream().close();
                process.getInputStream().close();
                process.getOutputStream().close();
            }
            catch(Exception ignored){
                e.getMessage();
            }
            return false;
        }

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
            log.error("read jsResultFile : {} failed ", filePath);
        }
        return null;
    }

    private List<FileInfo> getJsFileInfo(JSONArray jsCcnResult, JSONArray jsLineResult) {
        List<FileInfo> fileInfos = new ArrayList<>();
        Map<String,List<MethodInfo>> map = new HashMap<>();
        for (int i = 0; i < jsCcnResult.size(); i++) {
            JSONObject method = (JSONObject) jsCcnResult.get(i);
            String fileName = FileUtil.systemAvailablePath(method.getString("fileName"));
            String relativeName = FileUtil.getRelativePath(repoPath,fileName);
            if (!map.containsKey(relativeName)) {
                map.put(relativeName,new ArrayList<>());
            }
            map.get(relativeName).add(MethodInfo.builder()
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
            String relativeName = FileUtil.getRelativePath(repoPath,fileName);
            String rawFileText = line.getString("fileContent");
            String[] fileTexts = rawFileText.split("\n");
            TextInfo textInfo = FileFilter.textFilter(fileTexts);
            FileInfo fileInfo = FileInfo.builder()
                    .absolutePath(fileName)
                    .relativePath(relativeName)
                    .codeLines(textInfo.getCodeLines())
                    .blankLines(textInfo.getBlankLines())
                    .totalLines(textInfo.getTotalLines())
                    .build();
            if(!map.containsKey(relativeName)) {
                fileInfo.setMethodInfoList(new ArrayList<>());
                fileInfo.setFileCcn(0);
            }else {
                fileInfo.setMethodInfoList(map.get(relativeName));
                fileInfo.calFileCcn();
            }
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
                     // 这边拿的总行数，最后入库数据需要剪掉空白+注释
                    .totalLines(fileInfo.getCodeLines())
                    .build());
        }
        Total total = Total.builder()
                .files(fileInfos.size())
                .functions(totalFunctions).build();
        double functionAverageCcn = totalFunctions == 0 ? 0 : totalCcn*1.0/totalFunctions;
        FunctionAverage functionAverage = FunctionAverage.builder()
                .ccn(functionAverageCcn).build();

        return Measure.builder()
                .total(total)
                .functions(Functions.builder().functions(functions).functionAverage(functionAverage).build())
                .objects(Objects.builder().objects(objects).build())
                .build();
    }


    public  FileInfo getPreFileInfo(String filePath) {
        String absolutePath = FileUtil.getAbsolutePath(repoPath,filePath);
        if(!getJsCodeComplexity(absolutePath)) {
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
                    .absolutePath(absolutePath)
                    .relativePath(filePath)
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
