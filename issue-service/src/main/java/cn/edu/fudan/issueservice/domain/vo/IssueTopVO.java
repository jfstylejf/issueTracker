package cn.edu.fudan.issueservice.domain.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

/**
 * @author beethoven
 * @date 2021-03-18 17:17:42
 */
@Getter
@Builder
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class IssueTopVO {

    private final String issueType;
    private final Integer quantity;
    private final Integer solved;
    private final Integer open;

}
