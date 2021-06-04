package cn.edu.fudan.accountservice.exception;

import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author Richy
 * create: 2021-06-03 09:39
 **/
@NoArgsConstructor
public class RunTimeException extends Exception {

    public RunTimeException(String message) {
        super(message);
    }
}