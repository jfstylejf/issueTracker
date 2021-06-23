package cn.edu.fudan.cloneservice.domain;

import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JiraCount implements Comparable<JiraCount>, Serializable {
    private String developerName;
    private int num = 0;

    @Override
    public int compareTo(JiraCount o) {
        int thisValue = this.num;
        int objectValue = o.num;
        if(thisValue > objectValue){
            return 1;
        }else if(thisValue < objectValue){
            return -1;
        }
        return 0;
    }
}
