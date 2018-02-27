package karstenroethig.imagetags.webapp.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import karstenroethig.imagetags.webapp.dto.ImagesSearchDto;
import karstenroethig.imagetags.webapp.dto.TagDto;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@ComponentScan
@Controller
@RequestMapping( UrlMappings.CONTROLLER_IMAGES )
public class ImagesController
{
	@Autowired
	private ImageServiceImpl imageService;

	@Autowired
	private TagServiceImpl tagService;

	@Autowired
	private ImagesSearchBean imagesSearchBean;

	@RequestMapping(method = RequestMethod.GET)
	public String images(Model model)
	{
		return executeNewSearch(new ImagesSearchDto());
	}

	@RequestMapping(
		value = "/search",
		method = RequestMethod.POST
	)
	public String search(@ModelAttribute("searchParams") @Valid ImagesSearchDto searchParams, BindingResult bindingResult,
		RedirectAttributes redirectAttributes, Model model)
	{
		return executeNewSearch(searchParams);
	}

	@RequestMapping(
		value = "/tag/{id}",
		method = RequestMethod.GET
	)
	public String tag(@PathVariable("id") Long tagId, Model model)
	{
		TagDto searchTag = tagService.findTag(tagId);

		List<TagDto> searchTags = new ArrayList<>();
		searchTags.add(searchTag);

		ImagesSearchDto searchParams = new ImagesSearchDto();
		searchParams.setTags(searchTags);

		return executeNewSearch(searchParams);
	}

	@RequestMapping(
		value = "/page/{page}",
		method = RequestMethod.GET
	)
	public String page(@PathVariable("page") Integer page, Model model)
	{
		if (imagesSearchBean.getSearchParams() == null)
		{
			executeNewSearch(new ImagesSearchDto());
		}

		imagesSearchBean.switchToCurrentPage(page);

		model.addAttribute("allTags", tagService.getAllTagsByType());
		model.addAttribute("searchParams", imagesSearchBean.getSearchParams());
		model.addAttribute("imagesPage", imagesSearchBean.getCurrentImagesPage());
		model.addAttribute("image", imageService.findImage(imagesSearchBean.getImageIdForCurrentPage()));

		return ViewEnum.IMAGES.getViewName();
	}

	@RequestMapping(
		value = UrlMappings.ACTION_DELETE,
		method = RequestMethod.GET
	)
	public String delete(@PathVariable("id") Long imageId, Model model)
	{
		imageService.deleteImage(imageId);

		imagesSearchBean.removeImageFormSearchResult(imageId);

		return UrlMappings.redirect(UrlMappings.CONTROLLER_IMAGES, "/page/" + imagesSearchBean.getCurrentPage());
	}

	@RequestMapping(
		value = UrlMappings.ACTION_SHOW,
		method = RequestMethod.GET
	)
	public ResponseEntity<byte[]> show(@PathVariable("id") Long imageId) throws IOException
	{
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl(CacheControl.noCache().getHeaderValue());

		byte[] data = imageService.getImageData(imageId);
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(data, headers, HttpStatus.OK);

		return responseEntity;
	}

	@RequestMapping(
		value = UrlMappings.ACTION_UPDATE,
		method = RequestMethod.POST
	)
	public String update(@ModelAttribute("image") @Valid ImageDto image, BindingResult bindingResult, Model model)
	{
		imageService.editImage(image);

		return UrlMappings.redirect(UrlMappings.CONTROLLER_IMAGES, "/page/" + imagesSearchBean.getCurrentPage());
	}

	private String executeNewSearch(ImagesSearchDto searchParams)
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

		return UrlMappings.redirect(UrlMappings.CONTROLLER_IMAGES, "/page/1");
	}

	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException {
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Image %s does not exist.", ex.getMessage()));
	}

	@ExceptionHandler(IOException.class)
	void handleIOException(HttpServletResponse response, NotFoundException ex) throws IOException {
		log.error("Error on loading image", ex);
		response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error on loading image.");
	}
}
