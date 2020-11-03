package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.dto.LocationMatchResult;
import cn.edu.fudan.issueservice.util.CosineUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * fixme 修改不符合规范的field命名
 * @author fancying
 */
@Data
@NoArgsConstructor
public class Location {

    private String uuid;
    private int start_line;
    private int end_line;
    private String bug_lines;
    private int start_token;
    private int end_token;
    private String file_path;
    private String class_name;
    private String method_name;
    private String rawIssue_id;
    private String code;

    /**
     * location 起始位置相对于 所在方法或者属性起始位置的偏移量
     */
    private int offset = 1;

    private List<LocationMatchResult> locationMatchResults = new ArrayList<>(0);
    private boolean matched = false;
    private int matchedIndex = -1;

    private List<Object> tokens = null;

    public List<Object> getTokens() {
        if (tokens == null || tokens.size() == 0) {
            // 去掉注释的token
            tokens = CosineUtil.lexer(CosineUtil.removeComment(code), true);
        }
        return tokens;
    }

//    public Location() {
//    }
//
//    public Location(String uuid, int start_line, int end_line, String bug_lines, String file_path, String class_name, String method_name, String rawIssue_id, String code) {
//        this.uuid = uuid;
//        this.start_line = start_line;
//        this.end_line = end_line;
//        this.bug_lines = bug_lines;
//        this.file_path = file_path;
//        this.class_name = class_name;
//        this.method_name = method_name;
//        this.rawIssue_id = rawIssue_id;
//        this.code = code;
//    }


    public boolean isSame(Location location) {
        if (StringUtils.isEmpty(method_name) || StringUtils.isEmpty(code) ||
                StringUtils.isEmpty(location.getMethod_name()) || StringUtils.isEmpty(location.getCode())) {
            return false;
        }

        return method_name.equals(location.getMethod_name()) && code.equals(location.getCode());
    }


    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Location)) {
            return false;
        }
        Location location = (Location) obj;
        if(this.class_name != null && location.class_name != null
                && this.method_name != null && location.method_name!=null) {
            if (bug_lines==null && location.bug_lines==null) {
                return location.class_name.equals(class_name) &&
                        location.method_name.equals(method_name) &&
                        location.file_path.equals(file_path);
            } else if(bug_lines!=null && location.bug_lines!=null){

                return location.class_name.equals(class_name) &&
                        location.method_name.equals(method_name) &&
                        location.file_path.equals(file_path) &&
                        bug_lines.split(",").length == location.bug_lines.split(",").length ;

            }

        }
        return false;
    }


    public void setMappedLocation(Location location2, double matchDegree) {
        matched = true;
        locationMatchResults.add(LocationMatchResult.newInstance(location2, matchDegree));
    }
}
