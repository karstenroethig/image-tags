package karstenroethig.imagetags.webapp.model.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)

@Entity
@Table(name = "image")
public class Image extends AbstractEntityId
{
	@Column(name = "title", length = 191, nullable = false)
	private String title;

	@Column(name = "description", nullable = true)
	private String description;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "image_tag", joinColumns = { @JoinColumn(name = "image_id") }, inverseJoinColumns = { @JoinColumn(name = "tag_id") })
	private Set<Tag> tags = new HashSet<>();

	public void addTag(Tag tag)
	{
		tags.add(tag);
	}

	public void removeTag(Tag tag)
	{
		tags.remove(tag);
	}
}
