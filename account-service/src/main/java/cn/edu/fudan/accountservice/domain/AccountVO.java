package cn.edu.fudan.accountservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author fancying
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountVO implements Serializable {

    private String username;
    private String token;
    private Integer right;

}
