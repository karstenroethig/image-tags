package karstenroethig.imagetags.webapp.controller;

import java.io.IOException;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
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
import karstenroethig.imagetags.webapp.model.dto.ImageTagsUpdateDto;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.StorageServiceImpl;
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
	@Autowired private StorageServiceImpl storageService;

	@Autowired private ImageSearchBean imageSearchBean;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 30, sort = Image_.CREATED_DATETIME, direction = Direction.DESC) Pageable pageable)
	{
		Page<ImageDto> resultsPage = imageService.findBySearchParams(imageSearchBean.getImageSearchDto(), pageable);
		addPagingAttributes(model, resultsPage);

		model.addAttribute(AttributeNames.SEARCH_PARAMS, imageSearchBean.getImageSearchDto());
		addBasicAttributes(model);

		if (pageable.getPageSize() == 1 && resultsPage.hasContent())
		{
			ImageDto image = resultsPage.getContent().stream().findFirst().orElseThrow();
			model.addAttribute(AttributeNames.IMAGE, image);
			return ViewEnum.IMAGE_SHOW.getViewName();
		}

		return ViewEnum.IMAGE_LIST.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_SEARCH)
	public String search(@ModelAttribute(AttributeNames.IMAGE_SEARCH_PARAMS) ImageSearchDto imageSearchDto, Model model)
	{
		imageSearchBean.setImageSearchDto(imageSearchDto);
		return UrlMappings.redirect(UrlMappings.CONTROLLER_IMAGE, UrlMappings.ACTION_LIST);
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

	@GetMapping(value = UrlMappings.ACTION_CONTENT)
	@ResponseBody
	public ResponseEntity<Resource> content(@PathVariable("id") Long id,
		@RequestParam(name = "thumb", defaultValue = "false") boolean thumb,
		@RequestParam(name = "inline", defaultValue = "true") boolean inline, Model model) throws IOException
	{
		ImageDto image = imageService.find(id);
		if (image == null)
			throw new NotFoundException(String.valueOf(id));

		Resource fileResource = storageService.loadAsResource(image, thumb);
		return ResponseEntity
				.ok()
				.contentLength(fileResource.contentLength())
				.cacheControl(CacheControl.noCache())
				.header(HttpHeaders.CONTENT_DISPOSITION, (inline ? "inline" : "attachment") + "; filename=\"" + image.getStorageFilename() + "\"")
				.body(fileResource);
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		ImageDto image = imageService.find(id);
		if (image == null)
			throw new NotFoundException(String.valueOf(id));

		if (imageService.delete(id))
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.IMAGE_DELETE_SUCCESS));
		else
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
				Messages.createWithError(MessageKeyEnum.IMAGE_DELETE_ERROR));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_IMAGE, UrlMappings.ACTION_LIST);
	}

	@GetMapping(value = "/mark-deleted/{id}")
	public String markDeleted(@PathVariable("id") Long id,
		@RequestParam(name = "page", required = false) Integer page)
	{
		ImageDto image = imageService.find(id);
		if (image == null)
			throw new NotFoundException(String.valueOf(id));

		TagDto tag = tagService.findOrCreateDto(TagServiceImpl.TAG_DELETE);
		image.setTags(Set.of(tag));
		imageService.update(image);

		if (page == null)
			return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_IMAGE, UrlMappings.ACTION_SHOW, id);
		else
			return UrlMappings.redirect(UrlMappings.CONTROLLER_IMAGE, UrlMappings.ACTION_LIST)
				+ String.format("?page=%s&size=1", page);
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.IMAGE) @Valid ImageTagsUpdateDto imageTagsUpdate,
		@RequestParam(name = "page", required = false) Integer page,
		BindingResult bindingResult, final RedirectAttributes redirectAttributes, Model model)
	{
		Long id = imageTagsUpdate.getId();
		ImageDto image = imageService.find(id);
		if (image == null)
			throw new NotFoundException(String.valueOf(id));

		image.setTags(imageTagsUpdate.getTags());

		if (!validate(image, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.IMAGE_UPDATE_INVALID));
			addBasicAttributes(model);
			return ViewEnum.IMAGE_SHOW.getViewName();
		}

		ImageDto updatedImage = imageService.update(image);
		if (updatedImage != null)
		{
//			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
//					Messages.createWithSuccess(MessageKeyEnum.IMAGE_UPDATE_SUCCESS));
			if (page == null)
				return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_IMAGE, UrlMappings.ACTION_SHOW, updatedImage.getId());
			else
				return UrlMappings.redirect(UrlMappings.CONTROLLER_IMAGE, UrlMappings.ACTION_LIST)
					+ String.format("?page=%s&size=1", page);
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
