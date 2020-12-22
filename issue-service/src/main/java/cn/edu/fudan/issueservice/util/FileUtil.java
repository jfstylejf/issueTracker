package cn.edu.fudan.issueservice.util;

/**
 * @author Beethoven
 */
public class FileUtil {

    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");

    public static String getEsLintReportAbsolutePath(String resultFileHome, String repoUuid, String commit){
        return IS_WINDOWS ? resultFileHome + "\\eslint-report_" + repoUuid + "_" + commit + ".json"
                : resultFileHome + "/.eslint-report_" + repoUuid + "_" + commit + ".json";
    }

    public static String handleFileNameToRelativePath(String filePath) {
        String[] paths = filePath.split("duplicate_fdse");
        return paths[paths.length - 1].substring(paths[paths.length - 1].indexOf('/') + 1);
    }

    public static String getCode(String codeSource, int line, int endLine) {
        String[] code = codeSource.split("\\r\\n");
        StringBuilder result = new StringBuilder();
        for(int i = line - 1; i < endLine; i++){
            if(i != line - 1){
                result.append("\\r\\n");
            }
            result.append(code[i]);
        }
        return result.toString();
    }
}