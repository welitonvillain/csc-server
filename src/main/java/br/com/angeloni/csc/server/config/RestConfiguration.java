package br.com.angeloni.csc.server.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Tag;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTags;
import org.springframework.boot.actuate.metrics.web.client.RestTemplateExchangeTagsProvider;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class RestConfiguration {

  @Bean
  public RestTemplate restTemplate(RestTemplateBuilder builder) {
    return builder
      .setConnectTimeout(Duration.ofSeconds(360))
      .setReadTimeout(Duration.ofSeconds(360))
      .build();
  }

  @Bean
  public RestTemplateExchangeTagsProvider restMetricsTags() {
    return (urlTemplate, request, response) -> {
      List<Tag> tags = new ArrayList<>();
      tags.add(RestTemplateExchangeTags.method(request));
      tags.add(Tag.of("uri", request.getURI().toString()
        .replace("?" + request.getURI().getRawQuery(), "")));
      tags.add(RestTemplateExchangeTags.status(response));
      return tags;
    };
  }

  @Bean
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(final ObjectMapper objectMapper) {
    MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
    jsonConverter.setObjectMapper(objectMapper);
    jsonConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
    return jsonConverter;
  }

}
