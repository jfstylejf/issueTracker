package cn.edu.fudan.dependservice.utill;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class WriteUtill2 {
    public static String befor="{\n" +
            "\t\"architectures\": {},\n" +
            "\t\"projects\": [";
    public static String c1="{\n" +
            "\t\t\"path\":\"";
    public static String c2="\",\n" +
            "\t\t\"includeDirs\": [],\n" +
            "\t\t\"microserviceName\": \"saic\",\n" +
            "\t\t\"autoInclude\": true,\n" +
            "\t\t\"serviceGroupName\": \"source-part\",\n" +
            "\t\t\"project\": \"scenario-engine\",\n" +
            "\t\t\"language\": \"java\",\n" +
            "\t\t\"isMicroservice\": true,\n" +
            "\t\t\"excludes\": [\n" +
            "\t\t]\n" +
            "\t}";


    public static String rest="],\n" +
            "\t\"dynamics\": {\n" +
            "\t\t\"file_suffixes\": [\n" +
            "\t\t\t\"\"\n" +
            "\t\t],\n" +
            "\t\t\"logs_path\": \"\",\n" +
            "\t\t\"features_path\": \"\"\n" +
            "\t},\n" +
            "\t\"gits\": [],\n" +
            "\t\"libs\": [],\n" +
            "\t\"clones\": []\n" +
            "}";



    public static boolean writeProjecConf(String ConfPath, List<String> repoPaths) {
        File f = new File(ConfPath);
        try {
            FileOutputStream fop = new FileOutputStream(f);
            OutputStreamWriter writer = new OutputStreamWriter(fop, "UTF-8");
            writer.append(getJsonString(repoPaths));
            writer.close();

            //关闭输出流，释放系统资源
            fop.close();
            return true;
        }catch ( FileNotFoundException fileNotFoundException){
            log.error("write file fail");

            fileNotFoundException.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return false;

    }
    public  static String  getJsonString(List<String> repoPaths){
        StringBuilder res=new StringBuilder();
        res.append(befor);

        for(int i=0;i<repoPaths.size();i++){
            res.append(c1);
            res.append(repoPaths.get(i));
            res.append(c2);
            if(i!=repoPaths.size()-1){
                res.append(",");

            }
        }
        res.append(rest);
        return res.toString();
    }

    public static void main(String[] args) {
        String confPath ="D:\\home\\contxt.txt";
        List<String> repoPaths=new ArrayList<>();
        repoPaths.add("path1");
        repoPaths.add("path2");
        repoPaths.add("path3");
        writeProjecConf(confPath,repoPaths);
    }

}
