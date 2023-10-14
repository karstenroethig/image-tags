package karstenroethig.imagetags.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import karstenroethig.imagetags.webapp.model.domain.AbstractEntityId_;
import karstenroethig.imagetags.webapp.model.domain.Image;
import karstenroethig.imagetags.webapp.model.domain.Image_;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;

public class ImageSpecifications
{
	private ImageSpecifications() {}

	public static Specification<Image> matchesId(Long id)
	{
		return (Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
					cb.equal(root.get(AbstractEntityId_.id), id);
	}

	public static Specification<Image> matchesUniqueProperties(Long id, String title)
	{
		return (Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (id != null)
					restrictions.add(cb.notEqual(root.get(AbstractEntityId_.id), id));

				restrictions.add(cb.equal(cb.lower(root.get(Image_.title)), StringUtils.lowerCase(title)));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}

	public static Specification<Image> matchesSearchParam(ImageSearchDto imageSearchDto)
	{
		return (Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				addRestrictionsForText(root, cb, restrictions, imageSearchDto.getText());

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}

	private static void addRestrictionsForText(Root<Image> root, CriteriaBuilder cb, List<Predicate> restrictions, String text)
	{
		if (StringUtils.isBlank(text))
			return;

		restrictions.add(cb.or(
				cb.like(cb.lower(root.get(Image_.title)), "%" + StringUtils.lowerCase(text) + "%"),
				cb.like(cb.lower(root.get(Image_.description)), "%" + StringUtils.lowerCase(text) + "%")));
	}
}
