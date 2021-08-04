package cn.edu.fudan.measureservice.parser.Cpp;

import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CPP14Lexer;
import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CPP14Parser;
import cn.edu.fudan.measureservice.parser.Cpp.interpreter.CppExtractListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.IOException;

/**
 * @ClassName: test
 * @Description:
 * @Author wjzho
 * @Date 2021/7/8
 */

public class test {

    public static void main(String[] args) throws IOException {

        CPP14Lexer lexer = new CPP14Lexer(CharStreams.fromFileName("/Users/keyon/Documents/bigDataPlatform/cppFiles/b.cpp"));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        CPP14Parser parser = new CPP14Parser(tokens);

        ParseTree tree = parser.translationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();

        CppExtractListener listener = new CppExtractListener(parser);

        walker.walk(listener,tree);

        System.out.println(listener.getMemberList());
    }

}
