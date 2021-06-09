package cn.edu.fudan.issueservice.domain.dbo;

import cn.edu.fudan.issueservice.domain.dto.LocationMatchResult;
import cn.edu.fudan.issueservice.util.CosineUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * fixme 修改不符合规范的field命名 已修改
 *
 * @author fancying
 */
@Data
@NoArgsConstructor
public class Location {

    private String uuid;
    private int startLine;
    private int endLine;
    private String bugLines;
    private int startToken;
    private int endToken;
    private String filePath;
    private String className;
    private String methodName;
    private String rawIssueId;
    private String code;

    /**
     * location 起始位置相对于 所在方法或者属性起始位置的偏移量
     */
    private int offset = 0;

    private List<LocationMatchResult> locationMatchResults = new ArrayList<>(0);
    private boolean matched = false;
    private int matchedIndex = -1;

    private List<Object> tokens = null;

    public static List<Location> valueOf(JSONArray locations) {
        List<Location> locationList = new ArrayList<>();
        for (int i = 0; i < locations.size(); i++) {
            JSONObject tempLocation = locations.getJSONObject(i);
            Location location = new Location();
            location.setUuid(UUID.randomUUID().toString());
            location.setBugLines(tempLocation.getString("bug_lines"));
            location.setCode(tempLocation.getString("code"));
            location.setStartLine(tempLocation.getIntValue("start_line"));
            location.setEndLine(tempLocation.getIntValue("end_line"));
            location.setMethodName(tempLocation.getString("method_name"));
            locationList.add(location);
        }
        return locationList;
    }

    public List<Object> getTokens() {
        if (tokens == null) {

            // 去掉注释的token
            tokens = CosineUtil.lexer(CosineUtil.removeComment(code), true);
        }
        return tokens;
    }

    public boolean isSame(Location location) {
        if (StringUtils.isEmpty(methodName) || StringUtils.isEmpty(code) ||
                StringUtils.isEmpty(location.getMethodName()) || StringUtils.isEmpty(location.getCode())) {
            return false;
        }

        return methodName.equals(location.getMethodName()) && code.equals(location.getCode());
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
        if (this.className != null && location.className != null
                && this.methodName != null && location.methodName != null) {
            if (bugLines == null && location.bugLines == null) {
                return location.className.equals(className) &&
                        location.methodName.equals(methodName) &&
                        location.filePath.equals(filePath);
            } else if (bugLines != null && location.bugLines != null) {

                return location.className.equals(className) &&
                        location.methodName.equals(methodName) &&
                        location.filePath.equals(filePath) &&
                        bugLines.split(",").length == location.bugLines.split(",").length;

            }

        }
        return false;
    }

    public void setMappedLocation(Location location2, double matchDegree) {
        matched = true;
        locationMatchResults.add(LocationMatchResult.newInstance(location2, matchDegree));
    }
}
