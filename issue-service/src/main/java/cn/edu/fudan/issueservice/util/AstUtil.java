package cn.edu.fudan.issueservice.util;

import java.io.*;

/**
 * @author Beethoven
 */
public class AstUtil {

    public static String getCode(int startLine, int endLine, String filePath) {
        StringBuilder code = new StringBuilder();
        String s = "";
        int line = 1;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            while ((s = bufferedReader.readLine()) != null) {
                if (line >= startLine && line <= endLine) {
                    code.append(s);
                    code.append("\n");
                }
                line++;
                if (line > endLine) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return code.toString();
    }

    public static int getCodeLines(String filePath) {
        int result = 0;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath))) {
            while (bufferedReader.readLine() != null) {
                result++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
