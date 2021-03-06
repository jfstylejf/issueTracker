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
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
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
        CPP14Parser parser = initFileParser(file);
        ParseTree tree = parser.translationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();
        CppExtractListener listener = new CppExtractListener(parser);
        // 通过监听器遍历解析路径
        walker.walk(listener,tree);
        // 封装 c++ 文件解析内容
        List<ParameterPair> memberList = listener.getMemberList();
        List<MethodInfo> methodInfos = listener.getMethodInfoList();
        FileInfo fileInfo = FileInfo.builder()
                .methodInfoList(methodInfos)
                .memberList(memberList)
                .globalParameterList(listener.getGlobalParameterList())
                .absolutePath(file)
                .enumList(listener.getEnumList())
                .memberMethodList(listener.getMemberMethodInfoList())
                .build();


        return fileInfo;

    }

    private static String parseFileTree(String file) throws IOException {
        CPP14Parser parser = initFileParser(file);
        CPP14Parser.TranslationUnitContext tree = parser.translationUnit();
        return tree.toStringTree(parser);
    }

    private static CPP14Parser initFileParser(String file) throws IOException {
        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromFileName(file));
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        return new CPP14Parser(tokens);
    }


    public static void main(String[] args) throws IOException {

        String fileName = "C:\\Users\\wjzho\\Desktop\\walletdb.cpp";
        FileInfo fileInfo = CppCodeAnalyzer.parseFile(fileName);


        System.out.println(CppCodeAnalyzer.parseFileTree(fileName));

        System.out.println("MethodInfo is : {");
        for (MethodInfo methodInfo : fileInfo.getMethodInfoList()) {
            System.out.println("  " + methodInfo.getSpecifier());
            System.out.println("  " + methodInfo.getMethodName());
            System.out.println("  " + methodInfo.getMethodParameter());
            System.out.println(" start: " + methodInfo.getStartPosition() + " end : " + methodInfo.getEndPosition());
            System.out.println();
        }
        System.out.println("}");
        System.out.println();
        System.out.println("GlobalInfo is : {");
        for (ParameterPair parameterPair : fileInfo.getGlobalParameterList()) {
            System.out.println("  " + parameterPair.getSpecifier());
            System.out.println("  " + parameterPair.getParameterName());
            System.out.println(" start: " + parameterPair.getStartPosition() + " end : " + parameterPair.getEndPosition());
            System.out.println();
        }
        System.out.println("}");
        System.out.println();

        System.out.println("MemberInfo is : {");
        for (ParameterPair parameterPair : fileInfo.getMemberList()) {
            System.out.println("  " + parameterPair.getSpecifier());
            System.out.println("  " + parameterPair.getParameterName());
            System.out.println(" start: " + parameterPair.getStartPosition() + " end : " + parameterPair.getEndPosition());
            System.out.println();
        }
        System.out.println("}");

        System.out.println("memberMethodInfo is : {");
        for (MethodInfo methodInfo : fileInfo.getMemberMethodList()) {
            System.out.println("  " + methodInfo.getSpecifier());
            System.out.println("  " + methodInfo.getMethodName());
            System.out.println("  " + methodInfo.getMethodParameter());
            System.out.println(" start: " + methodInfo.getStartPosition() + " end : " + methodInfo.getEndPosition());
            System.out.println();
        }
        System.out.println("}");
//        System.out.println(fileInfo.getMemberList());

    }

}
