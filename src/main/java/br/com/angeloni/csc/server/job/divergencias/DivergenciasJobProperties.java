package br.com.angeloni.csc.server.job.divergencias;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@ToString
@Configuration
@ConfigurationProperties(prefix = "job.divergencias")
public class DivergenciasJobProperties {

  private Duration startDelay;
  private Duration interval;
  private boolean enabled = true;

}
