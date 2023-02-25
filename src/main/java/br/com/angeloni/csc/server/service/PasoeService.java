package br.com.angeloni.csc.server.service;

import br.com.angeloni.csc.pasoe.generated.dto.*;
import com.agendarentrega.agendas.generated.dto.AgendarentregapedidoPedido;
import com.agendarentrega.agendas.generated.dto.ComagendarentreganfeNotaFiscal;
import com.agendarentrega.agendas.generated.dto.RecebimentoapiAgenda;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

@Service
public class PasoeService {

  @Autowired
  private PasoeProperties pasoeProperties;

  @Autowired
  private br.com.angeloni.csc.pasoe.generated.ApiClient pasoeClient;

  @Autowired
  private br.com.angeloni.csc.pasoe.generated.api.TempoCertoApi pasoeApi;

  @PostConstruct
  private void configureClient() {
    pasoeClient.setBasePath(pasoeProperties.getBaseUrl());
  }

  public void criarAgendamento(final RecebimentoapiAgenda agenda) {
    AgendamentoTempoCerto body = new AgendamentoTempoCerto();
    AgendamentoTempoCertoDsAgendamentos list = new AgendamentoTempoCertoDsAgendamentos();
    AgendamentoTempoCertoDsAgendamentosAgendamento item = new AgendamentoTempoCertoDsAgendamentosAgendamento();

    String cnpjEstabelecimento = agenda.getDestinatario().getLocal().getEntidade().getCpfCnpjMatriz();
    String cnpjTransportadora = agenda.getRemetente().getEntidade().getCpfCnpjMatriz();
    String nomeTransportadora = agenda.getRemetente().getEntidade().getNomeFantasia();
    if (agenda.getNotas() != null && !agenda.getNotas().isEmpty()) {
      cnpjEstabelecimento = agenda.getNotas().get(0).getDestinatario().getCpfCnpj();
//      cnpjTransportadora = agenda.getNotas().get(0).getEmitente().getCpfCnpj();
//      nomeTransportadora = agenda.getNotas().get(0).getEmitente().getNomeFantasia();
    }

    item.setCnpjEstabelecimento(cnpjEstabelecimento);
    //TODO: Verificar erro de CNPJ inválido no PASOE!
    item.setCnpjTransportadora(cnpjTransportadora);
    item.setNomeTransportadora(nomeTransportadora);
    item.setDataPrevista(AgendaEntregaService.parseStringDate(agenda.getSolicitacao().getData()));
    item.setHoraInicialPrevista(agenda.getSolicitacao().getHoraInicial());
    item.setHoraFinalPrevista(agenda.getSolicitacao().getHoraFinal());
    item.setPaletizada(agenda.getCarga().isPaletizada());
    item.setQuantidadeVolumes(agenda.getCarga().getQtdVolumes());
    item.setIdAgenda(agenda.getUuid());
    item.setTicket(agenda.getTicket().toString());

    List<DocumentosTempoCerto> notas = new ArrayList<>();
    if (agenda.getNotasV2() != null) {
      for (ComagendarentreganfeNotaFiscal nota : agenda.getNotasV2()) {
        DocumentosTempoCerto documento = new DocumentosTempoCerto();

        /* feature merge nota com pedido */
        String pedidoNotaPasoe = "";
        if(nonNull(nota.getPedidos())) {
          if(nonNull(agenda.getPedidos())){
            for (String pedidoNota : nota.getPedidos()) {
              for(AgendarentregapedidoPedido pedido : agenda.getPedidos()){
                if(pedido.getCodigo().equalsIgnoreCase(pedidoNota)) {
                  pedidoNotaPasoe = pedido.getCodigo();
                  break;
                }

                if(!pedidoNotaPasoe.equals(""))
                  break;
              }
            }
          }
        }

        if(pedidoNotaPasoe.equals("")) {
          pedidoNotaPasoe = nonNull(nota.getPedidos()) ? nota.getPedidos().get(0) : "";
        }
        /***/

        documento.setCodPedido(pedidoNotaPasoe);
        documento.setChaveDanfe(nota.getChave());
        documento.setDataEmissao(AgendaEntregaService.parseStringDate(nota.getDataEmissao()));
        notas.add(documento);
      }
      item.setNotas(notas);
    }

    list.addAgendamentoItem(item);
    body.setDsAgendamentos(list);

    RetornoAtualizaPedidoTempoCertoDsRetornoRetorno retorno = pasoeApi.criarAgendamento(body)
      .getDsRetorno().getRetorno().get(0);
    if (!retorno.getDescricao().toLowerCase().contains("sucesso")) {
      throw new IllegalStateException(retorno.getDescricao());
    }
  }

  public List<BuscarPedidosTempoCertoResponse1> getPedidosPendentes() {
    return pasoeApi.buscarPedidos().getDsPedidos().getPedidos();
  }

  public void setPedidoSincronizado(final BuscarPedidosTempoCertoResponse1 pedido, LocalDate data) {
    AtualizaPedidoTempoCerto request = new AtualizaPedidoTempoCerto();
    AtualizaPedidoTempoCertoDsAtualizaPedido body = new AtualizaPedidoTempoCertoDsAtualizaPedido();
    AtualizaPedidoTempoCertoDsAtualizaPedidoAtualizaPedido pedidoAtualizado = new AtualizaPedidoTempoCertoDsAtualizaPedidoAtualizaPedido();
    pedidoAtualizado.setDataIntegracao(data);
    body.setAtualizaPedido(Arrays.asList(pedidoAtualizado));
    request.setDsAtualizaPedido(body);
    RetornoAtualizaPedidoTempoCertoDsRetornoRetorno retorno = pasoeApi.atualizarPedido(pedido.getCnpjEstabelecimento(),
      pedido.getCodigoPedido(), request).getDsRetorno().getRetorno().get(0);
    if (!retorno.getDescricao().toLowerCase().contains("sucesso")) {
      throw new IllegalStateException(retorno.getDescricao());
    }
  }

  public void criaDocumentos(final String agendaUuid, final List<DocumentosTempoCerto> documentos) {
    DocumentoTempoCerto documentoTempoCerto = new DocumentoTempoCerto();
    DocumentoTempoCertoDsDocumentos documentosTempoCerto = new DocumentoTempoCertoDsDocumentos();
    DocumentoTempoCertoDsDocumentosDocumento agendaDocumento = new DocumentoTempoCertoDsDocumentosDocumento();
    agendaDocumento.setIdAgenda(agendaUuid);
    agendaDocumento.setDocumentos(documentos);
    documentosTempoCerto.addDocumentoItem(agendaDocumento);
    documentoTempoCerto.setDsDocumentos(documentosTempoCerto);

    RetornoAtualizaPedidoTempoCertoDsRetornoRetorno retorno = pasoeApi.criarDocumento(documentoTempoCerto)
      .getDsRetorno().getRetorno().get(0);
    if (!retorno.getDescricao().toLowerCase().contains("sucesso")) {
      throw new IllegalStateException(retorno.getDescricao());
    }
  }

  public void setAgendamentoSincronizado(final String agendaUuid) {
    AtualizaAgendamentoTempoCerto body = new AtualizaAgendamentoTempoCerto();
    AtualizaAgendamentoTempoCertoDsAtualizaAgendamento ds = new AtualizaAgendamentoTempoCertoDsAtualizaAgendamento();
    AtualizaAgendamentoTempoCertoDsAtualizaAgendamentoAtualizaAgendamentos item = new AtualizaAgendamentoTempoCertoDsAtualizaAgendamentoAtualizaAgendamentos();
    item.setSincronizado(true);
    ds.setAtualizaAgendamentos(Arrays.asList(item));
    body.setDsAtualizaAgendamento(ds);
    RetornoAtualizaPedidoTempoCertoDsRetornoRetorno retorno = pasoeApi.atualizarAgendamento(agendaUuid, body).getDsRetorno().getRetorno().get(0);
    if (!retorno.getDescricao().toLowerCase().contains("sucesso")) {
      throw new IllegalStateException(retorno.getDescricao());
    }
  }

  public List<BuscarAgendamentosTempoCertoResponseDsAgendamentosStatusTtagendamentos> listaDivergencias() {
    BuscarAgendamentosTempoCertoResponseDsAgendamentosStatus response = pasoeApi.buscarAgendamentos("P")
      .getDsAgendamentosStatus();
    if (response.getTtAgendamentos() == null) {
      return Collections.emptyList();
    }
    return response.getTtAgendamentos();
  }
}