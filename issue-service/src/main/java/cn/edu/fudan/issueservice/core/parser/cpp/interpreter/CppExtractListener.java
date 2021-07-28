package cn.edu.fudan.issueservice.core.parser.cpp.interpreter;

import cn.edu.fudan.issueservice.domain.dto.MethodInfo;
import cn.edu.fudan.issueservice.domain.dto.ParameterPair;
import lombok.Data;
import org.antlr.v4.runtime.TokenStream;

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

    public CppExtractListener(CPP14Parser parser) {
        this.parser = parser;
        tokenStream = parser.getTokenStream();
        methodInfoList = new ArrayList<>();
    }

    @Override
    public void enterFunctionDefinition(CPP14Parser.FunctionDefinitionContext ctx) {

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
                }else {
                    // todo 处理有修饰符的情况
                }
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
    public void enterFunctionBody(CPP14Parser.FunctionBodyContext ctx) {

    }

    @Override
    public void enterClassName(CPP14Parser.ClassNameContext ctx) {

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
        if (memberDeclaratorListContext != null) {
            List<CPP14Parser.MemberDeclaratorContext> memberDeclaratorContexts = memberDeclaratorListContext.memberDeclarator();
            for (CPP14Parser.MemberDeclaratorContext memberDeclaratorContext : memberDeclaratorContexts) {
                ParameterPair parameterPair = new ParameterPair();
                String memberDeclarator = tokenStream.getText(memberDeclaratorContext);
                parameterPair.setSpecifier(specifier);
                parameterPair.setParameterName(memberDeclarator);
                memberList.add(parameterPair);
            }
        }


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
