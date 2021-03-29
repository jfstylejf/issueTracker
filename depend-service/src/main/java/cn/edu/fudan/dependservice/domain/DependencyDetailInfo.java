package cn.edu.fudan.dependservice.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class DependencyDetailInfo implements Serializable {
    private String fileName;
    private String filePath;
    private String metaFileUuid;

    public DependencyDetailInfo() {

    }
    public DependencyDetailInfo(String fileName,String filePath,String metaFileUuid) {

        this.fileName=fileName;
        this.filePath=filePath;
        this.metaFileUuid=metaFileUuid;

    }

}
