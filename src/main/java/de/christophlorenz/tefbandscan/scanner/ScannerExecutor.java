package de.christophlorenz.tefbandscan.scanner;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ScannerExecutor {

    private final TaskExecutor taskExecutor;
    private final ApplicationContext applicationContext;

    public ScannerExecutor(TaskExecutor taskExecutor, ApplicationContext applicationContext) {
        this.taskExecutor = taskExecutor;
        this.applicationContext = applicationContext;
    }

    @PostConstruct
    public void atStartup() {
        ScannerTask scannerTask = applicationContext.getBean(ScannerTask.class);
        taskExecutor.execute(scannerTask);
    }
}
