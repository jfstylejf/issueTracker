package cn.edu.fudan.measureservice.parser.Cpp.interpreter;

import cn.edu.fudan.measureservice.domain.dto.MethodInfo;
import cn.edu.fudan.measureservice.domain.dto.ParameterPair;
import lombok.Data;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.*;

/**
 * @ClassName: MyExtractListener
 * @Description:
 * @Author wjzho
 * @Date 2021/7/12
 */
@Data
public class CppExtractListener extends CPP14ParserBaseListener{

    CPP14Parser parser;

    private TokenStream tokenStream;

    private List<MethodInfo> methodInfoList;

    private List<MethodInfo> memberMethodInfoList;

    List<ParameterPair> memberList = new ArrayList<>();

    private boolean enterClassOrNot = false;

    private boolean enterFunctionOrNot = false;

    List<ParameterPair> globalParameterList = new ArrayList<>();

    List<ParameterPair> enumList = new ArrayList<>();

    public CppExtractListener(CPP14Parser parser) {
        this.parser = parser;
        tokenStream = parser.getTokenStream();
        methodInfoList = new ArrayList<>();
        memberMethodInfoList = new ArrayList<>();
    }


    @Override
    public void enterSimpleDeclaration(CPP14Parser.SimpleDeclarationContext ctx) {
        // 判断简单表达式是否在方法或类声明中
        if (enterFunctionOrNot || enterClassOrNot) {
            return;
        }
        CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext = ctx.declSpecifierSeq();
        if (declSpecifierSeqContext != null) {
            //判断是类声明或者枚举类声明还是全局变量
            int flag = isGlobalDefinitionOrEnum(declSpecifierSeqContext);
            if (flag == 0) {
                // 若是类声明则直接退出
               return;
            }
            else if(flag ==1){
                //枚举类型变量
                getEnum(ctx);
            }
            else{
                // 此时确定为全局变量，获取全局变量信息
                getGlobalParameter(ctx);
            }


        }
    }


    @Override
    public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {

        enterFunctionOrNot = true;
        MethodInfo methodInfo = new MethodInfo();
        // 判断是否是常规方法申明
        boolean flag = false;
        // 获取方法修饰符
        CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext = ctx.declSpecifierSeq();
        if (declSpecifierSeqContext != null) {
            String funcSpecifier = getSpecifier(declSpecifierSeqContext);
            methodInfo.setSpecifier(funcSpecifier);
        }else {
            // 对于这类结构特判     CBloomFilter() : isFull(true) {}
            flag = true;
        }

        // 获取方法的名字和参数
        CPP14Parser.DeclaratorContext declaratorContext = ctx.declarator();
        String methodName = getDeclarator(declaratorContext);
        if (flag) {
            methodInfo.setSpecifier(methodName);
            CPP14Parser.FunctionBodyContext functionBodyContext = ctx.functionBody();
            if (functionBodyContext.constructorInitializer() != null) {
                CPP14Parser.MemInitializerListContext memInitializerListContext = functionBodyContext.constructorInitializer().memInitializerList();
                methodName = tokenStream.getText(memInitializerListContext.memInitializer(0).meminitializerid());
                methodInfo.setMethodName(methodName);
            }
        }else {
            methodInfo.setMethodName(methodName);
        }
        List<ParameterPair> methodParameterPairList = getParametersAndQualifiers(declaratorContext);
        methodInfo.getMethodParameter().addAll(methodParameterPairList);
        methodInfo.setStartPosition(ctx.start.getLine());
        methodInfo.setEndPosition(ctx.stop.getLine());
        // 统计类声明中的方法定义
        if (enterClassOrNot) {
           memberMethodInfoList.add(methodInfo);
        }else {
            methodInfoList.add(methodInfo);
        }

    }

    @Override
    public void exitFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {
        enterFunctionOrNot = false;
    }


    @Override
    public void enterClassSpecifier(CPP14Parser.ClassSpecifierContext ctx) {
        // 标识进入类标志
        enterClassOrNot = true;
    }

    @Override
    public void exitClassSpecifier(CPP14Parser.ClassSpecifierContext ctx) {
        enterClassOrNot = false;
    }



    /**
     * 获取成员变量
     * @param ctx
     */
    @Override
    public void enterMemberdeclaration(CPP14Parser.MemberdeclarationContext ctx) {

        CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext = ctx.declSpecifierSeq();
        // todo attributeSpecifierSeq 情况未考虑
        String specifier = "";
        if (declSpecifierSeqContext != null) {
           specifier = getSpecifier(declSpecifierSeqContext);
        }
        CPP14Parser.MemberDeclaratorListContext memberDeclaratorListContext = ctx.memberDeclaratorList();

        if(memberDeclaratorListContext != null){
            List<CPP14Parser.MemberDeclaratorContext> memberDeclaratorContexts = memberDeclaratorListContext.memberDeclarator();
            for (CPP14Parser.MemberDeclaratorContext memberDeclaratorContext : memberDeclaratorContexts) {
                CPP14Parser.DeclaratorContext declarator = memberDeclaratorContext.declarator();
                String memberDeclarator = getDeclarator(declarator);
                boolean flag = isMemberDefinitionOrNot(declarator);
                if (flag) {
                    // 获取加成员变量相关信息
                    ParameterPair parameterPair = new ParameterPair();
                    parameterPair.setSpecifier(specifier);
                    parameterPair.setParameterName(memberDeclarator);
                    parameterPair.setStartPosition(memberDeclaratorContext.start.getLine());
                    parameterPair.setEndPosition(memberDeclaratorContext.stop.getLine());
                    memberList.add(parameterPair);
                } else {
                    MethodInfo methodInfo = new MethodInfo();
                    methodInfo.setMethodName(memberDeclarator);
                    methodInfo.setSpecifier(specifier);
                    List<ParameterPair> methodParameterPairList = getParametersAndQualifiers(declarator);
                    methodInfo.getMethodParameter().addAll(methodParameterPairList);
                    methodInfo.setStartPosition(memberDeclaratorContext.start.getLine());
                    methodInfo.setEndPosition(memberDeclaratorContext.stop.getLine());
                    memberMethodInfoList.add(methodInfo);

                }

            }
        }



    }

    /**
     * 判断是否是类成员变量或是类声明方法
     * @param declarator 声明内容
     * @return 若为false,则是声明方法内容，若为 true,则为成员变量
     */
    private boolean isMemberDefinitionOrNot(CPP14Parser.DeclaratorContext declarator) {
        Objects.requireNonNull(declarator);
        if (declarator.pointerDeclarator() != null) {
            CPP14Parser.NoPointerDeclaratorContext noPointerDeclaratorContext = declarator.pointerDeclarator().noPointerDeclarator();
            // todo : LeftParen pointerDeclarator RightParen 结构未处理
            if (noPointerDeclaratorContext.noPointerDeclarator() != null) {
                // 此时进入 noPointerDeclarator ( parametersAndQualifiers | ) 结构, 表明是类声明方法
                return false;
            }else {
                // 对应于签名结构 declaratorid attributeSpecifierSeq?
               return true;
            }
        }else {
            // todo 未考虑 noPointerDeclarator parametersAndQualifiers trailingReturnType 这一类型的定义
        }
        return false;
    }

    /**
     * 判段是否是全局变量或者枚举类，或者类声明，若为类声明则有 classSpecifier 的形式，枚举类则是形式
     * 类声明则返回0
     * 枚举类返回1
     * 全局变量返回2
     * @param declSpecifierSeqContext
     * @return
     */
    private int isGlobalDefinitionOrEnum(CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext) {
        Objects.requireNonNull(declSpecifierSeqContext);
        CPP14Parser.TypeSpecifierContext typeSpecifierContext = null;
        for (CPP14Parser.DeclSpecifierContext declSpecifierContext : declSpecifierSeqContext.declSpecifier()) {
            typeSpecifierContext = declSpecifierContext.typeSpecifier();
            if (typeSpecifierContext != null) {
                break;
            }
        }
        // 目前简单表达式修饰符类型只考虑了 typeSpecifier
        if (typeSpecifierContext != null) {
            // 若是类修饰和枚举修饰则代表不是全局变量
            if(typeSpecifierContext.classSpecifier() != null) {
                return 0;
            } else if(typeSpecifierContext.enumSpecifier() != null) {
                return 1;
            } else {
                return 2;
            }
        }
        // warning  不清楚是否有 static class 这种类声明，若有则需要继续区分之后的修饰符是否包含 类或枚举 修饰
        return 0;
    }

    private String getSpecifier(CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext) {
        Objects.requireNonNull(declSpecifierSeqContext);
        StringBuilder sb = new StringBuilder();
        List<CPP14Parser.DeclSpecifierContext> declSpecifierContextList = declSpecifierSeqContext.declSpecifier();
        for (CPP14Parser.DeclSpecifierContext declSpecifierContext : declSpecifierContextList) {
            sb.append(tokenStream.getText(declSpecifierContext));
            sb.append(" ");
        }
        return sb.toString();
    }

    /**
     * 获取签名
     * @param declaratorContext 声明内容
     * @return
     */
    private String getDeclarator(CPP14Parser.DeclaratorContext declaratorContext) {
        Objects.requireNonNull(declaratorContext);
        if (declaratorContext.pointerDeclarator() != null) {
            CPP14Parser.NoPointerDeclaratorContext noPointerDeclaratorContext = declaratorContext.pointerDeclarator().noPointerDeclarator();
            // todo : LeftParen pointerDeclarator RightParen 结构未处理
            if (noPointerDeclaratorContext.noPointerDeclarator() != null) {
                // 获取签名
                CPP14Parser.IdExpressionContext idExpressionContext = noPointerDeclaratorContext.noPointerDeclarator().declaratorid().idExpression();
                return extractNameFromIdExpression(idExpressionContext);
            }else {
                // 对应于签名结构 declaratorid attributeSpecifierSeq?
                CPP14Parser.IdExpressionContext idExpressionContext = noPointerDeclaratorContext.declaratorid().idExpression();
                return extractNameFromIdExpression(idExpressionContext);
            }
        }else {
            // todo 未考虑 noPointerDeclarator parametersAndQualifiers trailingReturnType 这一类型的定义
        }
        return "";
    }

    private String extractNameFromIdExpression(CPP14Parser.IdExpressionContext idExpressionContext) {
        Objects.requireNonNull(idExpressionContext);
        if (idExpressionContext.unqualifiedId() != null) {
            return tokenStream.getText(idExpressionContext.unqualifiedId());
        }else {
            // 型如 void CoinControlDialog::setModel ，有分域操作符时特殊处理
            return tokenStream.getText(idExpressionContext.qualifiedId().unqualifiedId());
        }
    }

    /**
     * 获取参数的签名和修饰符
     * @param declaratorContext
     * @return
     */
    private List<ParameterPair> getParametersAndQualifiers(CPP14Parser.DeclaratorContext declaratorContext) {
        Objects.requireNonNull(declaratorContext);
        List<ParameterPair> parameterPairList = new ArrayList<>();
        if (declaratorContext.pointerDeclarator() != null) {
            CPP14Parser.NoPointerDeclaratorContext noPointerDeclaratorContext = declaratorContext.pointerDeclarator().noPointerDeclarator();
            // 获取参数
            CPP14Parser.ParametersAndQualifiersContext parametersAndQualifiersContext = noPointerDeclaratorContext.parametersAndQualifiers();
            CPP14Parser.ParameterDeclarationClauseContext parameterDeclarationClauseContext = parametersAndQualifiersContext.parameterDeclarationClause();
            if (parameterDeclarationClauseContext != null) {
                // 此时有参数
                CPP14Parser.ParameterDeclarationListContext parameterDeclarationListContext = parameterDeclarationClauseContext.parameterDeclarationList();
                for (CPP14Parser.ParameterDeclarationContext parameterDeclarationContext : parameterDeclarationListContext.parameterDeclaration()) {
                    ParameterPair parameterPair = new ParameterPair();
                    String declSpecifier = getSpecifier(parameterDeclarationContext.declSpecifierSeq());
                    parameterPair.setSpecifier(declSpecifier);
                    if (parameterDeclarationContext.declarator() != null) {
                        String parameterName = getDeclarator(parameterDeclarationContext.declarator());
                        parameterPair.setParameterName(parameterName);
                        parameterPair.setStartPosition(parameterDeclarationContext.start.getLine());
                        parameterPair.setEndPosition(parameterDeclarationContext.stop.getLine());
                        parameterPairList.add(parameterPair);
                    }
                }
            }
        }else {
            // todo 未考虑 noPointerDeclarator parametersAndQualifiers trailingReturnType 这一类型的定义
        }
        return parameterPairList;
    }


    /**
     * 通过SimpleDeclaration获取到枚举类变量存入enumList
     * @param ctx
     */
    private void getEnum(CPP14Parser.SimpleDeclarationContext ctx){
        CPP14Parser.TypeSpecifierContext typeSpecifierContext = ctx.declSpecifierSeq().declSpecifier(0).typeSpecifier();
        CPP14Parser.EnumSpecifierContext enumSpecifier = typeSpecifierContext.enumSpecifier();
        if (enumSpecifier != null) {

            CPP14Parser.EnumeratorListContext enumeratorListContext = enumSpecifier.enumeratorList();
            if (enumeratorListContext != null) {
                for (CPP14Parser.EnumeratorDefinitionContext enumeratorDefinitionContext : enumeratorListContext.enumeratorDefinition()) {
                    ParameterPair parameterPair = new ParameterPair();
                    String enumerator = tokenStream.getText(enumeratorDefinitionContext.enumerator());
                    //parameterPair.setSpecifier(specifier);
                    parameterPair.setParameterName(enumerator);
                    parameterPair.setStartPosition(enumeratorDefinitionContext.start.getLine());
                    parameterPair.setEndPosition(enumeratorDefinitionContext.stop.getLine());
                    enumList.add(parameterPair);

                }
            }
        }
    }

    /**
     * 通过SimpleDeclaration获取到全局变量存入globalParameterList
     * @param ctx
     */
    private void getGlobalParameter(CPP14Parser.SimpleDeclarationContext ctx){
        CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext= ctx.declSpecifierSeq();
        String specifier = getSpecifier(declSpecifierSeqContext);
        CPP14Parser.InitDeclaratorListContext initDeclaratorListContext = ctx.initDeclaratorList();
        // 类似于 class COutPoint; 这一结构，需要特判 initDeclaratorList 是否为 null
        if (initDeclaratorListContext != null) {
            for (CPP14Parser.InitDeclaratorContext initDeclaratorContext : initDeclaratorListContext.initDeclarator()) {
                ParameterPair parameterPair = new ParameterPair();
                String declarator = getDeclarator(initDeclaratorContext.declarator());
                parameterPair.setSpecifier(specifier);
                parameterPair.setParameterName(declarator);
                parameterPair.setStartPosition(initDeclaratorContext.start.getLine());
                parameterPair.setEndPosition(initDeclaratorContext.stop.getLine());
                globalParameterList.add(parameterPair);
            }
        }
    }







}
