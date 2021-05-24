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

    private static final String SRC = "src";

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    private static final String JSON_STR = ".json", CODE_SOURCE_ERROR_MESSAGE = "get code source failed ! file is ---> {}";

    public static String getEsLintReportAbsolutePath(String resultFileHome, String repoUuid, String commit) {
        return IS_WINDOWS ? resultFileHome + "\\eslint-report_" + repoUuid + "_" + commit + JSON_STR
                : resultFileHome + "/eslint-report_" + repoUuid + "_" + commit + JSON_STR;
    }

    public static String handleFileNameToRelativePath(String filePath) {
        String[] paths = filePath.split("duplicate_fdse");
        return paths[paths.length - 1].substring(paths[paths.length - 1].indexOf('/') + 1);
    }

    public static String getCode(String filePath, int line, int endLine) {
        File codeFile = new File(filePath);
        //code line limit
        if (line < 0 || endLine > getTotalLines(codeFile)) {
            log.error("code line error,begin line is {},endLine is {}, code total line is {} !", line, endLine, getTotalLines(codeFile));
        }
        //get code
        try (LineNumberReader reader = new LineNumberReader(new FileReader(codeFile))) {
            StringBuilder code = new StringBuilder();
            int index = line == 0 ? -1 : 0;
            while (true) {
                index++;
                String s = reader.readLine();
                if (index >= line && index <= endLine) {
                    code.append(s);
                } else if (index > endLine) {
                    break;
                }
            }
            return StringsUtil.removeBr(code.toString());
        } catch (IOException e) {
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
        } catch (IOException e) {
            log.error("get total line failed !");
            return 0;
        }
    }

    public static String getEsLintRunningLogAbsolutePath(String resultFileHome, String repoUuid, String commit) {
        return IS_WINDOWS ? resultFileHome + "\\eslint-running-" + repoUuid + "_" + commit + ".log"
                : resultFileHome + "/eslint-running-" + repoUuid + "_" + commit + ".log";
    }

    public static String findSrcDir(String repoPath) {
        File repo = new File(repoPath);
        File[] files = repo.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && containSrc(file.getAbsolutePath())) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }

    private static boolean containSrc(String absolutePath) {
        return absolutePath.contains("/src");
    }

    public static void main(String[] args) {
        String srcDir = findSrcDir("/Users/beethoven/Desktop/saic/IssueTracker-Master/issue-service");
        System.out.println(srcDir);
    }
}
