package br.com.angeloni.csc.server.broker;

import br.com.angeloni.csc.server.model.NotificationMessage;
import br.com.angeloni.csc.server.service.NotificationsProperties;
import brave.Tracing;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class NotificationsProcessor {

  @Autowired
  private NotificationsProperties properties;

  @Autowired
  private Configuration freemarker;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private Tracing tracing;

  @RabbitListener(queues = Queues.NOTIFICATIONS)
  public void processMessage(final NotificationMessage message) {
    notifyErrorTeams(message);
  }

  private void notifyErrorTeams(NotificationMessage notification) {
    try {
      Template template = freemarker.getTemplate("errorTeams.ftl");
      StringWriter out = new StringWriter();
      Map<String, Object> map = new HashMap<>();
      map.put("error", notification);
      map.put("traceId", tracing.currentTraceContext().get().traceIdString());
      template.process(map, out);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<>(out.toString(), headers);
      String url = properties.getErrors().getTeams();
      restTemplate.postForEntity(url, entity, Void.class);
    } catch (Exception ex) {
      throw new IllegalStateException("Nao foi possível enviar notificação de erro para o teams: " + notification, ex);
    }
  }

}