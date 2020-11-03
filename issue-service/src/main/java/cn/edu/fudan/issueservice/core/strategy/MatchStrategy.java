package cn.edu.fudan.issueservice.core.strategy;

import cn.edu.fudan.issueservice.domain.dbo.RawIssue;

public interface MatchStrategy {

    /**
     * 该方法的返回布尔值表示，true表示两个raw issue匹配度为100% ，false表示匹配度不是100%
     * @param rawIssue1
     * @param rawIssue2
     * @return
     */
    boolean match(RawIssue rawIssue1, RawIssue rawIssue2);
}
