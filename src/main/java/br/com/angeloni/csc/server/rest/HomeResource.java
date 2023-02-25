package br.com.angeloni.csc.server.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/")
@ApiIgnore
public class HomeResource {

  @GetMapping
  public RedirectView apiDocs() {
    RedirectView redirectView = new RedirectView();
    redirectView.setUrl("/swagger-ui/");
    return redirectView;
  }

}
