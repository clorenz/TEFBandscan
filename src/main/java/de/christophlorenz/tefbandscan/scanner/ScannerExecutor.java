package de.christophlorenz.tefbandscan.scanner;

import jakarta.annotation.PostConstruct;
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
        Thread haltedHook = new Thread(() -> { System.out.println("Terminating..."); Runtime.getRuntime().halt(0); });
        Runtime.getRuntime().addShutdownHook(haltedHook);
        ScannerTask scannerTask = applicationContext.getBean(ScannerTask.class);
        taskExecutor.execute(scannerTask);
    }
}
