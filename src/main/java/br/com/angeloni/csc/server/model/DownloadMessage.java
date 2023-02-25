package br.com.angeloni.csc.server.model;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadMessage {

  private String agendamentoUuid;

  @Singular
  private List<DownloadItemMessage> items;

}
