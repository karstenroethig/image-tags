package karstenroethig.imagetags.webapp.service.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import karstenroethig.imagetags.webapp.model.domain.Album;
import karstenroethig.imagetags.webapp.model.dto.AlbumDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.repository.AlbumRepository;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.validation.ValidationException;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;

@Service
@Transactional
public class AlbumServiceImpl
{
	@Autowired private ImageServiceImpl imageService;

	@Autowired private AlbumRepository albumRepository;

	public AlbumDto create()
	{
		return new AlbumDto();
	}

	public ValidationResult validate(AlbumDto album)
	{
		ValidationResult result = new ValidationResult();

		if (album == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		result.add(validateUniqueness(album));

		return result;
	}

	private void checkValidation(AlbumDto album)
	{
		ValidationResult result = validate(album);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	private ValidationResult validateUniqueness(AlbumDto album)
	{
		ValidationResult result = new ValidationResult();

		Album existing = albumRepository.findOneByNameIgnoreCase(album.getName()).orElse(null);
		if (existing != null
				&& (album.getId() == null
				|| !existing.getId().equals(album.getId())))
			result.addError("name", MessageKeyEnum.COMMON_VALIDATION_ALREADY_EXISTS);

		return result;
	}

	public AlbumDto save(AlbumDto albumDto)
	{
		checkValidation(albumDto);

		Album album = new Album();
		merge(album, albumDto);

		return transform(albumRepository.save(album));
	}

	public AlbumDto update(AlbumDto albumDto)
	{
		checkValidation(albumDto);

		Album album = albumRepository.findById(albumDto.getId()).orElse(null);
		if (album == null)
			return null;

		merge(album, albumDto);

		return transform(albumRepository.save(album));
	}

	public ValidationResult validateDelete(AlbumDto album)
	{
		ValidationResult result = new ValidationResult();

		if (album == null)
		{
			result.addError(MessageKeyEnum.COMMON_VALIDATION_OBJECT_CANNOT_BE_EMPTY);
			return result;
		}

		ImageSearchDto imageSearch = new ImageSearchDto();
		imageSearch.setAlbum(album);
		long totalImagesUsedBy = imageService.countBySearchParams(imageSearch);
		if (totalImagesUsedBy > 0)
			result.addError(MessageKeyEnum.ALBUM_DELETE_INVALID_STILL_IN_USE_BY_IMAGES, totalImagesUsedBy);

		return result;
	}

	private void checkValidationDelete(AlbumDto album)
	{
		ValidationResult result = validateDelete(album);
		if (result.hasErrors())
			throw new ValidationException(result);
	}

	public boolean delete(Long id)
	{
		Album album = albumRepository.findById(id).orElse(null);
		if (album == null)
			return false;

		AlbumDto albumDto = transform(album);
		checkValidationDelete(albumDto);

		albumRepository.delete(album);

		return true;
	}

	public long count()
	{
		return albumRepository.count();
	}

	public List<AlbumDto> findAll()
	{
		Page<Album> page = albumRepository.findAll(Pageable.unpaged());
		Page<AlbumDto> pageDto = page.map(this::transform);
		return pageDto.getContent();
	}

	public Page<AlbumDto> findAll(Pageable pageable)
	{
		Page<Album> page = albumRepository.findAll(pageable);
		return page.map(this::transform);
	}

	public AlbumDto find(Long id)
	{
		return transform(albumRepository.findById(id).orElse(null));
	}

	private Album merge(Album album, AlbumDto albumDto)
	{
		if (album == null || albumDto == null )
			return null;

		album.setName(albumDto.getName());
		album.setAuthor(albumDto.getAuthor());

		return album;
	}

	protected AlbumDto transform(Album album)
	{
		if (album == null)
			return null;

		AlbumDto albumDto = new AlbumDto();

		albumDto.setId(album.getId());
		albumDto.setName(album.getName());
		albumDto.setAuthor(album.getAuthor());

		return albumDto;
	}

	protected Album transform(AlbumDto albumDto)
	{
		if (albumDto == null || albumDto.getId() == null)
			return null;

		return albumRepository.findById(albumDto.getId()).orElse(null);
	}

	protected boolean equals(AlbumDto album1, AlbumDto album2)
	{
		if (album1 == null && album2 == null)
			return true;

		if (album1 == null || album2 == null)
			return false;

		return Objects.equals(album1.getId(), album2.getId());
	}

	public Album findOrCreate(String name, String author)
	{
		Album album = albumRepository.findOneByNameIgnoreCase(name).orElse(null);

		if (album != null)
			return album;

		album = new Album();
		album.setName(name);
		album.setAuthor(author);

		return albumRepository.save(album);
	}

	public AlbumDto findOrCreateDto(String name, String author)
	{
		Album album = findOrCreate(name, author);
		return transform(album);
	}
}
