
package com.example.sfservice;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SfServiceApplication {
  public static void main(String[] args) { SpringApplication.run(SfServiceApplication.class, args); }

  @RestController
  static class Ping {
    @GetMapping("/healthz")
    public String ok(){ return "OK"; }
  }
}
