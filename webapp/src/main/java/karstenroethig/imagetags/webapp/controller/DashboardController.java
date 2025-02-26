package karstenroethig.imagetags.webapp.controller;

import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.model.enums.TagTypeEnum;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;

@ComponentScan
@Controller
public class DashboardController
{
	@Autowired private ImageServiceImpl imageService;
	@Autowired private TagServiceImpl tagService;

	@GetMapping(value = {UrlMappings.HOME, UrlMappings.DASHBOARD})
	public String dashborad(Model model)
	{
		addAttributesForTagCards(model);
		addAttributesForStatsCard(model);

		return ViewEnum.DASHBOARD.getViewName();
	}

	private void addAttributesForTagCards(Model model)
	{
		List<TagDto> allCategoryTags = tagService.findAll(TagTypeEnum.CATEGORY).stream()
			.sorted(Comparator.comparing(TagDto::getName))
			.toList();

		List<TagDto> allPersonTags = tagService.findAll(TagTypeEnum.PERSON).stream()
			.sorted(Comparator.comparing(TagDto::getName))
			.toList();

		model.addAttribute("allCategoryTags", allCategoryTags);
		model.addAttribute("allPersonTags", allPersonTags);
	}

	private void addAttributesForStatsCard(Model model)
	{
		long totalImages = imageService.count();

		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setTags(List.of(tagService.findOrCreateDto(TagServiceImpl.TAG_NEW)));
		long untaggedImages = imageService.countBySearchParams(searchParams);

		String totalFileSize = imageService.findSizeFormattedBySearchParams(new ImageSearchDto());

		model.addAttribute("statsTotalImages", totalImages);
		model.addAttribute("statsUntaggedImages", untaggedImages);
		model.addAttribute("statsTotalFileSize", totalFileSize);
	}
}
