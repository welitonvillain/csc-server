package br.com.angeloni.csc.server.broker;

import br.com.angeloni.csc.server.model.DownloadMessage;
import br.com.angeloni.csc.server.service.DownloadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DownloadProcessor {

  @Autowired
  private DownloadService downloadService;

  @RabbitListener(queues = Queues.DOWNLOADS)
  public void processMessage(final DownloadMessage message) {
    downloadService.processaDownload(message);
  }

}