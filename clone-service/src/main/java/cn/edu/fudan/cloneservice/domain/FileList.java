package cn.edu.fudan.cloneservice.domain;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class FileList {
    private String dirName = null;
    Vector<String> ver = null;
    ArrayList<String> result = new ArrayList<>();

    public FileList(String dirName) throws IOException {
        this.dirName = dirName;    //文件夹地址
        ver = new Vector<String>();    //用做堆栈
    }

    public void getList(String language) {
        ver.add(dirName);
        while (!ver.isEmpty()) {
            //获取该文件夹下所有的文件(夹)名
            File[] files = new File(ver.get(0)).listFiles();
            ver.remove(0);

            for (File file : files) {
                String tmp = file.getAbsolutePath();
                //如果是目录，则加入队列。以便进行后续处理
                if (file.isDirectory())
                    ver.add(tmp);
                else {
                    switch (language) {
                        case "java":
                            if (tmp.contains(".java")) {
                                result.add(tmp);//如果是文件，则直接输出文件名到指定的文件。
                            }
                            break;
                        case "js":
                            if (tmp.contains(".js")) {
                                result.add(tmp);
                            }
                            break;
                        default:
                            result.add(tmp);
                    }
                }
            }
        }
    }
}