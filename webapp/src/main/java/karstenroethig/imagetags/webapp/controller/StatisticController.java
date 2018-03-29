package karstenroethig.imagetags.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.service.impl.StatisticInfoServiceImpl;

@ComponentScan
@Controller
public class StatisticController
{
	@Autowired
	private StatisticInfoServiceImpl statisticService;

	@RequestMapping(
		value = UrlMappings.STATISTIC,
		method = RequestMethod.GET
	)
	public String dashborad(Model model)
	{
		model.addAttribute("stats", statisticService.getStatistic());

		return ViewEnum.STATISTIC.getViewName();
	}
}
