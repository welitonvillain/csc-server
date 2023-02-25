package br.com.angeloni.csc.server.service;

import br.com.angeloni.csc.pasoe.generated.dto.Nota;
import com.agendarentrega.agendas.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;

@Service
@Slf4j
public class AgendaEntregaService {

  @Autowired
  private AgendaEntregaProperties agendaEntregaProperties;

  @Autowired
  private com.agendarentrega.agendas.generated.ApiClient agendaEntregaClient;

  @Autowired
  private com.agendarentrega.agendas.generated.api.AgendaServiceApi agendaEntregaApi;

  @Autowired
  private MetricsService metricsService;

  @PostConstruct
  private void configureClient() {
    agendaEntregaClient.setBasePath(UriComponentsBuilder.fromHttpUrl(agendaEntregaProperties.getBaseUrl())
      .path("/agenda-recebimento/v2/").toUriString());
    agendaEntregaClient.setApiKey(agendaEntregaProperties.getApiKey());
  }

  public RecebimentoapiAgenda getAgenda(final String uuid) {
    ApiBatchGetAgendasResponse response;
    try {
      response = agendaEntregaApi.batchGetAgendas(Arrays.asList(uuid), Arrays.asList("*"));
    } catch (HttpClientErrorException ex) {
      if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
        return null;
      }
      // Propagar exception para outros tipos de erros fazer o retry
      throw ex;
    }
    return response.getAgendas().get(uuid);
  }

  public static LocalDate parseStringDate(final String text) {
    if (text == null || text.isEmpty()) {
      return null;
    }
    if (text.contains("-")) {
      return LocalDate.parse(text.substring(0, 10));
    }
    return Instant.ofEpochSecond(Long.parseLong(text))
      .atZone(ZoneId.of("UTC")).toLocalDate();
  }

  public String createDownloadLink(final String documentoUuid) {
    ApiCreateDownloadLinkDocumentoResponse response = agendaEntregaApi.createDownloadLinkDocumento(documentoUuid);
    return response.getDownloadLink();
  }

  public void confirmaAgenda(final String uuid, final ApiConfirmarAgendaRequest request) {
    agendaEntregaApi.confirmarAgenda(uuid, request);
    metricsService.getAgendamentosConfirmados().increment();
  }

  public void cancelaAgenda(final String uuid, final ApiCancelarAgendaRequest request) {
    agendaEntregaApi.cancelarAgenda(uuid, request);
    metricsService.getAgendamentosCancelados().increment();
  }

  public void updateStatusRecebimentoNota(String agendaUuid, Nota nota) {
    ApiUpdateStatusRecebimentoNotaRequest request = new ApiUpdateStatusRecebimentoNotaRequest();
    ApiUpdateStatusRecebimentoNotaRequestAgendaAnotacao anotacao = new ApiUpdateStatusRecebimentoNotaRequestAgendaAnotacao();
    UpdateStatusRecebimentoNotaRequestAgendaAnotacaoTipoAnotacao tipoAnotacao = new UpdateStatusRecebimentoNotaRequestAgendaAnotacaoTipoAnotacao();
    tipoAnotacao.setUuid(agendaEntregaProperties.getUuidAnotacao());
    anotacao.setTipoAnotacao(tipoAnotacao);
    anotacao.setDescricao(nota.getMotivo());

    request.setAgendaUuid(agendaUuid);
    request.setChave(nota.getChaveDanfe());
    request.setStatusRecebimento(
            nota.getStatus() == Nota.StatusEnum.CAN
                    ? AgendaNotaStatusRecebimento.NAO_RECEBIDO
                    : AgendaNotaStatusRecebimento.RECEBIDO
    );
    request.setAnotacao(anotacao);

    agendaEntregaApi.updateStatusRecebimentoNota(agendaUuid, nota.getChaveDanfe(), request);
  }
}
