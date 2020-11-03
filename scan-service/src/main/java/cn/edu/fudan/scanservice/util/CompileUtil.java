package cn.edu.fudan.scanservice.util;

import cn.edu.fudan.scanservice.ScanServiceApplication;
import cn.edu.fudan.scanservice.domain.enums.CompileTool;
import org.apache.maven.shared.invoker.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * description: 判断指定目录下的代码是否可以编译
 *
 * @author fancying
 * create: 2020-03-10 21:30
 **/
@Component
public class CompileUtil {

    @Value("${mvnHome}")
    private  String mvnHome;

    private static String gradlewHome;

//    static {
//        ConfigurableApplicationContext ctx = SpringApplication.run(ScanServiceApplication.class, "");
//        mvnHome = ctx.getEnvironment().getProperty("mvnHome");
//        gradlewHome = ctx.getEnvironment().getProperty("gradlewHome");
//    }

    public  boolean isCompilable(String repoPath) {
        final int compileSuccessCode = 0;

        List<String> compilePathList = getCompilePath(repoPath);
        if (compilePathList == null || compilePathList.size() == 0) {
            return false;
        }
        for (String compilePath : compilePathList) {
            CompileTool compileTool = getCompileToolByPath(compilePath);
            // TODO 应该根据接口来动态的根据 compileTool 调用相应的实现方法
            if (compileTool == CompileTool.maven) {
                InvocationRequest request = new DefaultInvocationRequest();
                request.setPomFile(new File(compilePath));
                request.setGoals(Collections.singletonList("compile"));
                Invoker invoker = new DefaultInvoker();
                invoker.setMavenHome(new File(mvnHome));
                try {
                    InvocationResult invocationResult = invoker.execute(request);
                    return invocationResult.getExitCode() == compileSuccessCode;
                } catch (MavenInvocationException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
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
        new DirExplorer((level, path, file) -> (isContainsCompileFile(path)),
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


}