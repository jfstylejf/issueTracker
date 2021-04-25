package cn.edu.fudan.cloneservice.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wjzho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    String token;
    String uuid;
    /**
     * 用户权限和职责 后面改成枚举类
     */
    Integer right;
}
