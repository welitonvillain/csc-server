package br.com.angeloni.csc.server.service;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ToString
@Configuration
@ConfigurationProperties(prefix = "agendaentrega")
public class AgendaEntregaProperties {

  private String baseUrl;
  private String apiKey;
  private String uuidAnotacao;

}