package cn.edu.fudan.common.scan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 统一开发者名称
 * @author Song Rui
 */
public interface UnifyAuthor {
    Logger log = LoggerFactory.getLogger(AbstractScanServiceImpl.class);

    /**
     * 针对一个开发者有多个开发账号的情况，将账号名转化为唯一名称
     * 若查询不到唯一名称，默认返回原名
     * @param committer 当前开发账号名称
     * @return 开发者唯一名称
     */
    default String unifyAuthorName(String committer) {
        if(committer == null || committer.length() == 0){
            log.warn("lack info of committer");
            return committer;
        }
        String authorName = committer;
        try {
            authorName = this.getAccountName(committer);
            if (authorName == null || authorName.length() == 0) {
                return committer;
            }
        } catch (Exception e) {
            log.error("get account name:{} failed", committer);
            log.error(e.getMessage());
        }

        return authorName;
    }

    /**
     * 查询开发者唯一名称
     * @param committer 当前开发账号名称
     * @return 开发者唯一名称
     */
    String getAccountName(String committer);
}
