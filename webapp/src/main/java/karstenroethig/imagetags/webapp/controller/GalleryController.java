package karstenroethig.imagetags.webapp.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import karstenroethig.imagetags.webapp.bean.ImagesSearchBean;
import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.dto.ImageDto;
import karstenroethig.imagetags.webapp.dto.ImageSearchDto;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_GALLERY)
public class GalleryController
{
	@Autowired
	private ImageServiceImpl imageService;

	@Autowired
	private TagServiceImpl tagService;

	@Autowired
	private ImagesSearchBean imagesSearchBean;

	@RequestMapping(
		value = UrlMappings.ACTION_LIST,
		method = RequestMethod.GET
	)
	public String list(Model model)
	{
		return executeNewSearch(model, new ImageSearchDto());
	}

	@RequestMapping(
		value = "/search",
		method = RequestMethod.POST
	)
	public String search(@ModelAttribute("searchParams") @Valid ImageSearchDto searchParams, BindingResult bindingResult,
		RedirectAttributes redirectAttributes, Model model)
	{
		return executeNewSearch(model, searchParams);
	}

	@RequestMapping(
		value = UrlMappings.ACTION_SHOW,
		method = RequestMethod.GET
	)
	public String show(@PathVariable("id") Long imageId, Model model)
	{
		ImageDto image = imageService.findImage(imageId);

		if (image == null)
		{
			throw new NotFoundException(String.valueOf(imageId));
		}

		model.addAttribute("allTags", tagService.getAllTagsByType());
		model.addAttribute("image", image);

		return ViewEnum.GALLERY_SHOW.getViewName();
	}

	@RequestMapping(
		value = UrlMappings.ACTION_DELETE,
		method = RequestMethod.GET
	)
	public String delete(@PathVariable("id") Long imageId, Model model)
	{
		imageService.deleteImage(imageId);

		return executeNewSearch(model, imagesSearchBean.getSearchParams());
	}

	@RequestMapping(
		value = UrlMappings.ACTION_UPDATE,
		method = RequestMethod.POST
	)
	public String update(@ModelAttribute("image") @Valid ImageDto image, BindingResult bindingResult, Model model)
	{
		imageService.editImage(image);

		return UrlMappings.redirect(UrlMappings.CONTROLLER_GALLERY, "/show/" + image.getId());
	}

	private String executeNewSearch(Model model, ImageSearchDto searchParams)
	{
		imagesSearchBean.clear();
		imagesSearchBean.setSearchParams(searchParams);

		List<Long> foundImagesIds = imageService.findImages(searchParams);

		if (foundImagesIds == null || foundImagesIds.isEmpty())
		{
			imagesSearchBean.setErrorMessageKey(MessageKeyEnum.IMAGES_SEARCH_EMPTY_RESULT.getKey());
		}
		else
		{
			imagesSearchBean.setSearchResultImageIds(foundImagesIds);
		}

		model.addAttribute("allTags", tagService.getAllTagsByType());
		model.addAttribute("searchParams", imagesSearchBean.getSearchParams());
		model.addAttribute("imageIds", foundImagesIds);

		return ViewEnum.GALLERY_LIST.getViewName();
	}

	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException {
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Image %s does not exist.", ex.getMessage()));
	}
}
