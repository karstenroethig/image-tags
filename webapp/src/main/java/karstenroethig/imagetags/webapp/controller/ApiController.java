package karstenroethig.imagetags.webapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import karstenroethig.imagetags.webapp.model.dto.AlbumDto;
import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.api.AlbumFilesizeApiDto;
import karstenroethig.imagetags.webapp.model.dto.api.AlbumUsageApiDto;
import karstenroethig.imagetags.webapp.model.dto.api.TagFilesizeApiDto;
import karstenroethig.imagetags.webapp.model.dto.api.TagUsageApiDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;

@RestController
@RequestMapping("/api/1.0")
public class ApiController
{
	@Autowired private ImageServiceImpl imageService;

	@GetMapping(value = "/tag/{id}/usage", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagUsageApiDto> fetchTagUsage(@PathVariable("id") Long tagId)
	{
		TagDto tag = new TagDto();
		tag.setId(tagId);
		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setTags(List.of(tag));

		Page<ImageDto> resultsPage = imageService.findBySearchParams(searchParams, Pageable.ofSize(1));

		TagUsageApiDto tagUsage = TagUsageApiDto.builder()
			.id(tagId)
			.usage(resultsPage.getTotalElements())
			.build();
		return new ResponseEntity<>(tagUsage, HttpStatus.OK);
	}

	@GetMapping(value = "/tag/{id}/filesize", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagFilesizeApiDto> fetchTagFilesize(@PathVariable("id") Long tagId)
	{
		TagDto tag = new TagDto();
		tag.setId(tagId);
		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setTags(List.of(tag));

		String filesize = imageService.findSizeBySearchParams(searchParams);

		TagFilesizeApiDto tagFilesize = TagFilesizeApiDto.builder()
			.id(tagId)
			.filesize(filesize)
			.build();
		return new ResponseEntity<>(tagFilesize, HttpStatus.OK);
	}

	@GetMapping(value = "/album/{id}/usage", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AlbumUsageApiDto> fetchAlbumUsage(@PathVariable("id") Long albumId)
	{
		AlbumDto album = new AlbumDto();
		album.setId(albumId);
		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setAlbum(album);

		Page<ImageDto> resultsPage = imageService.findBySearchParams(searchParams, Pageable.ofSize(1));

		AlbumUsageApiDto albumUsage = AlbumUsageApiDto.builder()
			.id(albumId)
			.usage(resultsPage.getTotalElements())
			.build();
		return new ResponseEntity<>(albumUsage, HttpStatus.OK);
	}

	@GetMapping(value = "/album/{id}/filesize", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AlbumFilesizeApiDto> fetchAlbumFilesize(@PathVariable("id") Long albumId)
	{
		AlbumDto album = new AlbumDto();
		album.setId(albumId);
		ImageSearchDto searchParams = new ImageSearchDto();
		searchParams.setAlbum(album);

		String filesize = imageService.findSizeBySearchParams(searchParams);

		AlbumFilesizeApiDto albumFilesize = AlbumFilesizeApiDto.builder()
			.id(albumId)
			.filesize(filesize)
			.build();
		return new ResponseEntity<>(albumFilesize, HttpStatus.OK);
	}
}
