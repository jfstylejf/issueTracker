package cn.edu.fudan.scanservice.domain.enums;

/**
 * description: 编译工具
 *
 * @author fancying
 * create: 2020-03-16 09:39
 **/
public enum CompileTool {
    /**
     * 编译工具的名字
     * @value 编译工具需要的文件名
     */
    maven("pom.xml"),
    gradle("build.gradle");

    private final String compileFile;

    CompileTool(String compileFile) {
        this.compileFile = compileFile;
    }

    public String compileFile() {
        return this.compileFile;
    }


}
