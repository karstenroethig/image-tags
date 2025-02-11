package karstenroethig.imagetags.webapp.repository.specification;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import karstenroethig.imagetags.webapp.model.domain.Tag;
import karstenroethig.imagetags.webapp.model.domain.Tag_;
import karstenroethig.imagetags.webapp.model.enums.TagTypeEnum;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TagSpecifications
{
	public static Specification<Tag> matchesType(TagTypeEnum type)
	{
		return (Root<Tag> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
					cb.equal(root.get(Tag_.type), type);
	}
}
