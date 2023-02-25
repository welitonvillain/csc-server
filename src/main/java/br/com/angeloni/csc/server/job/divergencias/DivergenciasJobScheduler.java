package br.com.angeloni.csc.server.job.divergencias;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Configuration
@Slf4j
@Profile("!test")
public class DivergenciasJobScheduler {

  @Autowired
  private DivergenciasJobProperties jobProperties;

  @Bean("divergenciasJobDetail")
  public JobDetail jobDetail() {
    return JobBuilder.newJob(DivergenciasJob.class)
      .withIdentity(DivergenciasJob.class.getSimpleName())
      .storeDurably()
      .build();
  }

  @Bean("divergenciasJobTrigger")
  public Trigger jobTrigger() {
    log.info("Configurando o job de divergencias com atraso de {} segundos e intervalo de {} segundos",
      jobProperties.getStartDelay().getSeconds(),
      jobProperties.getInterval().getSeconds());

    SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
      .withIntervalInSeconds(Long.valueOf(jobProperties.getInterval().getSeconds()).intValue())
      .withMisfireHandlingInstructionFireNow()
      .repeatForever();

    Date delayedStart = Date.from(LocalDateTime.now()
      .plusSeconds(jobProperties.getStartDelay().getSeconds())
      .atZone(ZoneId.systemDefault()).toInstant());

    return TriggerBuilder.newTrigger()
      .startAt(delayedStart)
      .forJob(jobDetail())
      .withSchedule(scheduleBuilder)
      .build();
  }

}
