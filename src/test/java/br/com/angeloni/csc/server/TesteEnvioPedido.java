package br.com.angeloni.csc.server;

import br.com.angeloni.csc.pasoe.generated.dto.AgendamentoTempoCerto;
import br.com.angeloni.csc.pasoe.generated.dto.AgendamentoTempoCertoDsAgendamentos;
import br.com.angeloni.csc.pasoe.generated.dto.AgendamentoTempoCertoDsAgendamentosAgendamento;
import br.com.angeloni.csc.pasoe.generated.dto.DocumentosTempoCerto;
import br.com.angeloni.csc.server.service.AgendaEntregaService;
import com.agendarentrega.agendas.generated.dto.AgendarentregapedidoPedido;
import com.agendarentrega.agendas.generated.dto.ComagendarentreganfeNotaFiscal;
import com.agendarentrega.agendas.generated.dto.RecebimentoapiAgenda;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

//@RunWith(SpringRunner.class)
//@SpringBootTest
public class TesteEnvioPedido {

    @Autowired
    private AgendaEntregaService agendaEntregaService;

//    @Test
    public void executarBacaSemPedido() throws IOException {

        RecebimentoapiAgenda agenda = agendaEntregaService.getAgenda("c3c44fe0-c73d-4747-b99c-5e7ad9764f03");

        AgendamentoTempoCerto body = new AgendamentoTempoCerto();
        AgendamentoTempoCertoDsAgendamentos list = new AgendamentoTempoCertoDsAgendamentos();
        AgendamentoTempoCertoDsAgendamentosAgendamento item = new AgendamentoTempoCertoDsAgendamentosAgendamento();

        String cnpjEstabelecimento = agenda.getDestinatario().getLocal().getEntidade().getCpfCnpjMatriz();
        String cnpjTransportadora = agenda.getRemetente().getEntidade().getCpfCnpjMatriz();
        String nomeTransportadora = agenda.getRemetente().getEntidade().getNomeFantasia();
        if (agenda.getNotas() != null && !agenda.getNotas().isEmpty()) {
            cnpjEstabelecimento = agenda.getNotas().get(0).getDestinatario().getCpfCnpj();
            cnpjTransportadora = agenda.getNotas().get(0).getEmitente().getCpfCnpj();
            nomeTransportadora = agenda.getNotas().get(0).getEmitente().getNomeFantasia();
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

        agenda.setPedidos(null);

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

        System.out.println(body.toString());
    }

//    @Test
    public void executarBacaComMesmoPedido() throws IOException {

        RecebimentoapiAgenda agenda = agendaEntregaService.getAgenda("c3c44fe0-c73d-4747-b99c-5e7ad9764f03");

        AgendamentoTempoCerto body = new AgendamentoTempoCerto();
        AgendamentoTempoCertoDsAgendamentos list = new AgendamentoTempoCertoDsAgendamentos();
        AgendamentoTempoCertoDsAgendamentosAgendamento item = new AgendamentoTempoCertoDsAgendamentosAgendamento();

        String cnpjEstabelecimento = agenda.getDestinatario().getLocal().getEntidade().getCpfCnpjMatriz();
        String cnpjTransportadora = agenda.getRemetente().getEntidade().getCpfCnpjMatriz();
        String nomeTransportadora = agenda.getRemetente().getEntidade().getNomeFantasia();
        if (agenda.getNotas() != null && !agenda.getNotas().isEmpty()) {
            cnpjEstabelecimento = agenda.getNotas().get(0).getDestinatario().getCpfCnpj();
            cnpjTransportadora = agenda.getNotas().get(0).getEmitente().getCpfCnpj();
            nomeTransportadora = agenda.getNotas().get(0).getEmitente().getNomeFantasia();
        }

        item.setCnpjEstabelecimento(cnpjEstabelecimento);
        //TODO: Verificar erro de CNPJ inválido no PASOE!
        //item.setCnpjTransportadora(cnpjTransportadora);
        item.setNomeTransportadora(nomeTransportadora);
        item.setDataPrevista(AgendaEntregaService.parseStringDate(agenda.getSolicitacao().getData()));
        item.setHoraInicialPrevista(agenda.getSolicitacao().getHoraInicial());
        item.setHoraFinalPrevista(agenda.getSolicitacao().getHoraFinal());
        item.setPaletizada(agenda.getCarga().isPaletizada());
        item.setQuantidadeVolumes(agenda.getCarga().getQtdVolumes());
        item.setIdAgenda(agenda.getUuid());

//        agenda.setPedidos(null);

        List<ComagendarentreganfeNotaFiscal> nota2 = new ArrayList<>();

        ComagendarentreganfeNotaFiscal notaV2 = agenda.getNotasV2().get(0);
        notaV2.setChave("123");
        nota2.add(notaV2);

        ComagendarentreganfeNotaFiscal notaV3 = agenda.getNotasV2().get(0);
        notaV3.setChave("1234");
        nota2.add(notaV3);

        agenda.setNotasV2(nota2);

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

        System.out.println(body.toString());
    }
}
