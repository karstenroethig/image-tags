package karstenroethig.imagetags.webapp.controller.global;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import karstenroethig.imagetags.webapp.bean.search.ImageSearchBean;
import karstenroethig.imagetags.webapp.controller.util.AttributeNames;
import karstenroethig.imagetags.webapp.controller.util.TemplateDateUtils;
import karstenroethig.imagetags.webapp.controller.util.TemplateTextUtils;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;

@ControllerAdvice
public class GlobalControllerAdvice
{
	@Autowired private ImageSearchBean imageSearchBean;

	@Autowired private TagServiceImpl tagService;

	@ModelAttribute(AttributeNames.DATE_UTILS)
	public TemplateDateUtils getDateUtils()
	{
		return TemplateDateUtils.INSTANCE;
	}

	@ModelAttribute(AttributeNames.TEXT_UTILS)
	public TemplateTextUtils getTextUtils()
	{
		return TemplateTextUtils.INSTANCE;
	}

	@ModelAttribute(AttributeNames.IMAGE_SEARCH_PARAMS)
	public ImageSearchDto getImageSearchParams()
	{
		return imageSearchBean.getImageSearchDto();
	}

	@ModelAttribute(AttributeNames.ALL_TAGS)
	public List<TagDto> getAllTags()
	{
		return tagService.findAll();
	}

	@InitBinder
	public void initBinder(WebDataBinder binder)
	{
		StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);
		binder.registerCustomEditor(String.class, stringTrimmerEditor);
	}
}
