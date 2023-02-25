package br.com.angeloni.csc.server.service;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ToString
@Configuration
@ConfigurationProperties(prefix = "file-server")
public class FileServerProperties {

  private String protocol;
  private String host;
  private Integer port;
  private String username;
  private String password;
  private String path;

}