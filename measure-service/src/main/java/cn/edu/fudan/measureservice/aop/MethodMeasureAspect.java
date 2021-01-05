package cn.edu.fudan.measureservice.aop;

/**
 * @author wjzho
 */

import cn.edu.fudan.measureservice.util.MapUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 记录一次请求中,流经的所有方法的调用耗时及次数
 *
 * @author wjzho
 */
@Component
@Aspect
public class MethodMeasureAspect {
    private static final Logger logger = LoggerFactory.getLogger(MethodMeasureAspect.class);

    private Map<String, Integer> methodCount = new ConcurrentHashMap();

    private Map<String, List<Integer>> methodCost = new ConcurrentHashMap();

    @SuppressWarnings(value = "unchecked")
    @Around("@annotation(cn.edu.fudan.measureservice.annotation.MethodMeasureAnnotation)"
            + "|| execution(* cn.edu.fudan.measureservice.controller.MeasureDeveloperController.*(..)) "
            + "|| execution(* cn.edu.fudan.measureservice.service.MeasureDeveloperService.*(..)) "
            + "|| execution(* cn.edu.fudan.measureservice.component.RestInterfaceManager.*(..))"
            + "|| execution(* cn.edu.fudan.measureservice.mapper.*.*(..))")
    public Object process(ProceedingJoinPoint joinPoint) {
        Object obj = null;
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = className + "_" + getMethodName(joinPoint);
        long startTime = System.currentTimeMillis();
        try {
            obj = joinPoint.proceed();
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            logger.debug("method={}, cost_time={}", methodName, costTime);
            methodCount.put(methodName, methodCount.getOrDefault(methodName, 0) + 1);
            List<Integer> costList = methodCost.getOrDefault(methodName, new ArrayList<>());
            costList.add((int)costTime);
            methodCost.put(methodName, costList);
        }
        return obj;
    }

    public String getMethodName(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        return method.getName();
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder("MethodCount:\n");
        Map<String,Integer> sorted =  MapUtil.sortByValue(methodCount);
        sorted.forEach(
                (method, count) -> {
                    sb.append("method=" + method + ", " + "count=" + count+'\n');
                }
        );
        sb.append('\n');
        sb.append("MethodCosts:\n");
        methodCost.forEach(
                (method, costList) -> {
                    IntSummaryStatistics stat = costList.stream().collect(Collectors.summarizingInt(x->x));
                    String info = String.format("method=%s, sum=%d, avg=%d, max=%d, min=%d, count=%d", method,
                            (int)stat.getSum(), (int)stat.getAverage(), stat.getMax(), stat.getMin(), (int)stat.getCount());
                    sb.append(info+'\n');
                }
        );

        sb.append('\n');
        sb.append("DetailOfMethodCosts:\n");
        methodCost.forEach(
                (method, costList) -> {
                    String info = String.format("method=%s, cost=%s", method, costList);
                    sb.append(info+'\n');
                }
        );
        return sb.toString();
    }
}
