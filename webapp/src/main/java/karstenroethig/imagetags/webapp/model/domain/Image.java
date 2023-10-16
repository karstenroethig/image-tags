package karstenroethig.imagetags.webapp.model.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import karstenroethig.imagetags.webapp.model.enums.ImageResolutionStatusEnum;
import karstenroethig.imagetags.webapp.model.enums.ImageThumbStatusEnum;
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
	@JoinColumn(name = "storage_id")
	@ManyToOne(optional = true)
	private Storage storage;

	@Column(name = "storage_key", length = 100, nullable = false)
	private String storageKey;

	@Column(name = "file_extension", length = 50, nullable = false)
	private String extension;

	@Column(name = "file_size", nullable = false)
	private Long size;

	@Column(name = "hash", length = 256, nullable = false)
	private String hash;

	@Column(name = "import_path", length = 256, nullable = true)
	private String importPath;

	@Column(name = "thumb_status", length = 191, nullable = false)
	@Enumerated(EnumType.STRING)
	private ImageThumbStatusEnum thumbStatus;

	@Column(name = "resolution_width", nullable = false)
	private Integer resolutionWidth;

	@Column(name = "resolution_height", nullable = false)
	private Integer resolutionHeight;

	@Column(name = "resolution_status", length = 191, nullable = false)
	@Enumerated(EnumType.STRING)
	private ImageResolutionStatusEnum resolutionStatus;

	@Column(name = "created_datetime", nullable = false)
	private LocalDateTime createdDatetime;

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
