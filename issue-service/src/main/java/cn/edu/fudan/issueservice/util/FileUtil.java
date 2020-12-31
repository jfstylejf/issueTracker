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

    public static String getEsLintReportAbsolutePath(String resultFileHome, String repoUuid, String commit){
        return IS_WINDOWS ? resultFileHome + "\\eslint-report_" + repoUuid + "_" + commit + ".json"
                : resultFileHome + "/eslint-report_" + repoUuid + "_" + commit + ".json";
    }

    public static String getEsLintAstReportAbsolutePath(String resultFileHome, String repoUuid) {
        return IS_WINDOWS ? resultFileHome + "\\ast-report" + repoUuid + ".json"
                : resultFileHome + "/ast-report" + repoUuid + ".json";
    }

    public static String handleFileNameToRelativePath(String filePath) {
        String[] paths = filePath.split("duplicate_fdse");
        return paths[paths.length - 1].substring(paths[paths.length - 1].indexOf('/') + 1);
    }

    public static String getCode(String filePath, int line, int endLine, int startToken, int endToken) {
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
                        s = s.substring(Math.max(startToken - 2, 0), Math.min(endToken - 1, s.length()));
                    }else {
                        if (index == line) {
                            s = s.substring(Math.max(startToken - 2, 0));
                        } else if (index == endLine) {
                            s = s.substring(0, Math.min(endToken - 1, s.length()));
                        }
                    }
                    code.append(s);
                }else if(index > endLine){
                    break;
                }
            }
            return code.toString();
        }catch (IOException e){
            log.error("get code source failed ! file is ---> {}", filePath);
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

    public static void main(String[] args) {
        System.out.println(getCode("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web\\test.js", 36, 36,9, 11));
        // 获取文件的内容的总行数
        System.out.println(getTotalLines(new File("C:\\Users\\Beethoven\\Desktop\\issue-tracker-web\\test.js")));
    }
}