package cn.edu.fudan.issueservice.domain.enums;

import lombok.Getter;

/**
 * @author beethoven
 * @date 2021-07-01 18:08:23
 */
public class IssuePriorityEnums {

    @Getter
    public enum JavaIssuePriorityEnum {

        /**
         * 缺陷优先级
         */
        LOW("Low", 4),
        URGENT("Urgent", 1),
        NORMAL("Normal", 3),
        HIGH("High", 2),
        IMMEDIATE("Immediate", 0);

        private final String name;
        private final int rank;

        JavaIssuePriorityEnum(String name, int rank) {
            this.name = name;
            this.rank = rank;
        }

        public static JavaIssuePriorityEnum getPriorityEnum(String name) {
            for (JavaIssuePriorityEnum priority : JavaIssuePriorityEnum.values()) {
                if (priority.getName().equals(name)) {
                    return priority;
                }
            }
            return null;
        }

        public static JavaIssuePriorityEnum getPriorityEnumByRank(int rank) {
            for (JavaIssuePriorityEnum priority : JavaIssuePriorityEnum.values()) {
                if (priority.getRank() == rank) {
                    return priority;
                }
            }
            return null;
        }
    }

    @Getter
    public enum JavaScriptIssuePriorityEnum {
        /**
         * JS缺陷优先级
         */
        OFF("Off", 0),
        WARN("Warn", 1),
        ERROR("Error", 2);

        private final String name;
        private final int rank;

        JavaScriptIssuePriorityEnum(String name, int rank) {
            this.name = name;
            this.rank = rank;
        }

        public static String getPriorityByRank(int rank) {
            for (JavaScriptIssuePriorityEnum priority : JavaScriptIssuePriorityEnum.values()) {
                if (priority.getRank() == rank) {
                    return priority.name;
                }
            }
            return null;
        }
    }

    @Getter
    public enum CppIssuePriorityEnum {
        /**
         * c++缺陷优先级
         */
        INFORMATION("Information", 0),
        WARNING("Warning", 1),
        SERIOUS("Serious", 2),
        CRITICAL("Critical", 3);

        private final String name;
        private final int rank;

        CppIssuePriorityEnum(String name, int rank) {
            this.name = name;
            this.rank = rank;
        }

        public static int getRankByPriority(String name) {

            if (INFORMATION.name.equals(name)) {
                return INFORMATION.rank;
            } else if (WARNING.name.equals(name)) {
                return WARNING.rank;
            } else if (SERIOUS.name.equals(name)) {
                return SERIOUS.rank;
            } else if (CRITICAL.name.equals(name)) {
                return CRITICAL.rank;
            }

            return 0;
        }
    }
}
