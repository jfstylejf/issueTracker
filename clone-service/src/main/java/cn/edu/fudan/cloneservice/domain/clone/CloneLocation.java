package cn.edu.fudan.cloneservice.domain.clone;

import cn.edu.fudan.cloneservice.domain.CloneDetailOverall;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
/**
 * @author zyh
 * @date 2020/5/25
 */
public class CloneLocation implements Comparable<CloneLocation>{
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
    private Date commitTime;
    private String className;
    private String methodName;
    /**
     * 记录去除空行和注释行的location行号
     */
    private String num;

    private String code;
    @Override
    public int compareTo(CloneLocation o) {
        int thisValue;
        int objectValue;
        thisValue = Integer.parseInt(this.category);
        objectValue = Integer.parseInt(o.category);
        if(thisValue > objectValue){
            return 1;
        }else if(thisValue < objectValue){
            return -1;
        }
        return 0;
    }

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


}
