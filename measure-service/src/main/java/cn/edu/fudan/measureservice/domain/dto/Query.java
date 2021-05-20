package cn.edu.fudan.measureservice.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @author wjzho
 */
@Data
@AllArgsConstructor
@Builder
public class Query implements Serializable {
    private String token;
    private String since;
    private String until;
    /**
     * note : 聚合后名
     */
    private String developer;
    private List<String> repoUuidList;

    public Query(Query redisQuery) {
        this.token = redisQuery.getToken();
        this.since = redisQuery.getSince();
        this.until = redisQuery.getUntil();
        this.developer = redisQuery.getDeveloper();
        this.repoUuidList = redisQuery.getRepoUuidList();
    }

}
