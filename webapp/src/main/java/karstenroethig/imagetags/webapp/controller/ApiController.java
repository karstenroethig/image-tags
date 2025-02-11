package karstenroethig.imagetags.webapp.controller;

import java.util.ArrayList;
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

import karstenroethig.imagetags.webapp.model.dto.ImageDto;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.api.TagChartFilesizeApiDto;
import karstenroethig.imagetags.webapp.model.dto.api.TagChartUsageApiDto;
import karstenroethig.imagetags.webapp.model.dto.api.TagFilesizeApiDto;
import karstenroethig.imagetags.webapp.model.dto.api.TagUsageApiDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.model.enums.TagTypeEnum;
import karstenroethig.imagetags.webapp.service.impl.ImageServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.TagServiceImpl;
import karstenroethig.imagetags.webapp.util.FilesizeUtils;
import lombok.Builder;
import lombok.Getter;

@RestController
@RequestMapping("/api/1.0")
public class ApiController
{
	@Autowired private ImageServiceImpl imageService;
	@Autowired private TagServiceImpl tagService;

	@GetMapping(value = "/tag/chart/usage/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagChartUsageApiDto> fetchTagChartUsage(@PathVariable("type") TagTypeEnum type)
	{
		List<TagDto> tags = tagService.findAll(type);
		List<TagAmountDto> tagsUsage = new ArrayList<>();

		for (TagDto tag : tags)
		{
			ImageSearchDto searchParams = new ImageSearchDto();
			searchParams.setTags(List.of(tag));
			Page<ImageDto> resultsPage = imageService.findBySearchParams(searchParams, Pageable.ofSize(1));

			tagsUsage.add(
				TagAmountDto.builder()
					.name(tag.getName())
					.amount(resultsPage.getTotalElements())
					.build()
			);
		}

		List<String> names = tagsUsage.stream()
			.sorted((e1, e2) -> Long.compare(e1.getAmount(), e2.getAmount()))
			.map(TagAmountDto::getName)
			.toList();

		List<Long> usage = tagsUsage.stream()
			.sorted((e1, e2) -> Long.compare(e1.getAmount(), e2.getAmount()))
			.map(TagAmountDto::getAmount)
			.toList();

		return new ResponseEntity<>(
			TagChartUsageApiDto.builder()
				.names(names)
				.usage(usage)
				.build(),
			HttpStatus.OK);
	}

	@GetMapping(value = "/tag/chart/filesize/{type}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<TagChartFilesizeApiDto> fetchTagChartFilesize(@PathVariable("type") TagTypeEnum type)
	{
		List<TagDto> tags = tagService.findAll(type);
		List<TagAmountDto> tagsFilesize = new ArrayList<>();

		for (TagDto tag : tags)
		{
			ImageSearchDto searchParams = new ImageSearchDto();
			searchParams.setTags(List.of(tag));
			Long filesize = imageService.findSizeBySearchParams(searchParams);

			tagsFilesize.add(
				TagAmountDto.builder()
					.name(tag.getName())
					.amount(filesize)
					.build()
			);
		}

		List<String> names = tagsFilesize.stream()
			.sorted((e1, e2) -> Long.compare(e1.getAmount(), e2.getAmount()))
			.map(TagAmountDto::getName)
			.toList();

		List<Float> filesize = tagsFilesize.stream()
			.sorted((e1, e2) -> Long.compare(e1.getAmount(), e2.getAmount()))
			.map(tagAmount -> FilesizeUtils.bytesToMegabytes(tagAmount.getAmount()))
			.toList();

		return new ResponseEntity<>(
			TagChartFilesizeApiDto.builder()
				.names(names)
				.filesize(filesize)
				.build(),
			HttpStatus.OK);
	}

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

		String filesize = imageService.findSizeFormattedBySearchParams(searchParams);

		TagFilesizeApiDto tagFilesize = TagFilesizeApiDto.builder()
			.id(tagId)
			.filesize(filesize)
			.build();
		return new ResponseEntity<>(tagFilesize, HttpStatus.OK);
	}

	@Builder
	@Getter
	private static class TagAmountDto
	{
		private String name;
		private Long amount;
	}
}
