package cn.edu.fudan.issueservice.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Beethoven
 */

@Getter
@AllArgsConstructor
public enum RepoNatureEnum {
    /**
     * issue repo nature
     */
    MAIN("main"),
    UPDATE("update");

    private String type;
}
