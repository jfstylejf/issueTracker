package cn.edu.fudan.measureservice.analyzer;

import cn.edu.fudan.measureservice.domain.*;
import cn.edu.fudan.measureservice.domain.Objects;
import cn.edu.fudan.measureservice.domain.Package;
import cn.edu.fudan.measureservice.util.DirExplorer;
import cn.edu.fudan.measureservice.util.FileFilter;
import cn.edu.fudan.measureservice.util.FileUtil;
import cn.edu.fudan.measureservice.util.JavaFileFilter;
import javancss.FunctionMetric;
import javancss.Javancss;
import javancss.PackageMetric;

import java.io.File;
import java.util.*;

/**
 * description: ncss 调用并得到目标文件
 *
 * @author fancying
 * create: 2020-06-10 20:26
 **/
public class JavaNcss {

    /**
     * 得到某个地址下面所有文件的圈复杂度
     *
     * @param repoPath  目录地址
     * @return key 文件的相对地址 value 文件的圈复杂度
     */
    public static Map<String, Integer> getFileCcn(String repoPath) {
        File projectDir = new File(repoPath);
        FileFilter fileFilter = new JavaFileFilter();
        List<String> pathList = new ArrayList<>();
        Map<String, Integer> result = new LinkedHashMap<>(128);
        Map<String, String> relativePath = new LinkedHashMap<>(128);
        new DirExplorer((level, path, file) -> !fileFilter.fileFilter(path),
                (level, path, file) -> {
                    pathList.add(file.getAbsolutePath());
                    relativePath.put(file.getAbsolutePath(), path);
                } ).explore(projectDir);

        for (String path : pathList) {
            int ccn = getOneFileCcn(path);
            result.put(relativePath.get(path), ccn);
        }
        return result;
    }

    /**
     * 得到指定文件（list）的圈复杂度
     *
     * @param filePathList  文件的绝对地址
     * @param preFix 代码库地址前缀  getRepoPath 得到的目录地址
     * @return key 文件的相对地址 value 文件的圈复杂度
     */
    public static Map<String, Integer> getFileCcn(List<String> filePathList, String preFix) {
        Map<String, Integer> result = new LinkedHashMap<>(128);
        for (String path : filePathList) {
            // 构造相对地址
            String relativePath = path.replace(preFix, "");
            int ccn = getOneFileCcn(path);
            result.put(relativePath, ccn);
        }
        return result;
    }

    /**
     * 得到某个文件的圈复杂度
     *
     * @param path  文件地址
     * @return 文件的圈复杂度
     */
    public static int getOneFileCcn(String path) {

        File tempFile = new File(path);
        Javancss javancss = new Javancss(tempFile);
        int ccn = 0;
        for (FunctionMetric functionMetric : javancss.getFunctionMetrics()) {
            ccn += functionMetric.ccn;
        }
        return ccn;
    }

    public static int getFileTotalLines(String path){
        File tempFile = new File(path);
        Javancss javancss = new Javancss(tempFile);
        return javancss.getNcss();
    }

    public static Measure analyse(String repoPath) {
        List<File> files = new ArrayList<>();
        FileFilter fileFilter = new JavaFileFilter();
        new DirExplorer((level, path, file) -> !fileFilter.fileFilter(path),
                (level, path, file) -> files.add(file)).explore(new File(repoPath));

        Javancss javancss = new Javancss(files);
        Total total = getTotal(javancss);
        total.setFiles(files.size());

        Packages packages = getPackages(javancss);
        Objects objects = getObjects(files, FileUtil.systemAvailablePath(repoPath));
        Functions functions = getFunctions(javancss) ;


        return new Measure(total, packages, objects, functions);
    }

    private static Functions getFunctions(Javancss javancss) {
        List<Function> functions = new ArrayList<>();
        double ncss = 0.0;
        double ccn = 0.0;
        double javaDocs = 0.0;
        int size = javancss.getFunctionMetrics().size();
        for (FunctionMetric f :  javancss.getFunctionMetrics()){
            ncss += f.ncss;
            ccn += f.ccn;
            javaDocs += f.javadocs;
            functions.add(new Function(f.name, f.ncss, f.ccn, f.javadocs));
        }
        FunctionAverage functionAverage;
        if(size == 0) {
            functionAverage = new FunctionAverage(0,0,0);
        }
        else {
            functionAverage = new FunctionAverage(ncss/size, ccn/size, javaDocs/size);
        }
        return new Functions(functions, functionAverage);
    }

    private static Objects getObjects(List<File> files, String repoPath) {
        List<OObject> oObjects = new ArrayList<>();
        double ncssA = 0.0;
        double functionsA = 0.0;
        double classesA = 0.0;
        double javaDocsA = 0.0;
        double javaDocsLinesA = 0.0;
        double singleCommentLinesA = 0.0;
        double implementationCommentLinesA = 0.0;


        for (File f : files) {
            Javancss javancss = new Javancss(f);
            int ccn = 0;
            for (FunctionMetric functionMetric :  javancss.getFunctionMetrics()){
                ccn += functionMetric.ccn;
            }
            int classes = 0;
            for (PackageMetric packageMetric : javancss.getPackageMetrics()) {
                classes += packageMetric.classes;
            }

            // 存入相对路径
            String path = FileUtil.getRelativePath(repoPath,FileUtil.systemAvailablePath(f.getPath()));
            int ncss = javancss.getNcss();
            int functions = javancss.getFunctions().size();
            int javaDocs = javancss.getJvdc();
            int javaDocsLines = javancss.getJdcl();
            int singleCommentLines = javancss.getSl();
            int implementationCommentLines = javancss.getSl() + javancss.getMl();
            int absoluteLines = javancss.getLOC();

            oObjects.add(new OObject(path, ncss, functions, classes, javaDocs, javaDocsLines, singleCommentLines, implementationCommentLines, ccn, ncss,absoluteLines));
            ncssA += ncss;
            functionsA += functions;
            classesA += classes;
            javaDocsA += javaDocs;
            javaDocsLinesA += javaDocsLines;
            singleCommentLinesA += singleCommentLines;
            implementationCommentLinesA += implementationCommentLines;
        }
        int size = oObjects.size();
        return new Objects(oObjects, new ObjectAverage(ncssA/size, functionsA/size, classesA/size, javaDocsA/size,
                javaDocsLinesA/size, singleCommentLinesA/size, implementationCommentLinesA/size));
    }

    private static Packages getPackages(Javancss javancss) {
        List<Package> packageList = new ArrayList<>();

        double classesA = 0.0;
        double functionsA = 0.0;
        double ncssA = 0.0;
        double javaDocsA = 0.0;

        for (PackageMetric packageMetric : javancss.getPackageMetrics()) {
            String uuid = UUID.randomUUID().toString();
            String name = packageMetric.name;
            int classes = packageMetric.classes;
            int functions = packageMetric.functions;
            int ncss = packageMetric.ncss;
            int javaDocs = packageMetric.javadocs;
            int javaDocsLines = packageMetric.javadocsLn;
            int singleCommentLines = packageMetric.singleLn;
            int multiCommentLines = packageMetric.multiLn;
            classesA += classes;
            functionsA += functions;
            ncssA += ncss;
            javaDocsA += javaDocs;
            Package p = new Package(uuid, name, classes, functions, ncss, javaDocs, javaDocsLines, singleCommentLines, multiCommentLines);
            packageList.add(p);
        }
        int size = javancss.getPackageMetrics().size();
        PackageAverage packageAverage = new PackageAverage(classesA/size, functionsA/size, ncssA/size, javaDocsA/size);
        return new Packages(packageList, packageAverage);
    }

    private static Total getTotal(Javancss javancss) {
        int files = 0;
        int classes = 0;
        int functions = javancss.getFunctions().size();
        int ncss = javancss.getNcss();

        int javaDocs = 0;
        int javaDocsLines = 0;
        int singleCommentLines = 0;
        int multiCommentLines = 0;
        int absoluteLine = javancss.getLOC();

        for (PackageMetric packageMetric : javancss.getPackageMetrics()) {
            classes += packageMetric.classes;
            javaDocs += packageMetric.javadocs;
            javaDocsLines += packageMetric.javadocsLn;
            singleCommentLines += packageMetric.singleLn;
            multiCommentLines += packageMetric.multiLn;
        }
        return new Total(files, classes, functions, ncss, javaDocs, javaDocsLines, singleCommentLines, multiCommentLines,absoluteLine);
    }

}