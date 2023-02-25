package br.com.angeloni.csc.server.service;

import br.com.angeloni.csc.server.broker.Queues;
import br.com.angeloni.csc.server.model.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationsService {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private MetricsService metricsService;

  public void notifyError(String message, Throwable cause) {
    log.error(message, cause);

    try {
      NotificationMessage notification = NotificationMessage.builder()
              .message(message)
              .detail(cause != null ? cause.getMessage().replaceAll("\"", "'") : null)
              .build();
      rabbitTemplate.convertAndSend(Queues.NOTIFICATIONS, notification);

    } catch (Exception ex) {
      log.error("Erro durante o envio da notificação: " + ex);
    }

    metricsService.getNotificacoesErro().increment();
  }

  public void notifyError(String message) {
    notifyError(message, null);
  }

}
