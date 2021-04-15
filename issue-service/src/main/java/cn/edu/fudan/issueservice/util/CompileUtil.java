package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.enums.CompileTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.shared.invoker.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private static int compileMaxWaitTime;

    @Value("${mvnHome}")
    public void setMvnHome(String mvnHome) {
        CompileUtil.mvnHome = mvnHome;
    }

    @Value("${gradleBin}")
    public void setGradleBin(String gradleBin) {
        CompileUtil.gradleBin = gradleBin;
    }

    @Value("${compile.maxWaitTime}")
    public void setCompileMaxWaitTime(int compileMaxWaitTime) {
        CompileUtil.compileMaxWaitTime = compileMaxWaitTime;
    }

    private static final String[] VAR = {"compile", "-Dmaven.test.skip=true", "-ff",
            "-B", "-q", "-l /dev/null", "-Dmaven.compile.fork=true", "-T 2C"};

    private static final List<String> var = java.util.Arrays.asList(VAR);

    private static class CompileThread extends Thread implements Runnable {

        private final Invoker invoker;

        private final InvocationRequest invocationRequest;
        //2表示等待执行结果,0成功编译,1编译失败
        public int compileSuccess = 2;

        public CompileThread(Invoker invoker, InvocationRequest invocationRequest) {
            this.invoker = invoker;
            this.invocationRequest = invocationRequest;
        }

        @Override
        public void run() {
            try {
                InvocationResult invocationResult = invoker.execute(invocationRequest);
                compileSuccess = invocationResult.getExitCode() == 0 ? 0 : 1;
            } catch (MavenInvocationException e) {
                log.error("MavenInvocationException,compile failed!");
            }
        }
    }

    public static boolean isCompilable(String repoPath) {

        List<String> compilePathList = PomAnalysisUtil.getMainPom(getCompilePath(repoPath));
        if (compilePathList == null || compilePathList.isEmpty()) {
            /// fixme 特殊处理  后面在看
            return gradleCompile(repoPath);
        }

        for (String compilePath : compilePathList) {
            CompileTool compileTool = getCompileToolByPath(compilePath);
            log.info("Compile Path is {}", compilePath);
            log.info("Compile Tool is {}", compileTool.name());
            // TODO 应该根据接口来动态的根据 compileTool 调用相应的实现方法
            if (compileTool == CompileTool.MAVEN) {
                if (!mvnCompile(compilePath)) {
                    return false;
                }
            } else if (compileTool == CompileTool.GRADLE) {
                compilePath = compilePath.replace("build.gradle", "");
                if (!gradleCompile(compilePath)) {
                    return false;
                }
            }

        }
        return true;
    }

    private static boolean mvnCompile(String compilePath) {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(new File(compilePath));
        request.setGoals(var);
        request.setInputStream(InputStream.nullInputStream());
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(mvnHome));
        //启动编译线程
        CompileThread compileThread = new CompileThread(invoker, request);
        compileThread.start();
        //最多等待编译线程执行60s
        try {
            TimeUnit.SECONDS.timedJoin(compileThread, compileMaxWaitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return compileThread.compileSuccess == 0;
    }

    private static boolean gradleCompile(String projectDirectory) {

        try {
            Runtime rt = Runtime.getRuntime();
            String command = gradleBin + " " + projectDirectory;
            log.info("command -> {}", command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(compileMaxWaitTime, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("compile gradle timeout ! ({}s)", compileMaxWaitTime);
                return false;
            }
            log.info("exit value is {}", process.exitValue());
            return process.exitValue() == 0;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("gradleCompile Exception!");
            return false;
        }
    }

    private static CompileTool getCompileToolByPath(String compilePath) {
        for (CompileTool compileTool : CompileTool.values()) {
            if (compilePath.endsWith(compileTool.compileFile())) {
                return compileTool;
            }
        }
        return CompileTool.MAVEN;
    }

    private static List<String> getCompilePath(String repoPath) {
        File repoFile = new File(repoPath);
        // 查找可编译的文件
        List<String> pathList = new ArrayList<>();
        new DirExplorer((level, path, file) -> (file.isFile() && isContainsCompileFile(path)),
                (level, path, file) -> pathList.add(file.getAbsolutePath())).explore(repoFile);
        if (pathList.size() == 0) {
            return new ArrayList<>();
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

}