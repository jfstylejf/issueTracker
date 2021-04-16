package cn.edu.fudan.dependservice.utill;

import cn.edu.fudan.dependservice.config.ScanConfig;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class WriteUtill {
    public static String projectConf1="{\n" +
            "\t\"architectures\": {},\n" +
            "\t\"projects\": [{\n" +
            "\t\t\"path\":\"";
    public  static String projectConf2 ="\",\n" +
            "\t\t\"includeDirs\": [],\n" +
            "\t\t\"microserviceName\": \"saic\",\n" +
            "\t\t\"autoInclude\": true,\n" +
            "\t\t\"serviceGroupName\": \"source-part\",\n" +
            "\t\t\"project\": \"scenario-engine\",\n" +
            "\t\t\"language\": \"java\",\n" +
            "\t\t\"isMicroservice\": true,\n" +
            "\t\t\"excludes\": [\n" +
            "\t\t]\n" +
            "\t}],\n" +
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

    public static boolean writeProjecConf(String ConfPath,String repoPath) {
        File f = new File(ConfPath);
        try {
            FileOutputStream fop = new FileOutputStream(f);
            OutputStreamWriter writer = new OutputStreamWriter(fop, "UTF-8");
            writer.append(getJsonString(repoPath));
            writer.close();

            //关闭输出流，释放系统资源
            fop.close();
        }catch ( FileNotFoundException fileNotFoundException){
            log.error("write file fail");

            fileNotFoundException.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;

    }
    public  static String  getJsonString(String repoPath){
        String res = projectConf1 +repoPath+projectConf2;

        return res;

    }


}
