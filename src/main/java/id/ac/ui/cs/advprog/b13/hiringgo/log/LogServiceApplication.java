package id.ac.ui.cs.advprog.b13.hiringgo.log;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan; // Import @ComponentScan
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
// Explicitly define the base packages to scan.
// This is generally covered by @SpringBootApplication if LogServiceApplication
// is in a parent package of your components, but adding it explicitly can
// resolve or highlight configuration issues.
@ComponentScan(basePackages = {"id.ac.ui.cs.advprog.b13.hiringgo.log"})
@EnableAsync
public class LogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogServiceApplication.class, args);
    }

}
