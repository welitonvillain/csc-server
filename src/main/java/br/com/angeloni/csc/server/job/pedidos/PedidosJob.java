package br.com.angeloni.csc.server.job.pedidos;

import br.com.angeloni.csc.server.service.MetricsService;
import br.com.angeloni.csc.server.service.NotificationsService;
import br.com.angeloni.csc.server.service.PedidosService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@DisallowConcurrentExecution
public class PedidosJob extends QuartzJobBean {

  @Autowired
  private PedidosJobProperties jobProperties;

  @Autowired
  private NotificationsService notificationsService;

  @Autowired
  private PedidosService pedidosService;

  @Autowired
  private MetricsService metricsService;

  @Override
  protected void executeInternal(final JobExecutionContext context) {
    if (!jobProperties.isEnabled()) {
      return;
    }

    log.info("-------------------- INICIO: PEDIDOS --------------------");

    try {
      if (pedidosService.processaPedidos()) {
        metricsService.getSchedulerPedidosSucesso().increment();
      } else {
        metricsService.getSchedulerPedidosErro().increment();
      }
    } catch (Exception ex) {
      metricsService.getSchedulerPedidosErro().increment();
      notificationsService.notifyError("Erro ao processar pedidos pendentes", ex);
    }

    log.info("Horário da próxima execução dos pedidos: {}", context.getNextFireTime());
    log.info("-------------------- FIM: PEDIDOS -----------------------");
  }

}