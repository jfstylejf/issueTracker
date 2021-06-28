package cn.edu.fudan.measureservice.domain.exception;

/**
 * @ClassName: AcquirePropertyException
 * @Description: 获取字段数据异常
 * @Author wjzho
 * @Date 2021/6/25
 */

public class AcquirePropertyException extends RuntimeException {

    AcquirePropertyException (String meg) {
        super(meg);
    }

}
