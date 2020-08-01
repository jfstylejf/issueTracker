package cn.edu.fudan.cloneservice.lock;

import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author zyh
 * @date 2020/5/28
 */
@Component
public class CloneScanLock extends ReentrantLock {

    public CloneScanLock(){
        super();
    }
}
