package br.com.angeloni.csc.server.service;

import br.com.angeloni.csc.pasoe.generated.dto.BuscarAgendamentosTempoCertoResponseDsAgendamentosStatusTtagendamentos;
import br.com.angeloni.csc.pasoe.generated.dto.Nota;
import com.agendarentrega.agendas.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
public class DivergenciasService {

  @Autowired
  private NotificationsService notificationsService;

  @Autowired
  private AgendaEntregaService agendaEntregaService;

  @Autowired
  private NotaFiscalService notaFiscalService;

  @Autowired
  private PasoeService pasoeService;

  public boolean processaDivergencias() {
    log.info("Buscando divergencias...");
    boolean sucesso = true;
    List<BuscarAgendamentosTempoCertoResponseDsAgendamentosStatusTtagendamentos> list = pasoeService.listaDivergencias();
    if (list.isEmpty()) {
      log.info("Nenhuma divergencia encontrada!");
      return sucesso;
    }
    for (BuscarAgendamentosTempoCertoResponseDsAgendamentosStatusTtagendamentos agenda : list) {
      try{
        if (agenda.getStatus() == BuscarAgendamentosTempoCertoResponseDsAgendamentosStatusTtagendamentos.StatusEnum.CA) {
          log.info("Cancelando agendamento... UUID: {}", agenda.getIdAgenda());
          ApiCancelarAgendaRequest request = new ApiCancelarAgendaRequest();
          request.setUuid(agenda.getIdAgenda());
          String observacoes = agenda.getObservacoes();
          if (observacoes == null || observacoes.isEmpty()) {
            observacoes = "Divergencia";
          }
          request.setObservacoes(observacoes);

          agendaEntregaService.cancelaAgenda(agenda.getIdAgenda(), request);
          pasoeService.setAgendamentoSincronizado(agenda.getIdAgenda());
          processaNotas(agenda.getIdAgenda(), agenda.getNotas());
        } else {
          log.info("Confirmando agendamento... UUID: {}", agenda.getIdAgenda());
          ApiConfirmarAgendaRequest request = new ApiConfirmarAgendaRequest();
          RecebimentoapiAgenda recebimentoapiAgenda = agendaEntregaService.getAgenda(agenda.getIdAgenda());
          for (RecebimentoapiDoca doca : recebimentoapiAgenda.getDocas()) {
            request.addDocasItem(doca.getUuid());
          }
          ApiAgendaPeriodo periodo = new ApiAgendaPeriodo();
          periodo.setData(recebimentoapiAgenda.getSolicitacao().getData());
          periodo.setHoraInicio(recebimentoapiAgenda.getSolicitacao().getHoraInicial());
          request.setPeriodo(periodo);
          request.setUuid(agenda.getIdAgenda());
          String observacoes = agenda.getObservacoes();
          if (observacoes == null || observacoes.isEmpty()) {
            observacoes = "Divergencia";
          }
          request.setObservacoes(observacoes);
          request.setTipoConfirmacao(AgendaTipoConfirmacaoParcial.FISCAL);

          agendaEntregaService.confirmaAgenda(agenda.getIdAgenda(), request);
          pasoeService.setAgendamentoSincronizado(agenda.getIdAgenda());
          processaNotas(agenda.getIdAgenda(), agenda.getNotas());
        }
      } catch (Exception ex) {
        notificationsService.notifyError("Erro durante processo de divergencias! UUID: " + agenda.getIdAgenda(), ex);
        sucesso = false;
      }
    }
    return sucesso;
  }

  private void processaNotas(String agendaUuid, List<Nota> notas) {
    for (Nota nota : notas) {
      if (nota.getStatus().equals(Nota.StatusEnum.CON)) {
        notaFiscalService.aprovarNfe(nota.getChaveDanfe());
      }

      if (nota.getStatus().equals(Nota.StatusEnum.CAN)) {
        notaFiscalService.recusarNfe(nota.getChaveDanfe(), nota.getMotivo());
        agendaEntregaService.updateStatusRecebimentoNota(agendaUuid, nota);
      }
    }
  }
}
