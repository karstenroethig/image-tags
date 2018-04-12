package karstenroethig.imagetags.webapp.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.imagetags.webapp.domain.Image;

public interface ImageRepository extends CrudRepository<Image,Long>
{
	List<Image> findByHashIgnoreCase(String hash);

	List<Image> findByThumbStatus(Integer thumbStatus);

	List<Image> findByResolutionWidthOrResolutionHeight(Integer resolutionWidth, Integer resolutionHeight);
}
