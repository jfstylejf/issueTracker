package cn.edu.fudan.measureservice.dao;

import cn.edu.fudan.measureservice.mapper.AccountMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    public String getDeveloperName(String gitName) {
        if (gitName == null) {
            log.error("cannot get authorName\n");
            return null;
        }
        try {
            String accountName = accountMapper.getAccountName(gitName);
            return accountName == null ? gitName : accountName;
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Autowired
    public void setAccountMapper(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }
}
