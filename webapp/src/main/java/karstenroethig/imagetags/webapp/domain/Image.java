package karstenroethig.imagetags.webapp.domain;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import karstenroethig.imagetags.webapp.domain.enums.ImageResolutionStatusEnum;
import karstenroethig.imagetags.webapp.domain.enums.ImageThumbStatusEnum;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString

@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint( columnNames = {"id" } ),
		@UniqueConstraint( columnNames = {"hash" } )
	}
)
public class Image
{
	@Column(
		length = 18,
		nullable = false,
		unique = true
	)
	@GeneratedValue( strategy = GenerationType.IDENTITY )
	@Id
	private Long id;

	@JoinColumn(name = "storage_id")
	@ManyToOne(optional = true)
	private Storage storage;

	@ManyToMany(
		fetch = FetchType.LAZY
	)
	@JoinTable(
		name = "Image_Tag",
		joinColumns = { @JoinColumn(name = "image_id") },
		inverseJoinColumns = { @JoinColumn(name = "tag_id") }
	)
	private Set<Tag> tags = new HashSet<>();

	@Column(
		name = "file_extension",
		length = 50,
		nullable = false
	)
	private String extension;

	@Column(
		name = "file_size",
		nullable = false
	)
	private Long size;

	@Column(
		length = 256,
		nullable = false
	)
	private String hash;

	@Column(
		name = "import_path",
		length = 256,
		nullable = true
	)
	private String importPath;

	@Column(
		name = "thumb_status",
		nullable = false
	)
	private Integer thumbStatus;

	@Column(
		name = "resolution_width",
		nullable = false
	)
	private Integer resolutionWidth;

	@Column(
		name = "resolution_height",
		nullable = false
	)
	private Integer resolutionHeight;

	@Column(
		name = "resolution_status",
		nullable = false
	)
	private Integer resolutionStatus;

	@Column(
		name = "created_date",
		nullable = true
	)
	@Type(
		type = "org.hibernate.type.LocalDateTimeType"
	)
	private LocalDateTime createdDate;

	public void addTag(Tag tag)
	{
		tags.add(tag);
	}

	public void clearTags()
	{
		tags.clear();
	}

	@Transient
	public ImageThumbStatusEnum getThumbStatusEnum()
	{
		return ImageThumbStatusEnum.getStatusForKey(thumbStatus);
	}

	public void setThumbStatusEnum(ImageThumbStatusEnum imageThumbStatusEnum)
	{
		if (imageThumbStatusEnum != null)
		{
			setThumbStatus(imageThumbStatusEnum.getKey());
		}
		else
		{
			setThumbStatus(ImageThumbStatusEnum.NO_THUMB.getKey());
		}
	}

	@Transient
	public ImageResolutionStatusEnum getResolutionStatusEnum()
	{
		return ImageResolutionStatusEnum.getStatusForKey(resolutionStatus);
	}

	public void setResolutionStatusEnum(ImageResolutionStatusEnum imageResolutionStatusEnum)
	{
		if (imageResolutionStatusEnum != null)
		{
			setResolutionStatus(imageResolutionStatusEnum.getKey());
		}
		else
		{
			setResolutionStatus(ImageResolutionStatusEnum.NO_RESOLUTION.getKey());
		}
	}
}
