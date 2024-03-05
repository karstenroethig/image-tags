package karstenroethig.imagetags.webapp.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.controller.util.AttributeNames;
import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.model.domain.Album_;
import karstenroethig.imagetags.webapp.model.domain.Image_;
import karstenroethig.imagetags.webapp.model.dto.AlbumDto;
import karstenroethig.imagetags.webapp.model.dto.ImageAlbumPageUpdateDto;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.service.impl.AlbumServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.Messages;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_ALBUM)
public class AlbumController extends AbstractController
{
	@Autowired private AlbumServiceImpl albumService;
	@Autowired private ImageServiceImpl imageService;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = Album_.NAME) Pageable pageable)
	{
		Page<AlbumDto> resultsPage = albumService.findAll(pageable);
		addPagingAttributes(model, resultsPage);

		return ViewEnum.ALBUM_LIST.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_SHOW)
	public String show(@PathVariable("id") Long id,
		@RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
		Model model)
	{
		AlbumDto album = albumService.find(id);
		if (album == null)
			throw new NotFoundException(String.valueOf(id));
		model.addAttribute(AttributeNames.ALBUM, album);

		ImageSearchDto imageSearch = new ImageSearchDto();
		imageSearch.setAlbum(album);
		Pageable pageRequest = PageRequest.of(page, 1, Direction.ASC, Image_.ALBUM_PAGE);
		Page<ImageDto> resultsPage = imageService.findBySearchParams(imageSearch, pageRequest);
		if (resultsPage != null && resultsPage.hasContent())
		{
			model.addAttribute(AttributeNames.PAGE, resultsPage);
			model.addAttribute(AttributeNames.IMAGE, resultsPage.getContent().stream().findFirst().orElse(null));
		}

		return ViewEnum.ALBUM_SHOW.getViewName();
	}

	@PostMapping(value = "/update-image/{id}")
	public String updateImage(
		@PathVariable("id") Long id,
		@ModelAttribute(AttributeNames.IMAGE) @Valid ImageAlbumPageUpdateDto imageAlbumPageUpdate,
		@RequestParam(name = "page", required = false) Integer page,
		BindingResult bindingResult, final RedirectAttributes redirectAttributes, Model model)
	{
		AlbumDto album = albumService.find(id);
		if (album == null)
			throw new NotFoundException(String.valueOf(id));

		Long imageId = imageAlbumPageUpdate.getId();
		ImageDto image = imageService.find(imageId);
		if (image == null)
			throw new NotFoundException(String.valueOf(imageId));

		image.setAlbumPage(imageAlbumPageUpdate.getAlbumPage());

		if (!validate(image, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.IMAGE_UPDATE_INVALID));
			return ViewEnum.ALBUM_SHOW.getViewName();
		}

		ImageDto updatedImage = imageService.update(image);
		if (updatedImage != null)
			return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_ALBUM, UrlMappings.ACTION_SHOW, id)
						+ String.format("?page=%s", page);

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.IMAGE_UPDATE_ERROR));
		return ViewEnum.ALBUM_SHOW.getViewName();
	}

	@GetMapping(value = "/remove/{id}/{imageId}")
	public String remove(@PathVariable("id") Long id, @PathVariable("imageId") Long imageId,
		@RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
		final RedirectAttributes redirectAttributes, Model model)
	{
		AlbumDto album = albumService.find(id);
		if (album == null)
			throw new NotFoundException(String.valueOf(id));

		ImageDto image = imageService.find(imageId);
		if (image == null)
			throw new NotFoundException(String.valueOf(imageId));

		image.setAlbum(null);
		image.setAlbumPage(null);
		imageService.update(image);

		return UrlMappings.redirectWithId(UrlMappings.CONTROLLER_ALBUM, UrlMappings.ACTION_SHOW, id)
					+ String.format("?page=%s", page);
	}

	@GetMapping(value = UrlMappings.ACTION_CREATE)
	public String create(Model model)
	{
		model.addAttribute(AttributeNames.ALBUM, albumService.create());
		return ViewEnum.ALBUM_CREATE.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_EDIT)
	public String edit(@PathVariable("id") Long id, Model model)
	{
		AlbumDto album = albumService.find(id);
		if (album == null)
			throw new NotFoundException(String.valueOf(id));

		model.addAttribute(AttributeNames.ALBUM, album);
		return ViewEnum.ALBUM_EDIT.getViewName();
	}

	@GetMapping(value = UrlMappings.ACTION_DELETE)
	public String delete(@PathVariable("id") Long id, final RedirectAttributes redirectAttributes, Model model)
	{
		AlbumDto album = albumService.find(id);
		if (album == null)
			throw new NotFoundException(String.valueOf(id));

		ValidationResult validationResult = albumService.validateDelete(album);
		if (validationResult.hasErrors())
		{
			addValidationMessagesToRedirectAttributes(MessageKeyEnum.ALBUM_DELETE_INVALID, validationResult.getErrors(), redirectAttributes);
		}
		else if (albumService.delete(id))
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.ALBUM_DELETE_SUCCESS, album.getName()));
		}
		else
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithError(MessageKeyEnum.ALBUM_DELETE_ERROR, album.getName()));
		}

		return UrlMappings.redirect(UrlMappings.CONTROLLER_ALBUM, UrlMappings.ACTION_LIST);
	}

	@PostMapping(value = UrlMappings.ACTION_SAVE)
	public String save(@ModelAttribute(AttributeNames.ALBUM) @Valid AlbumDto album, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(album, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.ALBUM_SAVE_INVALID));
			return ViewEnum.ALBUM_CREATE.getViewName();
		}

		AlbumDto savedAlbum = albumService.save(album);
		if (savedAlbum != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.ALBUM_SAVE_SUCCESS, album.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_ALBUM, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.ALBUM_SAVE_ERROR));
		return ViewEnum.ALBUM_CREATE.getViewName();
	}

	@PostMapping(value = UrlMappings.ACTION_UPDATE)
	public String update(@ModelAttribute(AttributeNames.ALBUM) @Valid AlbumDto album, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model)
	{
		if (!validate(album, bindingResult))
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.ALBUM_UPDATE_INVALID));
			return ViewEnum.ALBUM_EDIT.getViewName();
		}

		AlbumDto updatedAlbum = albumService.update(album);
		if (updatedAlbum != null)
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES,
					Messages.createWithSuccess(MessageKeyEnum.ALBUM_UPDATE_SUCCESS, album.getName()));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_ALBUM, UrlMappings.ACTION_LIST);
		}

		model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.ALBUM_UPDATE_ERROR));
		return ViewEnum.ALBUM_EDIT.getViewName();
	}

	private boolean validate(AlbumDto album, BindingResult bindingResult)
	{
		ValidationResult validationResult = albumService.validate(album);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	private boolean validate(ImageDto image, BindingResult bindingResult)
	{
		ValidationResult validationResult = imageService.validate(image);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		return !bindingResult.hasErrors() && !validationResult.hasErrors();
	}

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Album %s does not exist.", ex.getMessage()));
	}
}
