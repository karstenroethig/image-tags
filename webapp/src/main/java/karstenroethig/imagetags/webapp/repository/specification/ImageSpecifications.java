package karstenroethig.imagetags.webapp.repository.specification;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import jakarta.persistence.criteria.Subquery;
import karstenroethig.imagetags.webapp.model.domain.AbstractEntityId_;
import karstenroethig.imagetags.webapp.model.domain.Image;
import karstenroethig.imagetags.webapp.model.domain.Image_;
import karstenroethig.imagetags.webapp.model.domain.Tag;
import karstenroethig.imagetags.webapp.model.dto.TagDto;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import karstenroethig.imagetags.webapp.model.enums.ImageNewTagStatusEnum;
import karstenroethig.imagetags.webapp.model.enums.ImageThumbStatusEnum;

public class ImageSpecifications
{
	private ImageSpecifications() {}

	public static Specification<Image> matchesId(Long id)
	{
		return (Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
					cb.equal(root.get(AbstractEntityId_.id), id);
	}

	public static Specification<Image> matchesUniqueProperties(Long id, String hash)
	{
		return (Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				if (id != null)
					restrictions.add(cb.notEqual(root.get(AbstractEntityId_.id), id));

				restrictions.add(cb.equal(cb.lower(root.get(Image_.hash)), StringUtils.lowerCase(hash)));

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}

	public static Specification<Image> matchesSearchParam(ImageSearchDto imageSearchDto)
	{
		return (Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb) ->
			{
				List<Predicate> restrictions = new ArrayList<>();

				addRestrictionsForTags(root, query, cb, restrictions, imageSearchDto.getTags());
				addRestrictionsForNewTagStatus(root, query, cb, restrictions, imageSearchDto.getNewTagStatus());
				addRestrictionsForThumbStatus(root, query, cb, restrictions, imageSearchDto.getThumbStatus());

				return cb.and(restrictions.toArray(new Predicate[] {}));
			};
	}

	private static void addRestrictionsForTags(Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Predicate> restrictions, List<TagDto> tags)
	{
		if (tags == null || tags.isEmpty())
			return;

		for (TagDto tag : tags)
		{
			Subquery<Long> sub = query.subquery(Long.class);
			Root<Tag> subRoot = sub.from(Tag.class);
			SetJoin<Image, Tag> subTags = root.join(Image_.tags);
			sub.select(subRoot.get(AbstractEntityId_.id));
			sub.where(cb.equal(subTags.get(AbstractEntityId_.id), tag.getId()));

			restrictions.add(cb.exists(sub));
		}
	}

	private static void addRestrictionsForNewTagStatus(Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Predicate> restrictions, ImageNewTagStatusEnum newTagStatus)
	{
		if (newTagStatus == null)
			return;

		restrictions.add(cb.equal(root.get(Image_.newTagStatus), newTagStatus));
	}

	private static void addRestrictionsForThumbStatus(Root<Image> root, CriteriaQuery<?> query, CriteriaBuilder cb, List<Predicate> restrictions, ImageThumbStatusEnum thumbStatus)
	{
		if (thumbStatus == null)
			return;

		restrictions.add(cb.equal(root.get(Image_.thumbStatus), thumbStatus));
	}
}
