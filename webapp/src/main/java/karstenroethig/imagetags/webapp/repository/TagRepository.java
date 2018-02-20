package karstenroethig.imagetags.webapp.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import karstenroethig.imagetags.webapp.domain.Tag;

public interface TagRepository extends CrudRepository<Tag,Long>
{
	List<Tag> findByNameIgnoreCase(String name);
}
