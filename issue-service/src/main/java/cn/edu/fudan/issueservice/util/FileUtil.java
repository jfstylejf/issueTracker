package cn.edu.fudan.issueservice.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

/**
 * @author Beethoven
 */
@Slf4j
public class FileUtil {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private static final String JSON_STR = ".json", CODE_SOURCE_ERROR_MESSAGE = "get code source failed ! file is ---> {}";

    public static String getEsLintReportAbsolutePath(String resultFileHome, String repoUuid, String commit){
        return IS_WINDOWS ? resultFileHome + "\\eslint-report_" + repoUuid + "_" + commit + JSON_STR
                : resultFileHome + "/eslint-report_" + repoUuid + "_" + commit + JSON_STR;
    }

    public static String getEsLintAstReportAbsolutePath(String resultFileHome, String fileName) {
        return IS_WINDOWS ? resultFileHome + "\\ast-report" + fileName + JSON_STR
                : resultFileHome + "/ast-report" + fileName + JSON_STR;
    }

    public static String handleFileNameToRelativePath(String filePath) {
        String[] paths = filePath.split("duplicate_fdse");
        return paths[paths.length - 1].substring(paths[paths.length - 1].indexOf('/') + 1);
    }

    public static String getCode(String filePath, int line, int endLine) {
        File codeFile = new File(filePath);
        //code line limit
        if (line <= 0 || endLine > getTotalLines(codeFile)) {
            log.error("code line error,begin line is {},endLine is {}, code total line is {} !", line, endLine, getTotalLines(codeFile));
        }
        //get code
        try (LineNumberReader reader = new LineNumberReader(new FileReader(codeFile))){
            StringBuilder code = new StringBuilder();
            int index = 0;
            while (true) {
                index++;
                String s = reader.readLine();
                if(index >= line && index <= endLine){
                    code.append(s);
                }else if(index > endLine){
                    break;
                }
            }
            return code.toString().replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*\\/", "");
        }catch (IOException e){
            log.error(CODE_SOURCE_ERROR_MESSAGE, filePath);
            return null;
        }
    }

    private static int getTotalLines(File file) {
        try (LineNumberReader reader = new LineNumberReader(new FileReader(file))) {
            String s = reader.readLine();
            int lines = 0;
            while (s != null) {
                lines++;
                s = reader.readLine();
            }
            return lines;
        }catch (IOException e){
            log.error("get total line failed !");
            return 0;
        }
    }

    public static String getForStatementCode(String codePath, int line, int endLine) {
        try (LineNumberReader reader = new LineNumberReader(new FileReader(codePath))){
            StringBuilder code = new StringBuilder();
            int index = 0;
            while (true) {
                index++;
                String s = reader.readLine();
                if(index >= line && index <= endLine){
                    code.append(s);
                }else if(index > endLine){
                    break;
                }
            }
            return code.substring(0, code.indexOf(")") + 1);
        }catch (IOException e){
            log.error(CODE_SOURCE_ERROR_MESSAGE, codePath);
            return null;
        }
    }

    public static String getCode(String filePath, int line, int endLine, int beginColumn, int endColumn) {
        File codeFile = new File(filePath);
        //code line limit
        if (line <= 0 || endLine > getTotalLines(codeFile)) {
            log.error("code line error,begin line is {},endLine is {}, code total line is {} !", line, endLine, getTotalLines(codeFile));
        }
        //get code
        try (LineNumberReader reader = new LineNumberReader(new FileReader(codeFile))){
            StringBuilder code = new StringBuilder();
            int index = 0;
            while (true) {
                index++;
                String s = reader.readLine();
                if(index >= line && index <= endLine){
                    if(line == endLine){
                        s = s.substring(beginColumn, endColumn);
                    }else{
                        if(index == line){
                            s = s.substring(beginColumn);
                        }
                        if(index == endLine){
                            s = s.substring(0, endColumn);
                        }
                    }
                    code.append(s);
                }else if(index > endLine){
                    break;
                }
            }
            return code.toString().replaceAll("\\/\\/[^\\n]*|\\/\\*([^\\*^\\/]*|[\\*^\\/*]*|[^\\**\\/]*)*\\*\\/", "");
        }catch (IOException e){
            log.error(CODE_SOURCE_ERROR_MESSAGE, filePath);
            return null;
        }
    }

    public static void main(String[] args) {
        System.out.println(getCode("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web\\code.js", 10, 19, 1, 8));
        // 获取文件的内容的总行数
        System.out.println(getTotalLines(new File("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web\\test.js")));
    }
}