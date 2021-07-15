package cn.edu.fudan.measureservice.core.process;

import cn.edu.fudan.measureservice.domain.dto.FileInfo;
import cn.edu.fudan.measureservice.domain.dto.MethodInfo;
import cn.edu.fudan.measureservice.domain.dto.ParameterPair;
import cn.edu.fudan.measureservice.filter.CppFileFilter;
import cn.edu.fudan.measureservice.filter.FileFilter;
import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CPP14Lexer;
import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CPP14Parser;
import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CppExtractListener;
import cn.edu.fudan.measureservice.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CppCodeAnalyzer
 * @Description: c++ 解析逻辑
 * @Author wjzho
 * @Date 2021/7/2
 */
@Slf4j
public class CppCodeAnalyzer extends BaseAnalyzer{

    private List<String> cppFiles;


    @Override
    public boolean invoke() {
        cppFiles = new ArrayList<>();
        FileFilter filter = new CppFileFilter();
        List<String> fileList;
        try {
            fileList = FileUtil.getFilenames(new File(repoPath),filter);
        }catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        cppFiles.addAll(fileList);
        return true;
    }

    @Override
    public boolean analyze() {
        long parserStart = System.currentTimeMillis();
        for (String file : cppFiles) {

        }
        long parserStop = System.currentTimeMillis();
        System.out.println("Total lexer+parser time " + (parserStop - parserStart) + "ms.");
        return false;
    }

    public static FileInfo parseFile(String file) throws IOException {

        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromFileName(file));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        CPP14Parser parser = new CPP14Parser(tokens);

        ParseTree tree = parser.translationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();

        CppExtractListener listener = new CppExtractListener(parser);

        walker.walk(listener,tree);

        List<ParameterPair> memberList = listener.getMemberList();

        List<MethodInfo> methodInfos = listener.getMethodInfoList();

        FileInfo fileInfo = FileInfo.builder()
                .methodInfoList(methodInfos)
                .memberList(memberList)
                .absolutePath(file)
                .build();

        return fileInfo;

    }


}
