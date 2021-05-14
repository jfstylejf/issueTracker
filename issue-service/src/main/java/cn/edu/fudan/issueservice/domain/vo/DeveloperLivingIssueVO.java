package cn.edu.fudan.issueservice.domain.vo;

import lombok.Data;
import lombok.Getter;

/**
 * @author beethoven
 * @date 2021-05-12 15:40:18
 */
@Data
public class DeveloperLivingIssueVO {

    private String developerName;
    private Integer num;
    private String level;

    @Getter
    private enum Level {
        /**
         * level
         */
        HIGH("high"),
        MEDIUM("medium"),
        LOW("low");
        private final String type;

        Level(String type) {
            this.type = type;
        }
    }

    private void setLevel(Level level) {
        this.level = level.getType();
    }
}
