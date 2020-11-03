package cn.edu.fudan.issueservice.util;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;

import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author fancying
 */
public class AstParserUtil {


    public static Set<String> getAllMethodAndFieldName(String filePath) {
        Set<String> methodsAndFields = new HashSet<>();

        List<String> allFieldsInFile = getAllFieldsInFile(filePath);
        List<String> allMethodsInFile = getAllMethodsInFile(filePath);

        allFieldsInFile.forEach(field -> methodsAndFields.add(field));
        allMethodsInFile.forEach(method -> methodsAndFields.add(method));

        return methodsAndFields;
    }

    public static String findMethod(String filePath, int beginLine, int endLine) {
        try {
            CompilationUnit compilationUnit = JavaParser.parse(Paths.get(filePath), Charset.forName("UTF-8"));
            List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarationList = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
            //判断是否是enum
            if (classOrInterfaceDeclarationList.size() == 0) {
                List<EnumConstantDeclaration> enumConstantDeclarationList = compilationUnit.findAll(EnumConstantDeclaration.class);
                if (enumConstantDeclarationList.size() != 0) {
                    return "enum";
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
                            return constructorDeclaration.getSignature().toString();
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
                            return methodDeclaration.getSignature().toString();
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
                            for (VariableDeclarator variableDeclarator: fieldDeclaration.getVariables()) {
                                simpleName.append(variableDeclarator.getName());
                                simpleName.append(" ");
                            }
                            return simpleName.toString();
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

    /**
     * 抽java文件中所有方法签名
     * @param file 文件路径
     * @return 所有方法签名
     */
    public static List<String> getAllMethodsInFile(String file){
        List<String> allMethodsInFile = new ArrayList<>();
        try{
            CompilationUnit compileUtil = JavaParser.parse(Paths.get(file), Charset.forName("UTF-8"));
            List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compileUtil.findAll(ClassOrInterfaceDeclaration.class);
            for(ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations){
                List<MethodDeclaration> methodDeclarations = classOrInterfaceDeclaration.findAll(MethodDeclaration.class);
                for(MethodDeclaration methodDeclaration : methodDeclarations){
                    String method = methodDeclaration.getSignature().toString();
                    allMethodsInFile.add(method);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return allMethodsInFile;
    }

    /**
     * 抽一个java文件中所有成员变量名
     * @param file 文件路径
     * @return 成员变量list列表
     */
    public static List<String> getAllFieldsInFile(String file){
        List<String> allFieldsInFile = new ArrayList<>();
        try {
            CompilationUnit compileUtil = JavaParser.parse(Paths.get(file), Charset.forName("UTF-8"));
            List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = compileUtil.findAll(ClassOrInterfaceDeclaration.class);
            for(ClassOrInterfaceDeclaration classOrInterfaceDeclaration : classOrInterfaceDeclarations){
                List<FieldDeclaration> fieldDeclarations = classOrInterfaceDeclaration.findAll(FieldDeclaration.class);
                for(FieldDeclaration fieldDeclaration : fieldDeclarations){
                    NodeList<VariableDeclarator> variables = fieldDeclaration.getVariables();
                    for(VariableDeclarator variable : variables){
                        allFieldsInFile.add(variable.getName().toString());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return allFieldsInFile;
    }

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Beethoven\\Desktop\\IssueTracker-Master\\issue-service\\src\\main\\java\\cn\\edu\\fudan\\issueservice\\util\\SegmentationUtil.java";
        Set<String> allMethodAndFieldName = getAllMethodAndFieldName(filePath);
        for(String s : allMethodAndFieldName){
            System.out.println(s);
        }
    }
}
