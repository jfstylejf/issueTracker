package cn.edu.fudan.issueservice.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author fancying
 */
@Slf4j
public class AstParserUtil {

    public static Object[] findMethodNameAndOffset(String filePath, int beginLine, int endLine) {
        try {
            CompilationUnit compilationUnit = JavaParser.parse(Paths.get(filePath), StandardCharsets.UTF_8);
            List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            //判断是否是enum
            if (classOrInterfaceDeclarationList.isEmpty()) {
                List<EnumConstantDeclaration> enumConstantDeclarationList = compilationUnit.findAll(EnumConstantDeclaration.class);
                for (EnumConstantDeclaration enumConstantDeclaration : enumConstantDeclarationList) {
                    if (enumConstantDeclaration.getRange().isPresent()) {
                        int begin = enumConstantDeclaration.getRange().get().begin.line;
                        int end = enumConstantDeclaration.getRange().get().end.line;
                        if (beginLine >= begin && endLine <= end) {
                            return new Object[]{"enum", beginLine - begin};
                        }
                    }
                }
            }
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarationList) {
                //构造函数
                List<ConstructorDeclaration> constructorDeclarations = classOrInterfaceDeclaration.findAll(ConstructorDeclaration.class);
                for (ConstructorDeclaration constructorDeclaration : constructorDeclarations) {
                    if (constructorDeclaration.getRange().isPresent()) {
                        int begin = constructorDeclaration.getRange().get().begin.line;
                        int end = constructorDeclaration.getRange().get().end.line;
                        if (beginLine >= begin && endLine <= end) {
                            return new Object[]{constructorDeclaration.getSignature().toString(), beginLine - begin};
                        }
                    }
                }
                //一般函数
                List<MethodDeclaration> methodDeclarations = classOrInterfaceDeclaration.findAll(MethodDeclaration.class);
                for (MethodDeclaration methodDeclaration : methodDeclarations) {
                    if (methodDeclaration.getRange().isPresent()) {
                        int begin = methodDeclaration.getRange().get().begin.line;
                        int end = methodDeclaration.getRange().get().end.line;
                        if (beginLine >= begin && endLine <= end) {
                            return new Object[]{methodDeclaration.getSignature().toString(), beginLine - begin};
                        }
                    }
                }
                //字段
                List<FieldDeclaration> fieldDeclarations = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
                for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
                    if (fieldDeclaration.getRange().isPresent()) {
                        int begin = fieldDeclaration.getRange().get().begin.line;
                        int end = fieldDeclaration.getRange().get().end.line;
                        if (beginLine >= begin && endLine <= end) {
                            StringBuilder simpleName = new StringBuilder();
                            for (VariableDeclarator variableDeclarator : fieldDeclaration.getVariables()) {
                                simpleName.append(variableDeclarator.getName());
                                simpleName.append(" ");
                            }
                            return new Object[]{simpleName.toString(), beginLine - begin};
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Set<String> getAllMethodAndFieldName(String filePath) {
        Set<String> methodsAndFields = new HashSet<>();

        List<String> allFieldsInFile = getAllFieldsInFile(filePath);
        List<String> allMethodsInFile = getAllMethodsInFile(filePath);

        methodsAndFields.addAll(allFieldsInFile);
        methodsAndFields.addAll(allMethodsInFile);

        return methodsAndFields;
    }

    /**
     * 抽java文件中所有方法签名
     *
     * @param file 文件路径
     * @return 所有方法签名
     */
    public static List<String> getAllMethodsInFile(String file) {
        List<String> allMethodsInFile = new ArrayList<>();
        try {
            CompilationUnit compileUtil = JavaParser.parse(Paths.get(file), StandardCharsets.UTF_8);
            List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compileUtil.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
                List<MethodDeclaration> methodDeclarations = classOrInterfaceDeclaration.findAll(MethodDeclaration.class);
                for (MethodDeclaration methodDeclaration : methodDeclarations) {
                    String method = methodDeclaration.getSignature().toString();
                    allMethodsInFile.add(method);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return allMethodsInFile;
    }

    /**
     * 抽一个java文件中所有成员变量名
     *
     * @param file 文件路径
     * @return 成员变量list列表
     */
    public static List<String> getAllFieldsInFile(String file) {
        List<String> allFieldsInFile = new ArrayList<>();
        try {
            CompilationUnit compileUtil = JavaParser.parse(Paths.get(file), StandardCharsets.UTF_8);
            List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compileUtil.findAll(ClassOrInterfaceDeclaration.class);
            for (ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations) {
                List<FieldDeclaration> fieldDeclarations = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
                for (FieldDeclaration fieldDeclaration : fieldDeclarations) {
                    NodeList<VariableDeclarator> variables = fieldDeclaration.getVariables();
                    for (VariableDeclarator variable : variables) {
                        allFieldsInFile.add(variable.getName().toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
        return allFieldsInFile;
    }
}
