package br.com.angeloni.csc.server.broker;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Queues {

  public static final String DLQ_SUFFIX = ".dlq";
  public static final String NOTIFICATIONS = "csc.notifications.hml";
  public static final String AGENDAMENTOS = "csc.agendamentos.hml";
  public static final String DOWNLOADS = "csc.downloads.hml";

  @Bean
  public Queue notificationsQueue() {
    return QueueBuilder.durable(NOTIFICATIONS)
      .withArgument("x-dead-letter-exchange", "")
      .withArgument("x-dead-letter-routing-key", NOTIFICATIONS + DLQ_SUFFIX)
      .build();
  }

  @Bean
  public Queue notificationsDlq() {
    return QueueBuilder.durable(NOTIFICATIONS + DLQ_SUFFIX).build();
  }

  @Bean
  public Queue agendamentosQueue() {
    return QueueBuilder.durable(AGENDAMENTOS)
      .withArgument("x-dead-letter-exchange", "")
      .withArgument("x-dead-letter-routing-key", AGENDAMENTOS + DLQ_SUFFIX)
      .build();
  }

  @Bean
  public Queue agendamentosDlq() {
    return QueueBuilder.durable(AGENDAMENTOS + DLQ_SUFFIX).build();
  }

  @Bean
  public Queue downloadsQueue() {
    return QueueBuilder.durable(DOWNLOADS)
      .withArgument("x-dead-letter-exchange", "")
      .withArgument("x-dead-letter-routing-key", DOWNLOADS + DLQ_SUFFIX)
      .build();
  }

  @Bean
  public Queue downloadsDlq() {
    return QueueBuilder.durable(DOWNLOADS + DLQ_SUFFIX).build();
  }

}
