package cn.edu.fudan.issueservice.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author fancying
 */
@Slf4j
public class AstParserUtil {

    private final static String LOC = "loc", START = "start", END = "end", LINE = "line", BODY = "body", DECLARATIONS = "declarations",
            TYPE = "type", ID = "id", NAME = "name", PARAMS = "params", KEY = "key", VALUE = "value", KIND = "kind", SUPER_CLASS = "superClass",
            CLASS = "class", EXTENDS = "extends", OBJECT = "object", PROPERTY = "property";

    private final static String FUNCTION_DECLARATION = "FunctionDeclaration", VARIABLE_DECLARATION = "VariableDeclaration",
            IMPORT_DECLARATION = "ImportDeclaration", CLASS_DECLARATION = "ClassDeclaration", EXPORT_DEFAULT_DECLARATION = "ExportDefaultDeclaration",
            METHOD_DEFINITION = "MethodDefinition", EXPRESSION_STATEMENT = "ExpressionStatement";

    private final static  String CLASS_PROPERTY = "ClassProperty";

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

    public static JSONObject parseJsCode(String binHome, String codePath, String resultFileHome, String repoUuid) {
        //step 1. invoke script to analyze AST
        try {
            Runtime rt = Runtime.getRuntime();
            //run babelEsLint script
            String command = binHome + "babelEsLint.sh " + codePath + " " + repoUuid;
            log.info("command -> {}",command);
            Process process = rt.exec(command);
            boolean timeout = process.waitFor(100L, TimeUnit.SECONDS);
            if (!timeout) {
                process.destroy();
                log.error("run babelEsLint script timeout ! (100s)");
                return null;
            }
            //step 2. read file parse ast tree to json
            JSONObject nodeJsCode = readJsParseFile(resultFileHome, repoUuid);
            log.info("analyze AST success !");
            //step 3. delete file
            deleteNodeJsCodeFile(binHome, repoUuid);
            return nodeJsCode;
        } catch (Exception e) {
            log.error("invoke babelEsLint script failed !");
        }
        return null;
    }

    private static void deleteNodeJsCodeFile(String binHome, String repoUuid) throws Exception {
        Runtime rt = Runtime.getRuntime();
        String command = binHome + "deleteAstFile.sh " + repoUuid;
        log.info("command -> {}",command);
        Process process = rt.exec(command);
        boolean timeout = process.waitFor(20L, TimeUnit.SECONDS);
        if (!timeout) {
            process.destroy();
            log.error("delete AST file timeout ! (20s)");
            return;
        }
        log.info("delete AST file success !");
    }

    private static JSONObject readJsParseFile(String resultFileHome, String repoUuid) {
        try (BufferedReader reader = new BufferedReader(new FileReader(FileUtil.getEsLintAstReportAbsolutePath(resultFileHome, repoUuid)))) {
            int ch;
            char[] buf = new char[1024];
            StringBuilder data = new StringBuilder();
            while ((ch = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, ch);
                data.append(readData);
            }
            log.info("read AST file success !");
            return JSONObject.parseObject(data.toString());
        } catch (Exception e) {
            log.error("read AST file failed !");
        }
        return null;
    }

    public static String getJsClass(JSONObject nodeJsCode, int beginLine, int endLine){
        //todo parse json ---> loc to find class

        return null;
    }

    public static String getJsMethod(JSONObject nodeJsCode, int beginLine, int endLine, String filePath) {
        //parse json ---> loc to find Function,Import,Variable or Export.
        for(Object nodeJsCodeBody : nodeJsCode.getJSONArray(BODY)){
            JSONObject declaration = (JSONObject)nodeJsCodeBody;
            int declarationBeginLine = declaration.getJSONObject(LOC).getJSONObject(START).getIntValue(LINE);
            int declarationEndLine = declaration.getJSONObject(LOC).getJSONObject(END).getIntValue(LINE);
            if(declarationBeginLine <= beginLine && declarationEndLine >= endLine){
                //handle different condition
                switch (declaration.getString(TYPE)){
                    case CLASS_PROPERTY:
                        return handleClassProperty(declaration);
                    case VARIABLE_DECLARATION:
                        return handleVariableDeclaration(declaration);
                    case METHOD_DEFINITION:
                        return handleMethodDefinition(declaration);
                    case FUNCTION_DECLARATION:
                        return handleFunctionDeclaration(declaration);
                    case CLASS_DECLARATION:
                        return beginLine == declarationBeginLine && endLine == declarationEndLine ?
                                handleClassName(declaration):
                                handleClassDeclaration(declaration.getJSONObject(BODY), beginLine, endLine, filePath);
                    case IMPORT_DECLARATION:
                    case EXPORT_DEFAULT_DECLARATION:
                    case EXPRESSION_STATEMENT:
                        return FileUtil.getCode(filePath, declarationBeginLine, declarationEndLine);
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    private static String handleVariableDeclaration(JSONObject declaration) {
        //fixme js语法过于复杂,这里只取变量名,有函数定义为变量的情况会不会影响匹配待审核。
        StringBuilder variableName = new StringBuilder();
        variableName.append(declaration.getString(KIND)).append(" ");
        JSONArray paramsDetail = declaration.getJSONArray(DECLARATIONS);
        if(paramsDetail != null) {
            for (int i = 0; i < paramsDetail.size(); i++) {
                if (i != 0) {
                    variableName.append(",");
                }
                JSONObject param = paramsDetail.getJSONObject(i);
                variableName.append(param.getJSONObject(ID).getString(NAME));
            }
        }
        return variableName.toString();
    }

    private static String handleMethodDefinition(JSONObject declaration) {
        StringBuilder methodName = new StringBuilder();
        methodName.append(declaration.getJSONObject(KEY).getString(NAME)).append("(");
        //get params
        JSONArray paramsDetail = declaration.getJSONObject(VALUE).getJSONArray(PARAMS);
        if(paramsDetail != null) {
            for (int i = 0; i < paramsDetail.size(); i++) {
                if (i != 0) {
                    methodName.append(",");
                }
                JSONObject paramDetail = (JSONObject) paramsDetail.get(i);
                methodName.append(paramDetail.getString(NAME));
            }
        }
        return methodName.append(")").toString();
    }

    private static String handleFunctionDeclaration(JSONObject declaration) {
        StringBuilder functionName = new StringBuilder();
        functionName.append(declaration.getJSONObject(ID).getString(NAME)).append("(");
        //get params
        JSONArray paramsDetail = declaration.getJSONArray(PARAMS);
        if(paramsDetail != null) {
            for (int i = 0; i < paramsDetail.size(); i++) {
                if (i != 0) {
                    functionName.append(",");
                }
                JSONObject paramDetail = paramsDetail.getJSONObject(i);
                functionName.append(paramDetail.getString(NAME));
            }
        }
        return functionName.append(")").toString();
    }

    private static String handleClassName(JSONObject declaration) {
        StringBuilder className = new StringBuilder();
        className.append(CLASS).append(" ").append(declaration.getJSONObject(ID).getString(NAME));
        JSONObject superClass = declaration.getJSONObject(SUPER_CLASS);
        if(superClass != null){
            className.append(EXTENDS).append(" ").append(className.append(superClass.getJSONObject(OBJECT).getString(NAME)));
            className.append(superClass.getJSONObject(PROPERTY) == null ? "" : "." + superClass.getJSONObject(PROPERTY).getString(NAME));
        }
        return className.toString();
    }

    private static String handleClassDeclaration(JSONObject declaration, int beginLine, int endLine, String filePath) {
        JSONArray declarationBody = declaration.getJSONArray(BODY);
        for(Object nodeDetail : declarationBody){
            JSONObject node = (JSONObject) nodeDetail;
            int nodeBeginLine = node.getJSONObject(LOC).getJSONObject(START).getIntValue(LINE);
            int nodeEndLine = node.getJSONObject(LOC).getJSONObject(END).getIntValue(LINE);
            if(nodeBeginLine <= beginLine && nodeEndLine >= endLine){
                //handle different condition
                switch (node.getString(TYPE)){
                    case CLASS_PROPERTY:
                        return handleClassProperty(node);
                    case VARIABLE_DECLARATION:
                        return handleVariableDeclaration(node);
                    case METHOD_DEFINITION:
                        return handleMethodDefinition(node);
                    case FUNCTION_DECLARATION:
                        return handleFunctionDeclaration(node);
                    case CLASS_DECLARATION:
                        return handleClassDeclaration(node, beginLine, endLine, filePath);
                    case IMPORT_DECLARATION:
                    case EXPORT_DEFAULT_DECLARATION:
                    case EXPRESSION_STATEMENT:
                        return FileUtil.getCode(filePath, nodeBeginLine, nodeEndLine);
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    private static String handleClassProperty(JSONObject node) {
        StringBuilder methodName = new StringBuilder();
        methodName.append(node.getJSONObject(KEY).getString(NAME)).append("(");
        JSONArray paramsDetail = node.getJSONArray(PARAMS);
        if(paramsDetail != null) {
            for (int i = 0; i < paramsDetail.size(); i++) {
                if (i != 0) {
                    methodName.append(",");
                }
                JSONObject param = paramsDetail.getJSONObject(i);
                methodName.append(param.getString(NAME));
            }
        }
        return methodName.append(")").toString();
    }

    public static void main(String[] args) {
        JSONObject ast = readJsParseFile("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web", "2");
        assert ast != null;
        String jsMethod = getJsMethod(ast, 56, 58, "import { Table, Tooltip } from 'antd';");
        System.out.println(jsMethod);
    }
}
