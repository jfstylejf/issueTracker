package cn.edu.fudan.projectmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author Richy
 * create: 2021-01-15 17:11
 **/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private String accountName;
    private String accountId;
}