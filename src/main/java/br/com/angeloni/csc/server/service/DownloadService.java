package br.com.angeloni.csc.server.service;

import br.com.angeloni.csc.pasoe.generated.dto.DocumentosTempoCerto;
import br.com.angeloni.csc.server.model.DownloadItemMessage;
import br.com.angeloni.csc.server.model.DownloadMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class DownloadService {

  @Autowired
  private NotificationsService notificationsService;

  @Autowired
  private AgendaEntregaService agendaEntregaService;

  @Autowired
  private NotaFiscalService notaFiscalService;

  @Autowired
  private FileServerProperties properties;

  @Autowired
  private PasoeService pasoeService;

  @Autowired
  private MetricsService metricsService;

  private File downloadFile(final String link) throws IOException {
    log.info("Realizando download do arquivo: {}", link);
    URL url = new URL(link);
    URLConnection connection = url.openConnection();
    connection.setConnectTimeout(10_000);
    connection.setReadTimeout(30_000);
    String extension = connection.getContentType() != null ? connection.getContentType().split("/")[1] : "bin";
    File tempFile = File.createTempFile("download", "." + extension);
    FileUtils.copyInputStreamToFile(connection.getInputStream(), tempFile);
    tempFile.deleteOnExit();
    return tempFile;
  }

  private String uploadFile(final File file, final String remoteFolder, final String remoteName) throws IOException {
    String protocolPrefix = properties.getProtocol() + "://";
    String credentials = properties.getUsername() != null && !properties.getUsername().isEmpty() ?
      properties.getUsername() + ":" + properties.getPassword() + "@" : "";
    String hostPort = properties.getPort() != null && properties.getPort() != 0 ?
      properties.getHost() + ":" + properties.getPort() : properties.getHost();
    String remoteFile = properties.getPath() + (properties.getPath().endsWith("/") ? "" : "/")
      + remoteFolder + "/" + remoteName + "." + FilenameUtils.getExtension(file.getName());

    FileSystemOptions opts = new FileSystemOptions();
    SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
    SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);

    FileSystemManager manager = VFS.getManager();
    try (FileObject local = manager.resolveFile(file.toURI());
         FileObject remote = manager.resolveFile(protocolPrefix + credentials + hostPort + remoteFile, opts))
    {
      if (remote.exists()) {
        log.info("Arquivo já existe no destino! {}", remoteFile);
      } else {
        log.info("Realizando upload do arquivo: {}", remoteFile);
        remote.copyFrom(local, Selectors.SELECT_SELF);
      }
    }
    return remoteFile;
  }

  public void processaDownload(final DownloadMessage message) {
    try {
      if (message.getItems() == null || message.getItems().isEmpty()) {
        log.info("Nenhum documento pendente para download.");
        return;
      }
      List<DocumentosTempoCerto> documentos = new ArrayList<>();
      for (DownloadItemMessage item : message.getItems()) {
        String link, name;
        if (item.getDocumentoUuid() != null) {
          log.info("Processando download do documento UUID: {} para o agendamento UUID: {}",
            item.getDocumentoUuid(), message.getAgendamentoUuid());
          link = agendaEntregaService.createDownloadLink(item.getDocumentoUuid());
          name = item.getDocumentoUuid();
        } else {
          log.info("Processando download da NFE chave: {} para o agendamento UUID: {}",
            item.getChaveNfe(), message.getAgendamentoUuid());
          link = notaFiscalService.downloadNfe(item.getChaveNfe());
          name = item.getChaveNfe();
        }
        File file = downloadFile(link);
        String remoteFile = uploadFile(file, message.getAgendamentoUuid(), name);

        DocumentosTempoCerto documentoTempoCerto = new DocumentosTempoCerto();
        documentoTempoCerto.setIdDocTempoCerto(item.getDocumentoUuid());
        documentoTempoCerto.setChaveDanfe(item.getChaveNfe());
        documentoTempoCerto.setDataEmissao(item.getData());
        documentoTempoCerto.setTipoDocumento(item.getTipo());
        documentoTempoCerto.setDocumento(remoteFile);
        documentos.add(documentoTempoCerto);
      }
      metricsService.getDownloadSucesso().increment();
      log.info("Download de documentos finalizado, sincronizando agendamento UUID: {}", message.getAgendamentoUuid());
      pasoeService.criaDocumentos(message.getAgendamentoUuid(), documentos);

      // REMOVIDO A CONFIRMAÇÃO DE AGENDAMENTO NESTE LOCAL A PEDIDO DE CASSIO, DURANTE HOMOLOGAÇÃO
      // pasoeService.setAgendamentoSincronizado(message.getAgendamentoUuid());
//      metricsService.getAgendamentosSincronizados().increment();
    } catch (Exception ex) {
      metricsService.getDownloadErro().increment();
      notificationsService.notifyError("Erro ao realizar download de documentos. UUID: "
        + message.getAgendamentoUuid(), ex);
      throw new IllegalArgumentException(ex);
    }
  }

}
