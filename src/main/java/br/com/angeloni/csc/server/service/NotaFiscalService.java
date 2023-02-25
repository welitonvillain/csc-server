package br.com.angeloni.csc.server.service;

import com.agendarentrega.notas.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class NotaFiscalService {

  @Autowired
  private AgendaEntregaProperties agendaEntregaProperties;

  @Autowired
  private com.agendarentrega.notas.generated.ApiClient notasClient;

  @Autowired
  private com.agendarentrega.notas.generated.api.NotaFiscalServiceApi notasApi;

  @PostConstruct
  private void configureClient() {
    notasClient.setBasePath(UriComponentsBuilder.fromHttpUrl(agendaEntregaProperties.getBaseUrl())
      .path("/nota-fiscal/v2/").toUriString());
    notasClient.setApiKey(agendaEntregaProperties.getApiKey());
  }

  public String downloadNfe(final String chaveNfe) {
    NfeCreateDownloadLinkXMLResponse response = notasApi.createDownloadLinkXML(chaveNfe);
    return response.getUrl();
  }

  public NfeAprovarRecebimentoResponse aprovarNfe(String chaveNfe) {
    NfeAprovarRecebimentoRequest request = new NfeAprovarRecebimentoRequest();
    request.setChave(chaveNfe);

    return notasApi.aprovarRecebimento(chaveNfe, request);
  }

  public NfeRecusarRecebimentoResponse recusarNfe(String chaveNfe, String motivo) {
    NfeRecusarRecebimentoRequest request = new NfeRecusarRecebimentoRequest();
    request.setChave(chaveNfe);
    request.setMotivo(motivo);

    return notasApi.recusarRecebimento(chaveNfe, request);
  }

}
