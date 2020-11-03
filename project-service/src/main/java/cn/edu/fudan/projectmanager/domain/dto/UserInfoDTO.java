package cn.edu.fudan.projectmanager.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author fancying
 * create: 2020-09-28 13:41
 **/
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