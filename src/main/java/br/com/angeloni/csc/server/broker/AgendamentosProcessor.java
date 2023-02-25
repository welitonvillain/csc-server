package br.com.angeloni.csc.server.broker;

import br.com.angeloni.csc.server.model.AgendamentoMessage;
import br.com.angeloni.csc.server.service.AgendamentosService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AgendamentosProcessor {

  @Autowired
  private AgendamentosService agendamentosService;

  @RabbitListener(queues = Queues.AGENDAMENTOS)
  public void processMessage(final AgendamentoMessage agendamento) {
    agendamentosService.processaAgendamento(agendamento);
  }

}