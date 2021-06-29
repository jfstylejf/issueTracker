package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @ClassName: AccountDao
 * @Description: 操作account相关数据
 * @Author wjzho
 * @Date 2021/5/24
 */
@Slf4j
@Repository
public class AccountDao {

    private AccountMapper accountMapper;

    /**
     * 获取开发者聚合名，若为空，则返回它的 gitName
     * @param gitName 查询 gitName
     * @return
     */
    @Cacheable(value = "developer",key = "#gitName")
    public String getDeveloperName(String gitName) {
        if (gitName == null) {
            log.error("cannot get authorName\n");
            return null;
        }
        try {
            String accountName = accountMapper.getAccountName(gitName);
            return accountName == null ? gitName : accountName;
        }catch (Exception e) {
            log.error("failed to get accountName with gitName : {}\n",gitName);
            log.error(e.getMessage());
            return null;
        }
    }

    /**
     * 获取全部开发者下的 gitName
     * @return 开发者
     */
    @Cacheable("allGitName")
    public List<String> getAllAccountGitName() {
        return accountMapper.getAllAccountGitNameList();
    }


    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
}
