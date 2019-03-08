package cn.edu.fudan.measureservice.domain;

public class Package {

    private String name;
    private int classes;
    private int functions;
    private int ncss;
    private int javaDocs;
    private int javaDocsLines;
    private int singleCommentLines;
    private int multiCommentLines;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClasses() {
        return classes;
    }

    public void setClasses(int classes) {
        this.classes = classes;
    }

    public int getFunctions() {
        return functions;
    }

    public void setFunctions(int functions) {
        this.functions = functions;
    }

    public int getNcss() {
        return ncss;
    }

    public void setNcss(int ncss) {
        this.ncss = ncss;
    }

    public int getJavaDocs() {
        return javaDocs;
    }

    public void setJavaDocs(int javaDocs) {
        this.javaDocs = javaDocs;
    }

    public int getJavaDocsLines() {
        return javaDocsLines;
    }

    public void setJavaDocsLines(int javaDocsLines) {
        this.javaDocsLines = javaDocsLines;
    }

    public int getSingleCommentLines() {
        return singleCommentLines;
    }

    public void setSingleCommentLines(int singleCommentLines) {
        this.singleCommentLines = singleCommentLines;
    }

    public int getMultiCommentLines() {
        return multiCommentLines;
    }

    public void setMultiCommentLines(int multiCommentLines) {
        this.multiCommentLines = multiCommentLines;
    }
}