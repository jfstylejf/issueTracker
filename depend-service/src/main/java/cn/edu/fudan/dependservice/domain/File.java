package cn.edu.fudan.dependservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
public class File {
    private String fileName;
    private String repoUUid;
    private String commitId;
   @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof File)) {
            return false;
        }
        File file = (File) obj;
        return fileName.equals(file.getFileName());
    }

    public File(String fileName, String repoUUid, String commitId) {
        this.fileName = fileName;
        this.repoUUid = repoUUid;
        this.commitId = commitId;

    }
}
