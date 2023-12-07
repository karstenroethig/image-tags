package karstenroethig.imagetags.webapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.imagetags.webapp.model.domain.Storage;

public interface StorageRepository extends JpaRepository<Storage,Long>, JpaSpecificationExecutor<Storage>
{
	List<Storage> findBySizeLessThanOrderBySizeAsc(Long size);
}
