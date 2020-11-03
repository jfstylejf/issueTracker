package cn.edu.fudan.projectmanager.exception;

import lombok.NoArgsConstructor;

/**
 * description:
 *
 * @author fancying
 * create: 2020-09-29 10:37
 **/
@NoArgsConstructor
public class RunTimeException extends Exception{

    public RunTimeException(String message) {
        super(message);
    }
}