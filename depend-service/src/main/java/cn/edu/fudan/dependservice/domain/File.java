package cn.edu.fudan.dependservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
public class File {
    private String projectName;
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
    @Override
    public int hashCode() {
        int result = fileName.hashCode();
        result = 17 * result + repoUUid.hashCode();
        return result;
    }


    public File(String fileName, String repoUUid, String commitId) {
        this.fileName = fileName;
        this.repoUUid = repoUUid;
        this.commitId = commitId;

    }
}
