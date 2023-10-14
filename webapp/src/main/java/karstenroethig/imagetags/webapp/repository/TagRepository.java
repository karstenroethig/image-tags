package karstenroethig.imagetags.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.imagetags.webapp.model.domain.Tag;

public interface TagRepository extends JpaRepository<Tag,Long>, JpaSpecificationExecutor<Tag>
{
	Optional<Tag> findOneByNameIgnoreCase(String name);
}
