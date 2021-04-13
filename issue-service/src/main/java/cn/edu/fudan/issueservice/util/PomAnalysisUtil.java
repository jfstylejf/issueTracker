package cn.edu.fudan.issueservice.util;

import cn.edu.fudan.issueservice.domain.enums.CompileTool;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * description: 对java pom 文件的解析
 *
 * @author fancying
 * create: 2020-06-18 15:45
 **/
@Slf4j
public class PomAnalysisUtil {


    /**
     * 得到某个主要的pom文件  解决重复编译的问题
     *
     * @param pomPaths pom.xml 的绝对地址
     * @return 过滤后的pom文件
     */
    @SneakyThrows
    public static List<String> getMainPom(List<String> pomPaths) {
        if (pomPaths == null || pomPaths.size() < 2) {
            return pomPaths;
        }

        Map<String, String> names = new HashMap<>(pomPaths.size() << 1);
        Set<String> modules = new HashSet<>(pomPaths.size() << 1);
        for (String pomPath : pomPaths) {
            if (!pomPath.endsWith(CompileTool.MAVEN.compileFile())) {
                continue;
            }
            try (FileInputStream fis = new FileInputStream(new File(pomPath))) {
                //pom 为 pom.xml 路径
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(fis);
                modules.addAll(model.getModules());
                names.put(model.getName(), pomPath);
            } catch (Exception e) {
                log.error("analyzed pom failed！");
                log.error(e.getMessage());
            }
        }
        List<String> r = new ArrayList<>(4);
        names.keySet().stream().filter(n -> !modules.contains(n)).forEach(n -> r.add(names.get(n)));
        return r;
    }
}