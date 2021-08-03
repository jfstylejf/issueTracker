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

    List<ParameterPair> memberList = new ArrayList<>();

    private boolean enterClassOrNot = false;

    private boolean enterFunctionOrNot = false;

    List<ParameterPair> globalParameterList = new ArrayList<>();

    public CppExtractListener(CPP14Parser parser) {
        this.parser = parser;
        tokenStream = parser.getTokenStream();
        methodInfoList = new ArrayList<>();
    }


    @Override
    public void enterSimpleDeclaration(CPP14Parser.SimpleDeclarationContext ctx) {
        // 判断简单表达式是否在方法或类声明中
        if (enterFunctionOrNot || enterClassOrNot) {
            return;
        }
        CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext = ctx.declSpecifierSeq();
        if (declSpecifierSeqContext != null) {
            Boolean flag = isGlobalDefinitionOrNot(declSpecifierSeqContext);
            if (!flag) {
                // 若非全局变量则直接退出
                return;
            }
            // 此时确定为全局变量，获取全局变量信息
            String specifier = getSpecifier(declSpecifierSeqContext);
            CPP14Parser.InitDeclaratorListContext initDeclaratorListContext = ctx.initDeclaratorList();
            if(initDeclaratorListContext != null){
                for (CPP14Parser.InitDeclaratorContext initDeclaratorContext : initDeclaratorListContext.initDeclarator()) {
                    ParameterPair parameterPair = new ParameterPair();
                    String declarator = tokenStream.getText(initDeclaratorContext.declarator());
                    parameterPair.setSpecifier(specifier);
                    parameterPair.setParameterName(declarator);
                    parameterPair.setStartPosition(ctx.start.getLine());
                    parameterPair.setEndPosition(ctx.stop.getLine());
                    globalParameterList.add(parameterPair);
                }
            }

            //枚举类型变量
            CPP14Parser.TypeSpecifierContext typeSpecifierContext = ctx.declSpecifierSeq().declSpecifier(0).typeSpecifier();
            CPP14Parser.EnumSpecifierContext enumSpecifier = typeSpecifierContext.enumSpecifier();
            if(enumSpecifier != null){

                CPP14Parser.EnumeratorListContext enumeratorListContext= enumSpecifier.enumeratorList();
                if(enumeratorListContext != null){
                    for(CPP14Parser.EnumeratorDefinitionContext enumeratorDefinitionContext : enumeratorListContext.enumeratorDefinition()) {
                        ParameterPair parameterPair = new ParameterPair();
                        String enumerator = tokenStream.getText(enumeratorDefinitionContext.enumerator());
                        //parameterPair.setSpecifier(specifier);
                        parameterPair.setParameterName(enumerator);
                        parameterPair.setStartPosition(enumeratorDefinitionContext.start.getLine());
                        parameterPair.setEndPosition(enumeratorDefinitionContext.start.getLine());
                        memberList.add(parameterPair);

                    }
                }
            }



        }

    }

    @Override
    public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {

        enterFunctionOrNot = true;
        MethodInfo methodInfo = new MethodInfo();

        // 获取方法修饰符
        CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext = ctx.declSpecifierSeq();
        if (declSpecifierSeqContext != null) {
            String funcSpecifier = getSpecifier(declSpecifierSeqContext);
            methodInfo.setSpecifier(funcSpecifier);
        }else {
            // todo 还有 attributeSpecifierSeq， virtualSpecifierSeq 存在的情况需要再考虑
        }

        // 获取方法的名字和参数
        String methodName;
        CPP14Parser.DeclaratorContext declaratorContext = ctx.declarator();
        if (declaratorContext.pointerDeclarator() != null) {
            CPP14Parser.NoPointerDeclaratorContext noPointerDeclaratorContext = declaratorContext.pointerDeclarator().noPointerDeclarator();
            // todo : LeftParen pointerDeclarator RightParen 结构未处理
            if (noPointerDeclaratorContext.noPointerDeclarator() != null) {
                // 获取方法名
                CPP14Parser.IdExpressionContext idExpressionContext = noPointerDeclaratorContext.noPointerDeclarator().declaratorid().idExpression();
                if (idExpressionContext.unqualifiedId() != null) {
                    methodName = tokenStream.getText(idExpressionContext.unqualifiedId());
                    methodInfo.setMethodName(methodName);
                }
                //使用双冒号定义的方法
                else if(idExpressionContext.qualifiedId() != null){
                    methodName = tokenStream.getText(idExpressionContext.qualifiedId().unqualifiedId());
                    methodInfo.setMethodName(methodName);
                }// todo 处理有修饰符的情况
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
                            String parameterName = tokenStream.getText(parameterDeclarationContext.declarator());
                            parameterPair.setParameterName(parameterName);
                            parameterPair.setStartPosition(parameterDeclarationContext.start.getLine());
                            parameterPair.setEndPosition(parameterDeclarationContext.stop.getLine());
                        }
                        methodInfo.getMethodParameter().add(parameterPair);
                    }
                }
                methodInfo.setStartPosition(ctx.start.getLine());
                methodInfo.setEndPosition(ctx.stop.getLine());
            }else {
                // todo : LeftParen pointerDeclarator RightParen 结构未处理 以及 declaratorid attributeSpecifierSeq? 未处理
            }


        }else {
            // todo 未考虑 noPointerDeclarator parametersAndQualifiers trailingReturnType 这一类型的定义
        }

        methodInfoList.add(methodInfo);

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
                // 获取加成员变量相关信息
                ParameterPair parameterPair = new ParameterPair();
                String memberDeclarator = tokenStream.getText(memberDeclaratorContext);
                parameterPair.setSpecifier(specifier);
                parameterPair.setParameterName(memberDeclarator);
                parameterPair.setStartPosition(ctx.start.getLine());
                parameterPair.setEndPosition(ctx.stop.getLine());
                memberList.add(parameterPair);
            }
        }



    }

    /**
     * 判段是否是全局变量，若为类声明则有 classSpecifier 的形式
     * @param declSpecifierSeqContext
     * @return
     */
    private Boolean isGlobalDefinitionOrNot(CPP14Parser.DeclSpecifierSeqContext declSpecifierSeqContext) {
        Objects.requireNonNull(declSpecifierSeqContext);
        CPP14Parser.DeclSpecifierContext declSpecifierContext = declSpecifierSeqContext.declSpecifier(0);
        // 目前简单表达式修饰符类型只考虑了 typeSpecifier
        CPP14Parser.TypeSpecifierContext typeSpecifierContext = declSpecifierContext.typeSpecifier();
        if (typeSpecifierContext != null) {
            return typeSpecifierContext.classSpecifier() == null;
        }
        return false;
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






}
