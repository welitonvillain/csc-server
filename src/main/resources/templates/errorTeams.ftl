<#ftl output_format="XML">
{
  "@type": "MessageCard",
  "@context": "http://schema.org/extensions",
  "themeColor": "D7000B",
  "summary": "Erro na integração com TempoCerto",
  "sections": [
    {
      "activityTitle": "Atenção – Erro na integração com TempoCerto!",
      "facts": [
        {
          "name": "Mensagem",
          "value": "${error.message!}"
        },
        {
          "name": "Detalhes",
          "value": "${error.detail!}"
        },
        {
          "name": "Trace ID",
          "value": "${traceId!}"
        }
       ],
      "markdown": true
    }
  ]
}
