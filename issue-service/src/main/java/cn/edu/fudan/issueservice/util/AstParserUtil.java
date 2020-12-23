package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.exception.ParseFileException;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.FileReader;
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

    private final static String loc = "loc", start = "start", end = "end", line = "line", body = "body",
            type = "type", id = "id", name = "name", params = "params", key = "key", value = "value";

    private final static String Program = "Program", FunctionDeclaration = "FunctionDeclaration", VariableDeclaration = "VariableDeclaration",
            ImportDeclaration = "ImportDeclaration", ClassDeclaration = "ClassDeclaration", ExportDefaultDeclaration = "ExportDefaultDeclaration",
            MethodDefinition = "MethodDefinition";

    public static String findMethod(String filePath, int beginLine, int endLine) {
        try {
            CompilationUnit compilationUnit = JavaParser.parse(Paths.get(filePath), StandardCharsets.UTF_8);
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

    public static Set<String> getAllMethodAndFieldName(String filePath) {
        Set<String> methodsAndFields = new HashSet<>();

        List<String> allFieldsInFile = getAllFieldsInFile(filePath);
        List<String> allMethodsInFile = getAllMethodsInFile(filePath);

        if(allFieldsInFile != null){
            methodsAndFields.addAll(allFieldsInFile);
        }

        if(allMethodsInFile != null) {
            methodsAndFields.addAll(allMethodsInFile);
        }

        return methodsAndFields;
    }

    /**
     * 抽java文件中所有方法签名
     * @param file 文件路径
     * @return 所有方法签名
     */
    public static List<String> getAllMethodsInFile(String file){
        List<String> allMethodsInFile = new ArrayList<>();
        try{
            CompilationUnit compileUtil = JavaParser.parse(Paths.get(file), StandardCharsets.UTF_8);
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
            CompilationUnit compileUtil = JavaParser.parse(Paths.get(file), StandardCharsets.UTF_8);
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

    public static JSONObject parseJsCode(String codeSource, String resultFileHome) throws ParseFileException {
        //todo step 1. pass codeSource to script and invoke script to analyze ast tree , if can't get result throws ParseFileException
        invokeEspreeScript();
        //step 2. read file parse ast tree to json
        JSONObject ast = readJsParseFile(resultFileHome);
        //todo step 3. delete file
        deleteAstFile();
        return ast;
    }

    private static void invokeEspreeScript() {
        
    }

    private static JSONObject readJsParseFile(String resultFileHome) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FileUtil.getEsLintAstReportAbsolutePath(resultFileHome)))) {
            int ch;
            char[] buf = new char[1024];
            StringBuilder data = new StringBuilder();
            while ((ch = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, ch);
                data.append(readData);
            }
            return JSONObject.parseObject(data.toString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("read ast failed !");
        }
        return null;
    }

    private static void deleteAstFile() {

    }

    public static String getJsClass(JSONObject nodeJsCode, int beginLine, int endLine){
        //todo parse json ---> loc to find class

        return null;
    }

    public static String getJsMethod(JSONObject nodeJsCode, int beginLine, int endLine, String code) {
        //parse json ---> loc to find Function,Import,Variable or Export.
        for(Object nodeJsCodeBody : nodeJsCode.getJSONArray(body)){
            JSONObject declaration = (JSONObject)nodeJsCodeBody;
            if(declaration.getJSONObject(loc).getJSONObject(start).getIntValue(line) <= beginLine &&
                    declaration.getJSONObject(loc).getJSONObject(end).getIntValue(line) >= endLine){
                //handle different condition
                switch (declaration.getString(type)){
                    case MethodDefinition:
                        return handleMethodDefinition(declaration);
                    case FunctionDeclaration:
                        return handleFunctionDeclaration(declaration);
                    case ClassDeclaration:
                        return handleClassDeclaration(declaration.getJSONObject(body), beginLine, endLine, code);
                    case ImportDeclaration:
                    case VariableDeclaration:
                    case ExportDefaultDeclaration:
                        return code;
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    private static String handleMethodDefinition(JSONObject declaration) {
        StringBuilder methodName = new StringBuilder();
        methodName.append(declaration.getJSONObject(key).getString(name)).append("(");
        //get params
        JSONArray paramsDetail = declaration.getJSONObject(value).getJSONArray(params);
        for(int i = 0; i < paramsDetail.size(); i++){
            if(i != 0){
                methodName.append(",");
            }
            JSONObject paramDetail = (JSONObject) paramsDetail.get(i);
            methodName.append(paramDetail.getString(name));
        }
        return methodName.append(")").toString();
    }

    private static String handleFunctionDeclaration(JSONObject declaration) {
        StringBuilder functionName = new StringBuilder();
        functionName.append(declaration.getJSONObject(id).getString(name)).append("(");
        //get params
        JSONArray paramsDetail = declaration.getJSONArray(params);
        for(int i = 0; i < paramsDetail.size(); i++){
            if(i != 0){
                functionName.append(",");
            }
            JSONObject paramDetail = (JSONObject) paramsDetail.get(i);
            functionName.append(paramDetail.getString(name));
        }
        return functionName.append(")").toString();
    }

    private static String handleClassDeclaration(JSONObject declaration, int beginLine, int endLine, String code) {
        JSONArray declarationBody = declaration.getJSONArray(body);
        for(Object nodeDetail : declarationBody){
            JSONObject node = (JSONObject) nodeDetail;
            if(node.getJSONObject(loc).getJSONObject(start).getIntValue(line) <= beginLine &&
                    node.getJSONObject(loc).getJSONObject(end).getIntValue(line) >= endLine){
                //handle different condition
                switch (node.getString(type)){
                    case MethodDefinition:
                        return handleMethodDefinition(node);
                    case FunctionDeclaration:
                        return handleFunctionDeclaration(node);
                    case ClassDeclaration:
                        return handleClassDeclaration(node, beginLine, endLine, code);
                    case ImportDeclaration:
                    case VariableDeclaration:
                    case ExportDefaultDeclaration:
                        return code;
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        JSONObject ast = readJsParseFile("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web");
        String jsMethod = getJsMethod(ast, 9, 9, "var forge = require('node-forge');");
        System.out.println(jsMethod);
    }
}
