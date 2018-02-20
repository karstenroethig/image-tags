package karstenroethig.imagetags.webapp.controller;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;

@ComponentScan
@Controller
public class DashboardController
{
	@RequestMapping(
		value = { UrlMappings.HOME, UrlMappings.DASHBOARD },
		method = RequestMethod.GET
	)
	public String dashborad( Model model )
	{
		return ViewEnum.DASHBOARD.getViewName();
	}
}
