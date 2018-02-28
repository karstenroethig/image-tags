package karstenroethig.imagetags.webapp.controller;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.dto.api.TagApiDto;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;

@RestController
@RequestMapping(UrlMappings.CONTROLLER_API + UrlMappings.CONTROLLER_API_VERSION_1_0)
public class ApiController
{
	@Autowired
	private TagServiceImpl tagService;

	@RequestMapping(
		value = "/allTagsWithOccurrences",
		method = RequestMethod.GET,
		produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<Collection<TagApiDto>> allTagsWithOccurrences()
	{
		Collection<TagApiDto> tags = tagService.findAllTagsWithOccurrences();

		return new ResponseEntity<>(tags, HttpStatus.OK);
	}
}
