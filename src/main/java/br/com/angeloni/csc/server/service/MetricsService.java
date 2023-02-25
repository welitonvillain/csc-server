package br.com.angeloni.csc.server.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
@Getter
public class MetricsService {

  private MeterRegistry meterRegistry;

  private Counter agendamentosRecebidos;
  private Counter agendamentosCriados;
  private Counter agendamentosErro;
  private Counter agendamentosSincronizados;
  private Counter agendamentosCancelados;
  private Counter agendamentosConfirmados;

  private Counter downloadSucesso;
  private Counter downloadErro;

  private Counter notificacoesErro;

  private Counter pedidosImportadosSucesso;
  private Counter pedidosImportadosErro;

  private Counter schedulerPedidosSucesso;
  private Counter schedulerPedidosErro;

  private Counter schedulerDivergenciasSucesso;
  private Counter schedulerDivergenciasErro;

  public MetricsService(final MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @PostConstruct
  public void initMetrics() {
    log.info("Registrando métricas...");

    this.agendamentosRecebidos = Counter.builder("agendamentos")
      .tag("status", "recebido")
      .description("Status do agendamento")
      .register(meterRegistry);

    this.agendamentosCriados = Counter.builder("agendamentos")
      .tag("status", "criado")
      .description("Status do agendamento")
      .register(meterRegistry);

    this.agendamentosErro = Counter.builder("agendamentos")
      .tag("status", "erro")
      .description("Status do agendamento")
      .register(meterRegistry);

    this.agendamentosSincronizados = Counter.builder("agendamentos")
      .tag("status", "sincronizado")
      .description("Status do agendamento")
      .register(meterRegistry);

    this.agendamentosConfirmados = Counter.builder("agendamentos")
      .tag("status", "confirmado")
      .description("Status do agendamento")
      .register(meterRegistry);

    this.agendamentosCancelados = Counter.builder("agendamentos")
      .tag("status", "cancelado")
      .description("Status do agendamento")
      .register(meterRegistry);

    this.downloadSucesso = Counter.builder("downloads")
      .tag("status", "sucesso")
      .description("Status do download")
      .register(meterRegistry);

    this.downloadErro = Counter.builder("downloads")
      .tag("status", "erro")
      .description("Status do download")
      .register(meterRegistry);

    this.notificacoesErro = Counter.builder("notifications")
      .tag("status", "erro")
      .description("Status da notificação")
      .register(meterRegistry);

    this.pedidosImportadosSucesso = Counter.builder("pedidos")
      .tag("status", "importado")
      .description("Status do pedido")
      .register(meterRegistry);

    this.pedidosImportadosErro = Counter.builder("pedidos")
      .tag("status", "erro")
      .description("Status do pedido")
      .register(meterRegistry);

    this.schedulerDivergenciasSucesso = Counter.builder("jobs")
      .tag("name", "divergencias")
      .tag("status", "sucesso")
      .description("Status do job de divergencias")
      .register(meterRegistry);

    this.schedulerDivergenciasErro = Counter.builder("jobs")
      .tag("name", "divergencias")
      .tag("status", "erro")
      .description("Status do job de divergencias")
      .register(meterRegistry);

    this.schedulerPedidosSucesso = Counter.builder("jobs")
      .tag("name", "pedidos")
      .tag("status", "sucesso")
      .description("Status do job de pedidos")
      .register(meterRegistry);

    this.schedulerPedidosErro = Counter.builder("jobs")
      .tag("name", "pedidos")
      .tag("status", "erro")
      .description("Status do job de pedidos")
      .register(meterRegistry);
  }

}