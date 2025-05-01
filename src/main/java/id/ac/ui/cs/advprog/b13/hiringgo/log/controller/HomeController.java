package id.ac.ui.cs.advprog.b13.hiringgo.log.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    /**
     * Catch-all root mapping so "/" never returns 404.
     */
    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("OK");
    }
}
