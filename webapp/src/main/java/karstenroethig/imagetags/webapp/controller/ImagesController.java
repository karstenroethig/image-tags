package karstenroethig.imagetags.webapp.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import karstenroethig.imagetags.webapp.controller.exceptions.NotFoundException;
import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@ComponentScan
@Controller
@RequestMapping( UrlMappings.CONTROLLER_IMAGES )
public class ImagesController
{
	@Autowired
	ImageServiceImpl imageService;

	@Autowired
	TagServiceImpl tagService;

	@RequestMapping(
		method = RequestMethod.GET
	)
	public String images(Model model)
	{
		model.addAttribute("allTags", tagService.getAllTagsByType());
		model.addAttribute("imagesPage", imageService.findImages(null));

		return ViewEnum.IMAGES.getViewName();
	}

	@RequestMapping(
		value = UrlMappings.ACTION_SHOW,
		method = RequestMethod.GET
	)
	public ResponseEntity<byte[]> imageData(@PathVariable("id") Long imageId) throws IOException
	{
		HttpHeaders headers = new HttpHeaders();
		headers.setCacheControl(CacheControl.noCache().getHeaderValue());

		byte[] data = imageService.getImageData(imageId);
		ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(data, headers, HttpStatus.OK);

		return responseEntity;
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
