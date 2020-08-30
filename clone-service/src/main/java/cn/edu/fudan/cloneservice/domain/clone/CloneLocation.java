package cn.edu.fudan.cloneservice.domain.clone;

/**
 * @author zyh
 * @date 2020/5/25
 */
public class CloneLocation {
    private String uuid;
    private String repoId;
    private String commitId;
    /**
     * 记录clone组的组号
     */
    private String category;

    private String filePath;
    private String methodLines;
    private String cloneLines;
    /**
     * 记录是方法级还是片段级
     */
    private String type;

    private String className;
    private String methodName;
    /**
     * 记录去除空行和注释行的location行号
     */
    private String num;

    private String code;

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }

        if(obj == null){
            return false;
        }

        if(obj instanceof CloneLocation){
            CloneLocation cloneLocation = (CloneLocation) obj;
            return cloneLocation.uuid.equals(this.uuid);
        }

        return false;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRepoId() {
        return repoId;
    }

    public void setRepoId(String repoId) {
        this.repoId = repoId;
    }

    public String getCommitId() {
        return commitId;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getMethodLines() {
        return methodLines;
    }

    public void setMethodLines(String methodLines) {
        this.methodLines = methodLines;
    }

    public String getCloneLines() {
        return cloneLines;
    }

    public void setCloneLines(String cloneLines) {
        this.cloneLines = cloneLines;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
