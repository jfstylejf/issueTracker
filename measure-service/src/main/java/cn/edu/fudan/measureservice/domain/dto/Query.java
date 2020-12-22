package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author wjzho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Query {
    private String token;
    private String since;
    private String until;
    private String developer;
    private List<String> repoUuidList;
}
