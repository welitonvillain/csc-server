package br.com.angeloni.csc.server.service;

import br.com.angeloni.csc.server.broker.Queues;
import br.com.angeloni.csc.server.model.AgendamentoMessage;
import br.com.angeloni.csc.server.model.DownloadItemMessage;
import br.com.angeloni.csc.server.model.DownloadMessage;
import com.agendarentrega.agendas.generated.dto.ApiAgendaDocumento;
import com.agendarentrega.agendas.generated.dto.NotafiscalnfeNotaFiscal;
import com.agendarentrega.agendas.generated.dto.RecebimentoapiAgenda;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class AgendamentosService {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private NotificationsService notificationsService;

  @Autowired
  private AgendaEntregaService agendaEntregaService;

  @Autowired
  private PasoeService pasoeService;

  @Autowired
  private MetricsService metricsService;

  public void recebeAgendamento(final AgendamentoMessage agendamento) {
    rabbitTemplate.convertAndSend(Queues.AGENDAMENTOS, agendamento);
    metricsService.getAgendamentosRecebidos().increment();
    log.info("Agendamento recebido. UUID: {}", agendamento.getUuid());
  }

  public void enfileirarDownload(final RecebimentoapiAgenda agenda) {
    log.info("Enfileirando download de documentos... UUID: {}", agenda.getUuid());
    List<DownloadItemMessage> lista = new ArrayList<>();
    List<NotafiscalnfeNotaFiscal> nfes = agenda.getNotas();
    if (nfes != null && !nfes.isEmpty()) {
      for (NotafiscalnfeNotaFiscal nfe : nfes) {
        DownloadItemMessage download = DownloadItemMessage.builder()
          .chaveNfe(nfe.getChave())
          .tipo("NF")
          .data(AgendaEntregaService.parseStringDate(nfe.getDataEmissao()))
          .build();
        lista.add(download);
      }
    }
    List<ApiAgendaDocumento> documentos = agenda.getDocumentos();
    if (documentos != null && !documentos.isEmpty()) {
      for (ApiAgendaDocumento documento : documentos) {
        DownloadItemMessage download = DownloadItemMessage.builder()
          .documentoUuid(documento.getUuid())
          .tipo(documento.getTipo().getChave())
          .data(AgendaEntregaService.parseStringDate(documento.getTipo().getDataCadastro()))
          .build();
        lista.add(download);
      }
    }
    if (!lista.isEmpty()) {
      log.info("Documentos pendentes para download: {},  UUID: {}", lista.size(), agenda.getUuid());
      DownloadMessage message = DownloadMessage.builder().agendamentoUuid(agenda.getUuid()).items(lista).build();
      rabbitTemplate.convertAndSend(Queues.DOWNLOADS, message);
    } else {
      log.info("Nenhum documento pendente para download. UUID: {}", agenda.getUuid());
    }
  }

  public void processaAgendamento(final AgendamentoMessage agendamento) {
    RecebimentoapiAgenda agenda = agendaEntregaService.getAgenda(agendamento.getUuid());
    if (agenda == null) {
      notificationsService.notifyError("Agendamento não encontrado! UUID: " + agendamento.getUuid());
      return;
    }
    try {
      log.info("Criando agendamento... UUID: {}", agenda.getUuid());
      pasoeService.criarAgendamento(agenda);
      enfileirarDownload(agenda);
      metricsService.getAgendamentosCriados().increment();
    } catch (Exception ex) {
      metricsService.getAgendamentosErro().increment();
      notificationsService.notifyError("Erro ao criar agendamento! UUID: " + agenda.getUuid(), ex);
      throw ex;
    }
  }

}
