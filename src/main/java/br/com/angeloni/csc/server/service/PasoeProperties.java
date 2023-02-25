package br.com.angeloni.csc.server.service;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ToString
@Configuration
@ConfigurationProperties(prefix = "pasoe")
public class PasoeProperties {

  private String baseUrl;

}