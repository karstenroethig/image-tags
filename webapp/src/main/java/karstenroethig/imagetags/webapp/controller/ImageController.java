package karstenroethig.imagetags.webapp.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import karstenroethig.imagetags.webapp.bean.search.ImageSearchBean;
import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.controller.util.AttributeNames;
import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.model.domain.Image_;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.Messages;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_IMAGE)
public class ImageController extends AbstractController
{
	@Autowired private ImageServiceImpl imageService;
	@Autowired private TagServiceImpl tagService;

	@Autowired private ImageSearchBean imageSearchBean;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = Image_.TITLE) Pageable pageable)
	{
		Page<ImageDto> resultsPage = imageService.findBySearchParams(imageSearchBean.getImageSearchDto(), pageable);
		addPagingAttributes(model, resultsPage);

		model.addAttribute(AttributeNames.SEARCH_PARAMS, imageSearchBean.getImageSearchDto());
		addBasicAttributes(model);

		return ViewEnum.IMAGE_LIST.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@ModelAttribute(AttributeNames.IMAGE_SEARCH_PARAMS) ImageSearchDto imageSearchDto, Model model)
	{
		imageSearchBean.setImageSearchDto(imageSearchDto);
		return UrlMappings.redirect(UrlMappings.DASHBOARD);
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id, Model model)
	{
		ImageDto image = imageService.find(id);
		if (image == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.IMAGE, image);
		return ViewEnum.IMAGE_SHOW.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		ImageDto image = imageService.find(id);
		if (image == null)
			throw new NotFoundException(String.valueOf(id));

		if (imageService.delete(id))
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.IMAGE_DELETE_SUCCESS, image.getTitle()));
		else
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
				Messages.createWithError(MessageKeyEnum.IMAGE_DELETE_ERROR, image.getTitle()));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_IMAGE, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.IMAGE) @Valid ImageDto image, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(image, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.IMAGE_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.IMAGE_SHOW.getViewName();
		}

		ImageDto updatedImage = imageService.update(image);
		if (updatedImage != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.IMAGE_UPDATE_SUCCESS, image.getTitle()));
			return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_IMAGE, UrlMappings.ACTION_SHOW, updatedImage.getId());
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.IMAGE_UPDATE_ERROR));
		addBasicAttributes(model);
		return ViewEnum.IMAGE_SHOW.getViewName();
	}

	private boolean validate(ImageDto image, BindingResult bindingResult)
	{
		ValidationResult validationResult = imageService.validate(image);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	private void addBasicAttributes(Model model)
	{
		model.addAttribute(AttributeNames.ALL_TAGS, tagService.findAll(Pageable.unpaged()));
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Image %s does not exist.", ex.getMessage()));
	}
}
