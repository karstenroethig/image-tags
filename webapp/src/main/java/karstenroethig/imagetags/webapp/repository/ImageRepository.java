package karstenroethig.imagetags.webapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import karstenroethig.imagetags.webapp.model.domain.Image;

public interface ImageRepository extends JpaRepository<Image,Long>, JpaSpecificationExecutor<Image>
{
}
