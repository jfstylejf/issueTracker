package cn.edu.fudan.cloneservice.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadFactory;

/**
 * @author zyh
 * @date 2020/5/17
 */
public class CloneThreadFactory implements ThreadFactory {

    private int          counter;
    private String       name;
    private List<String> stats;

    public CloneThreadFactory(String name){
        counter = 1;
        this.name = name;
        stats = new ArrayList<>();
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, name + "CloneThread_" + counter);
        counter++;
        stats.add(String.format("Created thread %d with accountName %s on %s \n", t.getId(), t.getName(), new Date()));
        return t;
    }

    public String getStats() {
        StringBuffer buffer = new StringBuffer();
        stats.forEach(str -> buffer.append(str));

        return buffer.toString();
    }


}
