package br.com.angeloni.csc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"br.com.angeloni", "com.agendarentrega"})
public class IntegracaoCSCServer {

  public static void main(final String[] args) {
    SpringApplication.run(IntegracaoCSCServer.class, args);
  }

}
