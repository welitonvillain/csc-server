package br.com.angeloni.csc.server.job.divergencias;

import br.com.angeloni.csc.server.service.DivergenciasService;
import br.com.angeloni.csc.server.service.MetricsService;
import br.com.angeloni.csc.server.service.NotificationsService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@DisallowConcurrentExecution
public class DivergenciasJob extends QuartzJobBean {

  @Autowired
  private DivergenciasJobProperties jobProperties;

  @Autowired
  private NotificationsService notificationsService;

  @Autowired
  private DivergenciasService divergenciasService;

  @Autowired
  private MetricsService metricsService;

  @Override
  protected void executeInternal(final JobExecutionContext context) {
    if (!jobProperties.isEnabled()) {
      return;
    }

    log.info("-------------------- INICIO: DIVERGENCIAS --------------------");
    try {
      if (divergenciasService.processaDivergencias()) {
        metricsService.getSchedulerDivergenciasSucesso().increment();
      } else {
        metricsService.getSchedulerDivergenciasErro().increment();
      }
    } catch (Exception ex) {
      metricsService.getSchedulerDivergenciasErro().increment();
      notificationsService.notifyError("Erro ao processar divergencias pendentes", ex);
    }

    log.info("Horário da próxima execução das divergencias: {}", context.getNextFireTime());
    log.info("-------------------- FIM: DIVERGENCIAS -----------------------");
  }

}