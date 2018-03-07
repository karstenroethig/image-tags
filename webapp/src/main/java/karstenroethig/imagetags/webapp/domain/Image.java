package karstenroethig.imagetags.webapp.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

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

	@ManyToMany(
		fetch = FetchType.LAZY,
		cascade = CascadeType.ALL
	)
	@JoinTable(
		name = "Image_Tag",
		joinColumns = { @JoinColumn(name = "image_id") },
		inverseJoinColumns = { @JoinColumn(name = "tag_id") }
	)
	private Set<Tag> tags = new HashSet<>();

	public void addTag(Tag tag)
	{
		tags.add(tag);
	}

	public void clearTags()
	{
		tags.clear();
	}
}
