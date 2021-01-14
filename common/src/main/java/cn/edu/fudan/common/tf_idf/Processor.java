package cn.edu.fudan.common.tf_idf;

import cn.edu.fudan.common.domain.FileWordInfo;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author wjzho
 */
@Component
public interface Processor {

    /**
     * TODO 对文本分词(中文)
     * @param file 文件路径
     * @return
     * @throws FileNotFoundException
     */
    default Map<String, Integer> getChineseWordList(File file) throws IOException {
        return null;
    }

    /**
     * 获取java文件的词信息
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    default FileWordInfo getFileEnglishWordList(File file) throws IOException {
        Map<String,Integer> wordListMap = new LinkedHashMap<>();
        List<String> commonWord = Arrays.asList("String","public","static","private","final","class","");
        int sum = 0;
        FileInputStream fileInputStream = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(fileInputStream,StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(isr);
        String line ;
        try {
            while ((line = bufferedReader.readLine())!=null) {
                line = line.trim();
                // 过滤空行
                if(line.length() == 0) {
                    continue;
                }
                // 过滤注释行，引包行
                if(line.startsWith("package") || line.startsWith("import") || line.startsWith("//") || line.startsWith("/*")  || line.startsWith("*") || line.endsWith("*/" ) ) {
                    continue;
                }
                // 过滤标点符号除 - _
                String regex = "[\\p{P} && [^-_]]";
                line = line.replaceAll(regex," ");
                // 过滤 $ = + - < > 数字
                // fixme 数字处理存在问题 包含在字符串中的不应该删除
                String reg = "[\\$\\+\\-\\<\\>\\=\\d+]";
                line = line.replaceAll(reg,"");

                List<String> list = Arrays.asList(line.split(" "));
                for(String word : list) {
                    if(commonWord.contains(word)) {
                        continue;
                    }
                    if(wordListMap.get(word)==null) {
                        wordListMap.put(word,0);
                    }
                    wordListMap.put(word,wordListMap.get(word)+1);
                    sum++;
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }finally {
            bufferedReader.close();
        }
        return new FileWordInfo(file,wordListMap,sum);
    }


}
