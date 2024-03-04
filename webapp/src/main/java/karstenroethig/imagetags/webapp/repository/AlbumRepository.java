package karstenroethig.imagetags.webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.imagetags.webapp.model.domain.Album;

public interface AlbumRepository extends JpaRepository<Album,Long>, JpaSpecificationExecutor<Album>
{
	Optional<Album> findOneByNameIgnoreCase(String name);
}
