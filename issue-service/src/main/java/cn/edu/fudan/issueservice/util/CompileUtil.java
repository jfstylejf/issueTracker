package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.enums.CompileTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;
import org.gradle.tooling.ProjectConnection;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * description: 判断指定目录下的代码是否可以编译
 *
 * @author fancying
 * create: 2020-03-10 21:30
 **/
@Component
@Slf4j
public class CompileUtil {


    private static String mvnHome;

    private static String gradleBin;

    @Value("${mvnHome}")
    public void setMvnHome(String mvnHome) {
        CompileUtil.mvnHome = mvnHome;
    }

    @Value("${gradleBin}")
    public void setGradleBin(String gradleBin) {
        CompileUtil.gradleBin = gradleBin;
    }


    //"-T 2C" "-Dmaven.compile.fork=true"

    private static final String[] VAR = {"compile", "-Dmaven.test.skip=true", "-ff",
            "-B", "-q", "-l /dev/null", "-Dmaven.compile.fork=true","-T 2C"};
    private static List<String> var = java.util.Arrays.asList(VAR);

    public static boolean isCompilable(String repoPath) {
        final int compileSuccessCode = 0;

        List<String> compilePathList = PomAnalysisUtil.getMainPom(getCompilePath(repoPath));
        if (compilePathList == null || compilePathList.size() == 0) {
            log.error("repo path is {}, compilePathList is null", repoPath);
            //return false;
            /// fixme 特殊处理  后面在看
            return  gradleCompile(repoPath);
        }

        for (String compilePath : compilePathList) {
            CompileTool compileTool = getCompileToolByPath(compilePath);
            log.info("Compile Path is {}", compilePath);
            log.info("Compile Tool is {}", compileTool.name());
            // TODO 应该根据接口来动态的根据 compileTool 调用相应的实现方法
            if (compileTool == CompileTool.maven) {
                InvocationRequest request = new DefaultInvocationRequest();
                request.setPomFile(new File(compilePath));
                request.setGoals(var);
                request.setInputStream(InputStream.nullInputStream());
                Invoker invoker = new DefaultInvoker();
                invoker.setMavenHome(new File(mvnHome));
                try {
                    InvocationResult invocationResult = invoker.execute(request);
                    if(invocationResult.getExitCode() != compileSuccessCode){
                        return false;
                    }
                } catch (MavenInvocationException e) {
                    log.error("maven compile failure message is {}", e.getMessage());
                    return false;
                }
            } else if (compileTool == CompileTool.gradle) {
                compilePath = compilePath.replace("build.gradle", "");
                if (! gradleCompile(compilePath)) {
                    return false;
                }
            }

        }
        return true;
    }

    private static CompileTool getCompileToolByPath(String compilePath) {
        for (CompileTool compileTool : CompileTool.values()) {
            if (compilePath.endsWith(compileTool.compileFile())) {
                return compileTool;
            }
        }
        return CompileTool.maven;
    }


    private static List<String> getCompilePath(String repoPath) {
        File repoFile = new File(repoPath);
        // 查找可编译的文件
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> (file.isFile() && isContainsCompileFile(path)),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(repoFile);
        if (pathList.size() == 0) {
            return null;
        }
        return pathList;
    }

    private static boolean isContainsCompileFile(String path) {
        for (CompileTool s : CompileTool.values()) {
            if (path.endsWith(s.compileFile())) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, ProjectConnection>  projectConnectionMap = new ConcurrentHashMap<>(512);

    private static boolean gradleCompile(String projectDirectory) {

        try {
            Runtime rt = Runtime.getRuntime();
            String command = gradleBin + " " + projectDirectory ;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            process.waitFor();

            int exitValue = process.exitValue();
            log.info("exit valuse is {}", exitValue);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}