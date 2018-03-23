package karstenroethig.imagetags.webapp.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.imagetags.webapp.domain.Storage;

public interface StorageRepository extends CrudRepository<Storage,Long>
{
	List<Storage> findBySizeLessThanOrderBySizeAsc(Long size);
}
