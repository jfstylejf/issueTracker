package cn.edu.fudan.issueservice.domain.vo;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * @author beethoven
 * @date 2021-05-12 15:40:18
 */
@Data
@Builder
public class DeveloperLivingIssueVO {

    private String developerName;
    private Long num;
    private Level level;

    @Getter
    public enum Level {
        /**
         * level
         */
        worst(1),
        worse(2),
        normal(3),
        better(4),
        best(5);

        private final int type;

        Level(int type) {
            this.type = type;
        }
    }

    public static Level getLevel(int level) {
        for (Level value : Level.values()) {
            if (value.type == level) {
                return value;
            }
        }
        return null;
    }
}
