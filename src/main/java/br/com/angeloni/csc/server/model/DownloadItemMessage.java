package br.com.angeloni.csc.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadItemMessage {

  private String tipo;

  private LocalDate data;

  private String chaveNfe;

  private String documentoUuid;

}
