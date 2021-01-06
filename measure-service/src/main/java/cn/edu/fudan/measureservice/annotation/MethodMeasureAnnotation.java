package cn.edu.fudan.measureservice.annotation;
import java.lang.annotation.*;

/**
 * 记录方法调用
 * @author wjzho
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface MethodMeasureAnnotation {

}
