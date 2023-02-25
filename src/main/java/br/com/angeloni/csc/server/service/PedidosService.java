package br.com.angeloni.csc.server.service;

import br.com.angeloni.csc.pasoe.generated.dto.BuscarPedidosItemImpostoTempoCertoResponse;
import br.com.angeloni.csc.pasoe.generated.dto.BuscarPedidosItemTempoCertoResponse;
import br.com.angeloni.csc.pasoe.generated.dto.BuscarPedidosTempoCertoResponse1;
import com.agendarentrega.pedidos.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PedidosService {

  @Autowired
  private NotificationsService notificationsService;

  @Autowired
  private AgendaEntregaProperties agendaEntregaProperties;

  @Autowired
  private com.agendarentrega.pedidos.generated.ApiClient pedidosClient;

  @Autowired
  private com.agendarentrega.pedidos.generated.api.PedidoServiceApi pedidosApi;

  @Autowired
  private PasoeService pasoeService;

  @Autowired
  private MetricsService metricsService;

  @PostConstruct
  private void configureClient() {
    pedidosClient.setBasePath(UriComponentsBuilder.fromHttpUrl(agendaEntregaProperties.getBaseUrl())
      .path("/pedido/").toUriString());
    pedidosClient.setApiKey(agendaEntregaProperties.getApiKey());
  }

  private ItemImpostosImposto getImposto(String codigo, List<BuscarPedidosItemImpostoTempoCertoResponse> lista) {
    BuscarPedidosItemImpostoTempoCertoResponse item = lista.stream()
      .filter(i -> i.getCodigoImposto().equalsIgnoreCase(codigo)).findFirst().orElse(null);
    if (item == null) {
      return null;
    }

    ItemImpostosImposto imposto = new ItemImpostosImposto();
    imposto.setAliquota(item.getAliquota());
    imposto.setValor(item.getValor());
    return imposto;
  }

  private PedidoStatusPedido statusPedido(Integer status) {
    for (PedidoStatusPedido b : PedidoStatusPedido.values()) {
      if (b.ordinal() == status) {
        return b;
      }
    }
    return null;
  }

  public boolean processaPedidos() {
    log.info("Buscando pedidos pendentes...");
    boolean sucesso = true;
    List<BuscarPedidosTempoCertoResponse1> pedidos = pasoeService.getPedidosPendentes();
    if (pedidos == null || pedidos.isEmpty()) {
      log.info("Nenhum pedido pendente.");
      return sucesso;
    }
    for (BuscarPedidosTempoCertoResponse1 pedido : pedidos) {
      PedidoImportarPedidoRequest pedidoTempo = new PedidoImportarPedidoRequest();
      try {
        log.info("Importanto pedido... Cod: " + pedido.getCodigoPedido() + ", CNPJ: " + pedido.getCnpjEstabelecimento());

        pedidoTempo.setCodigo(pedido.getCodigoPedido().toString());
        ImportarPedidoRequestFornecedor fornecedor = new ImportarPedidoRequestFornecedor();
//        fornecedor.setCnpj(pedido.getCnpjFornecedor());

        fornecedor.setIdentidade(pedido.getCnpjFornecedor());

        // feature trata produto rural
        if(pedido.getEhCPF().equalsIgnoreCase("sim"))
          fornecedor.setNomeCompleto(pedido.getNomeProdutorRural());

        pedidoTempo.setFornecedor(fornecedor);
        ImportarPedidoRequestEmpresa emitente = new ImportarPedidoRequestEmpresa();
        emitente.setCnpj(pedido.getCnpjEmitente());
        pedidoTempo.setEmitente(emitente);
        ImportarPedidoRequestEmpresa destinatario = new ImportarPedidoRequestEmpresa();
        destinatario.setCnpj(pedido.getCnpjDestinatario());
        pedidoTempo.setDestinatario(destinatario);
        pedidoTempo.setDataEmissao(LocalDateTime.of(pedido.getDataEmissao(), LocalTime.now()).atOffset(ZoneOffset.UTC)
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm")));
        pedidoTempo.setObservacoes(pedido.getObservacoes());
        PedidoImportarPedidoRequestEntrega entrega = new PedidoImportarPedidoRequestEntrega();
        entrega.setData(pedido.getEntregaData().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        entrega.setHora(LocalTime.of(Integer.parseInt(pedido.getEntregaHora()), 0)
          .format(DateTimeFormatter.ofPattern("hh:mm")));
        pedidoTempo.setEntrega(entrega);
        pedidoTempo.setStatus(statusPedido(pedido.getStatus()));

        List<ImportarPedidoRequestItem> items = new ArrayList<>();
        for (BuscarPedidosItemTempoCertoResponse item : pedido.getItens()) {
          ImportarPedidoRequestItem itemTempo = new ImportarPedidoRequestItem();
          itemTempo.setSequencia(item.getSequencia());
          itemTempo.setPrecoUnitario(item.getPrecoUnitario());
          itemTempo.setPrecoTotal(item.getPrecoTotal());
          itemTempo.setQuantidade(item.getQuantidade());
          ImportarPedidoRequestItemProduto produto = new ImportarPedidoRequestItemProduto();
          produto.setEan(item.getEan());
          produto.setDescricao(item.getDescricao());
          produto.setCodigoFornecedor(item.getCodigoFornecedor());
          produto.setCodigoDestinatario(item.getCodigoDestinatario());
          itemTempo.setProduto(produto);
          ItemVolume volume = new ItemVolume();
          volume.setTotal(item.getVolumeTotal());
          volume.setUnitario(item.getVolumeUnitario());
          itemTempo.setVolume(volume);
          itemTempo.setNcm(item.getNcm());
          ItemImpostos impostos = new ItemImpostos();
          impostos.setCofins(getImposto("COFINS", item.getImpostos()));
          impostos.setIcms(getImposto("ICMS", item.getImpostos()));
          impostos.setIcmsst(getImposto("ICMSST", item.getImpostos()));
          impostos.setIpi(getImposto("IPI", item.getImpostos()));
          impostos.setPis(getImposto("PIS", item.getImpostos()));
          itemTempo.setImpostos(impostos);
          items.add(itemTempo);
        }
        pedidoTempo.setItems(items);

        PedidoPedido pedidoImportado = pedidosApi.importarPedido(pedidoTempo).getPedido();
        pasoeService.setPedidoSincronizado(pedido, LocalDate.now());
        metricsService.getPedidosImportadosSucesso().increment();
        log.info("Pedido importado! UUID: " + pedidoImportado.getUuid());
      } catch (Exception ex) {
        sucesso = false;
        metricsService.getPedidosImportadosErro().increment();
        notificationsService.notifyError("Erro ao importar pedido! " +
          "Código: " + pedido.getCodigoPedido() + ", CNPJ: " + pedido.getCnpjEstabelecimento(), ex);
      }
    }
    return sucesso;
  }

  public PedidoPedido importaPedido(final PedidoImportarPedidoRequest pedido) {
    return pedidosApi.importarPedido(pedido).getPedido();
  }

}
