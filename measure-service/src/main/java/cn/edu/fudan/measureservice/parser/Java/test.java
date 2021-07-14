package cn.edu.fudan.measureservice.parser.Java;

import cn.edu.fudan.measureservice.parser.Java.interpreter.Java8Lexer;
import cn.edu.fudan.measureservice.parser.Java.interpreter.Java8Parser;
import cn.edu.fudan.measureservice.parser.Java.interpreter.Java8ParserBaseListener;
import cn.edu.fudan.measureservice.parser.Java.interpreter.MyExtractListener;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;

/**
 * @ClassName: test
 * @Description:
 * @Author wjzho
 * @Date 2021/7/5
 */

public class test {
    public static void main(String[] args) throws IOException {

        Java8Lexer lexer = new Java8Lexer(CharStreams.fromFileName("C:\\work\\IssueTracker-Master\\measure-service\\src\\main\\java\\cn\\edu\\fudan\\measureservice\\service\\MeasureRepoService.java"));

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        Java8Parser parser = new Java8Parser(tokens);

        ParseTree tree = parser.compilationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();

        MyExtractListener listener = new MyExtractListener(parser);

        walker.walk(listener,tree);



    }
}
