package cn.edu.fudan.issueservice.component.scan;

import cn.edu.fudan.issueservice.core.ToolInvoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ScanApplicationRunner implements ApplicationRunner {

    private ToolInvoker toolInvoker;

    @Autowired
    public void setToolInvoker(ToolInvoker toolInvoker) {
        this.toolInvoker = toolInvoker;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}
