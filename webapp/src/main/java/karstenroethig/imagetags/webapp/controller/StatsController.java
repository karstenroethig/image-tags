package karstenroethig.imagetags.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.model.enums.TagTypeEnum;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_STATS)
public class StatsController extends AbstractController
{
	@Autowired private TagServiceImpl tagService;

	@GetMapping(value = "/most-used-tags-person")
	public String mostUsedTagsPerson(Model model)
	{
		model.addAttribute("numberOfTags", tagService.count(TagTypeEnum.PERSON));

		return ViewEnum.STATS_MOST_USED_TAGS_PERSON.getViewName();
	}

	@GetMapping(value = "/most-used-tags-category")
	public String mostUsedTagsCategory(Model model)
	{
		model.addAttribute("numberOfTags", tagService.count(TagTypeEnum.CATEGORY));

		return ViewEnum.STATS_MOST_USED_TAGS_CATEGORY.getViewName();
	}

	@GetMapping(value = "/most-used-filesize-per-tag-person")
	public String mostUsedFilesizePerTagPerson(Model model)
	{
		model.addAttribute("numberOfTags", tagService.count(TagTypeEnum.PERSON));

		return ViewEnum.STATS_MOST_USED_FILESIZE_PER_TAG_PERSON.getViewName();
	}

	@GetMapping(value = "/most-used-filesize-per-tag-category")
	public String mostUsedFilesizePerTagCategory(Model model)
	{
		model.addAttribute("numberOfTags", tagService.count(TagTypeEnum.CATEGORY));

		return ViewEnum.STATS_MOST_USED_FILESIZE_PER_TAG_CATEGORY.getViewName();
	}
}
