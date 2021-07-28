package cn.edu.fudan.issueservice.core.parser.cpp;

import cn.edu.fudan.issueservice.core.parser.cpp.interpreter.CPP14Lexer;
import cn.edu.fudan.issueservice.core.parser.cpp.interpreter.CPP14Parser;
import cn.edu.fudan.issueservice.core.parser.cpp.interpreter.CppExtractListener;
import cn.edu.fudan.issueservice.domain.dto.FileInfo;
import cn.edu.fudan.issueservice.domain.dto.MethodInfo;
import cn.edu.fudan.issueservice.domain.dto.ParameterPair;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;
import java.util.List;

/**
 * @ClassName: CppCodeAnalyzer
 * @Description: c++ 解析逻辑
 * @Author wjzho
 * @Date 2021/7/2
 */
public class CppCodeAnalyzer {

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
