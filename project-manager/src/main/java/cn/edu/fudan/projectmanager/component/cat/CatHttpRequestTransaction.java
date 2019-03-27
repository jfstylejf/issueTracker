package cn.edu.fudan.projectmanager.component.cat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CatHttpRequestTransaction {
    String name() default "";
    String type() default "ProjectURL";
}