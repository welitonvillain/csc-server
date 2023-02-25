package br.com.angeloni.csc.server.rest;

import br.com.angeloni.csc.server.model.AgendamentoMessage;
import br.com.angeloni.csc.server.model.WebookMessage;
import br.com.angeloni.csc.server.service.*;
import com.agendarentrega.agendas.generated.dto.ApiCancelarAgendaRequest;
import com.agendarentrega.agendas.generated.dto.ApiConfirmarAgendaRequest;
import com.agendarentrega.pedidos.generated.dto.PedidoImportarPedidoRequest;
import com.agendarentrega.pedidos.generated.dto.PedidoPedido;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@Api(tags = "Todos")
public class ApiResource {

  @Autowired
  private AgendamentosService agendamentosService;

  @Autowired
  private DivergenciasService divergenciasService;

  @Autowired
  private PedidosService pedidosService;

  @Autowired
  private RabbitAdmin rabbitAdmin;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AgendaEntregaService agendaEntregaService;

  @Autowired
  private NotificationsService notificationsService;

  @PostMapping(value = "/agendamentos/webhook", produces = "application/json")
  @ApiOperation(value = "Recebe um agendamento via webhook", tags = "Agendamentos")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Sucesso")
  })
  public void recebeAgendamentoWebhook(@RequestBody(required = true) String payload) throws Exception {
    try {
      WebookMessage webhook = objectMapper.readValue(payload, WebookMessage.class);
      if (webhook.getAgenda() != null) {
        agendamentosService.recebeAgendamento(webhook.getAgenda());
      } else {
        throw new IllegalStateException("Payload inválido: " + payload);
      }
    } catch (Exception ex) {
      notificationsService.notifyError("Erro ao receber agendamento via webhook", ex);
      throw ex;
    }
  }

  @PostMapping(value = "/agendamentos", produces = "application/json")
  @ApiOperation(value = "Recebe um agendamento manualmente", tags = "Agendamentos")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Sucesso")
  })
  public void recebeAgendamento(@RequestBody(required = true) AgendamentoMessage agendamento) {
    agendamentosService.recebeAgendamento(agendamento);
  }

  @PostMapping(value = "/agendamentos/{uuid}/confirmar", produces = "application/json")
  @ApiOperation(value = "Confirma um agendamento manualmente", tags = "Agendamentos")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Sucesso")
  })
  public void confirmaAgendamento(@PathVariable("uuid") String uuid, @RequestBody(required = true) ApiConfirmarAgendaRequest request) {
    agendaEntregaService.confirmaAgenda(uuid, request);
  }

  @PostMapping(value = "/agendamentos/{uuid}/cancelar", produces = "application/json")
  @ApiOperation(value = "Cancela um agendamento manualmente", tags = "Agendamentos")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Sucesso")
  })
  public void cancelaAgendamento(@PathVariable("uuid") String uuid, @RequestBody(required = true) ApiCancelarAgendaRequest request) {
    agendaEntregaService.cancelaAgenda(uuid, request);
  }

  @GetMapping(value = "/queue/{queue}", produces = "text/plain")
  @ApiOperation(value = "Retorna o número de mensagens pendentes na fila ou -1 se não existe", tags = "Admin")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Sucesso")
  })
  public Integer queueSize(@PathVariable("queue") String queue) {
    Properties queueProperties = rabbitAdmin.getQueueProperties(queue);
    if (queueProperties == null) {
      return -1;
    }
    return (Integer) rabbitAdmin.getQueueProperties(queue).get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
  }

  @PostMapping(value = "/schedulers/divergencias", produces = "application/json")
  @ApiOperation(value = "Aciona o scheduler de divergencias manualmente", tags = "Agendamentos")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Sucesso")
  })
  public void schedulerDivergencias() {
    divergenciasService.processaDivergencias();
  }

  @PostMapping(value = "/schedulers/pedidos", produces = "application/json")
  @ApiOperation(value = "Aciona o scheduler de pedidos manualmente", tags = "Pedidos")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Sucesso")
  })
  public void schedulerPedidos() {
    pedidosService.processaPedidos();
  }

  @PostMapping(value = "/pedidos", produces = "application/json")
  @ApiOperation(value = "Importa um pedido manualmente", tags = "Pedidos")
  @ApiResponses({
    @ApiResponse(code = 200, message = "Sucesso")
  })
  public PedidoPedido importaPedido(@RequestBody(required = true) PedidoImportarPedidoRequest pedido) {
    return pedidosService.importaPedido(pedido);
  }

}
