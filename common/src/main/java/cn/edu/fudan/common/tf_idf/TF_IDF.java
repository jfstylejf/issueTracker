package cn.edu.fudan.common.tf_idf;

import cn.edu.fudan.common.domain.FileWordInfo;
import cn.edu.fudan.common.domain.Word_TF_IDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.*;

/**
 * @author wjzho
 */
@Component
public class TF_IDF implements Processor{

    private static TF_IDF tf_idf = new TF_IDF();

    public static void main(String[] args) throws IOException {
        File srcFile = new File("C:\\work");
        File targetFile = new File("C:\\work\\IssueTracker-Master\\measure-service\\src\\main\\java\\cn\\edu\\fudan\\measureservice\\config\\Swagger.java");
        try {
            List<Word_TF_IDF> wordTfIdfList = tf_idf.getFileWordFrequencyList(srcFile,targetFile);
            System.out.println(wordTfIdfList);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Word_TF_IDF> getFileWordFrequencyList(File src,File target) {
        List<Word_TF_IDF> wordTfIdfList = new ArrayList<>();
        try {
            Map<File,FileWordInfo> fileWordInfos = getFileWordInfoMap(src);
            FileWordInfo targetFileWordInfo = fileWordInfos.get(target);
            for (String word : targetFileWordInfo.getFileWordList().keySet()) {
                double termFrequency = targetFileWordInfo.getFileWordList().get(word) * 1.0 / targetFileWordInfo.getTotalWordNum();
                double inverseDocumentFrequency = Math.log( fileWordInfos.size() * 1.0 / (getWordContainFileNumber(word,fileWordInfos) + 1 ));
                Word_TF_IDF wordTfIdf = Word_TF_IDF.builder().word(word).inverseDocumentFrequency(inverseDocumentFrequency).termFrequency(termFrequency).build();
                wordTfIdf.cal();
                wordTfIdfList.add(wordTfIdf);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        if (wordTfIdfList.size() > 0) {
            Collections.sort(wordTfIdfList, new Comparator<Word_TF_IDF>() {
                @Override
                public int compare(Word_TF_IDF o1, Word_TF_IDF o2) {
                    Double value1 = o1.getWeight();
                    Double value2 = o2.getWeight();
                    return value2.compareTo(value1);
                }
            });
        }
        return wordTfIdfList;
    }

    /**
     *
     * @param word
     * @param fileWordInfos
     * @return
     */
    private int getWordContainFileNumber(String word , Map<File,FileWordInfo> fileWordInfos) {
        int sum = 0;
        for (Map.Entry<File,FileWordInfo> entry : fileWordInfos.entrySet()) {
            FileWordInfo fileWordInfo = entry.getValue();
            if(fileWordInfo.getFileWordList().containsKey(word)) {
                sum++;
            }
        }
        return sum;
    }

    /**
     * 获取文件目录下所有java文件词信息
     * @param srcFile
     * @return
     * @throws IOException
     */
    private Map<File,FileWordInfo> getFileWordInfoMap(File srcFile) throws IOException {
        Map<File,FileWordInfo> fileWordInfos = new HashMap<>();
        List<File> javaFileList = getJavaFileList(srcFile);
        for (File file : javaFileList) {
            fileWordInfos.put(file,tf_idf.getFileEnglishWordList(file));
        }
        return fileWordInfos;
    }


    /**
     * 获取文件目录下的java文件数
     * @param file
     * @return
     */
    private List<File> getJavaFileList(File file) {
        if (file.getName().endsWith(".java")) {
            return Collections.singletonList(file);
        }
        List<File> javaFileList = new ArrayList<>();
        Queue<File> queue = new LinkedList<>();
        queue.add(file);
        while (queue.size()>0) {
            File srcFile = queue.remove();
            File[] files = srcFile.listFiles();
            if (files!=null) {
                queue.addAll(Arrays.asList(files));
            }
            if (srcFile.getName().endsWith(".java")) {
                javaFileList.add(srcFile);
            }
        }
        return javaFileList;
    }

}
