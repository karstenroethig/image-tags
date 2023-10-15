package karstenroethig.imagetags.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.model.dto.info.ServerInfoDto;
import karstenroethig.imagetags.webapp.service.impl.ServerInfoServiceImpl;

@ComponentScan
@Controller
public class ServerInfoController
{
	@Autowired ServerInfoServiceImpl serverInfoService;

	@GetMapping(value = {UrlMappings.SERVER_INFO})
	public String serverInfo(Model model)
	{
		ServerInfoDto serverInfo = serverInfoService.getInfo();

		model.addAttribute("serverInfo", serverInfo);

		return ViewEnum.SERVER_INFO.getViewName();
	}
}
