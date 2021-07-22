package cn.edu.fudan.measureservice.parser.Java.interpreter;

import org.antlr.v4.runtime.TokenStream;

/**
 * @ClassName: MyExtractListener
 * @Description:
 * @Author wjzho
 * @Date 2021/7/9
 */

public class MyExtractListener extends Java8ParserBaseListener{

    Java8Parser java8Parser;

    public MyExtractListener(Java8Parser java8Parser) {
        this.java8Parser = java8Parser;
    }

    @Override
    public void enterClassDeclaration(Java8Parser.ClassDeclarationContext ctx) {
        System.out.println("类的modifier为： " + ctx.normalClassDeclaration().classModifier());
        System.out.println("类名为 ： " + ctx.normalClassDeclaration().Identifier());

    }

    @Override
    public void enterMethodDeclaration(Java8Parser.MethodDeclarationContext ctx) {
        /*TokenStream tokenStream = java8Parser.getTokenStream();
        System.out.println(ctx.methodHeader());
        System.out.println(ctx.methodModifier());
        System.out.println(ctx.methodBody());
        System.out.println(tokenStream.getText());*/
    }

}
