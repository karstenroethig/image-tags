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
import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.controller.util.AttributeNames;
import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.model.domain.Album_;
import karstenroethig.imagetags.webapp.model.dto.AlbumDto;
import karstenroethig.imagetags.webapp.service.impl.AlbumServiceImpl;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.Messages;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_ALBUM)
public class AlbumController extends AbstractController
{
	@Autowired private AlbumServiceImpl albumService;

	@GetMapping(value = UrlMappings.ACTION_LIST)
	public String list(Model model, @PageableDefault(size = 20, sort = Album_.NAME) Pageable pageable)
	{
		Page<AlbumDto> resultsPage = albumService.findAll(pageable);
		addPagingAttributes(model, resultsPage);

		return ViewEnum.ALBUM_LIST.getViewName();
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

	@Override
	@ExceptionHandler(NotFoundException.class)
	void handleNotFoundException(HttpServletResponse response, NotFoundException ex) throws IOException
	{
		response.sendError(HttpStatus.NOT_FOUND.value(), String.format("Album %s does not exist.", ex.getMessage()));
	}
}
