package com.springapp.mvc;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/")
public class HelloController {

  // Получение значений "системных" свойств. Речь идет о томкате и свойствах в файле catalina.proprties
  // Получаем влоб, да
  @Value("#{systemProperties['ucs.scc.pool.url']}")
  private String url;

  // Для того что бы этот подход роботал необходимо добавить в xml файл контекста строку
  // <context:property-placeholder /> и все, все системные параметры будут доступны.
  // Приятный бонус! Если определен локальный файл настроек, то параметры в нем будут перекрыты
  // системными параметрами.
  @Value("${ucs.scc.pool.url}")
  private String url_placeholder;

	@RequestMapping(method = RequestMethod.GET)
	public String printWelcome(ModelMap model) {
		model.addAttribute("message", "Hello world!");
    model.addAttribute("url", url);
    model.addAttribute("url_placeholder", url_placeholder);
		return "hello";
	}
}