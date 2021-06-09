package cn.edu.fudan.dependservice.domain;

import lombok.Data;

/**
 * @author shaoxi
 * @version 0.1.0
 * @create 2021-06-07 16:11
 **/
@Data
public class FileNode {
    private int id;
    private String filePath;
    private String fileName;
    public FileNode(String filePath){
        this.filePath=filePath;
        this.fileName=filePath.substring(filePath.lastIndexOf("/")+1);
    }
    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 17 * result + filePath.hashCode();
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof FileNode)) {
            // instanceof 已经处理了obj = null的情况
            return false;
        }
        FileNode fileNodeObj = (FileNode) obj;
        // 地址相等
        if (this ==fileNodeObj ) {
            return true;
        }
        // 如果两个对象姓名、年龄、性别相等，我们认为两个对象相等
        if (fileNodeObj.filePath.equals(this.filePath) && fileNodeObj.fileName.equals(this.fileName)) {
            return true;
        } else {
            return false;
        }

    }

}
