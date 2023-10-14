package karstenroethig.imagetags.webapp.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;

@ComponentScan
@Controller
public class DashboardController
{
	@GetMapping(value = {UrlMappings.HOME, UrlMappings.DASHBOARD})
	public String dashborad(Model model)
	{
		return ViewEnum.DASHBOARD.getViewName();
	}
}
