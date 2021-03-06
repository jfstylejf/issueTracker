package cn.edu.fudan.measureservice.aop;

import cn.edu.fudan.measureservice.component.RestInterfaceManager;
import cn.edu.fudan.measureservice.domain.dto.RepoResourceDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author fancying
 * create: 2020-04-22 16:02
 **/
@Aspect
@Component
@Slf4j
public class ResourceAspect {

    private RestInterfaceManager restInvoker;

    /**
     * 定义切点
     */
    @Pointcut("@annotation(cn.edu.fudan.measureservice.annotation.RepoResource)")
    public void resource() {
    }


    @Before("resource()")
    public void getRepoResource(JoinPoint joinPoint) {
        for (Object o : joinPoint.getArgs()) {
            if (o instanceof RepoResourceDTO) {
                RepoResourceDTO repoResourceDTO = (RepoResourceDTO)o;
                //String repoPath = "C:\\Users\\wjzho\\Desktop\\js_test\\fortestjs-davidtest_duplicate_fdse-0";
                String repoPath = restInvoker.getRepoPath (repoResourceDTO.getRepoUuid(), null);
                repoResourceDTO.setRepoPath (repoPath);
                log.info("get repo:{}, path:{}", repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
                return;
            }
        }
        log.error("no parameter RepoResourceDTO ");
    }

    @AfterReturning("resource()")
    public void releaseRepoRelease(JoinPoint joinPoint) {
        for (Object o : joinPoint.getArgs()) {
            if (o instanceof RepoResourceDTO) {
                RepoResourceDTO repoResourceDTO = (RepoResourceDTO)o;
                log.info("free repo:{}, path:{}", repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
                restInvoker.freeRepoPath(repoResourceDTO.getRepoUuid(), repoResourceDTO.getRepoPath());
                return;
            }
        }
        log.error("no parameter RepoResourceDTO ");
    }

    @Autowired
    public void setRestInvoker(RestInterfaceManager restInvoker) {
        this.restInvoker = restInvoker;
    }
}