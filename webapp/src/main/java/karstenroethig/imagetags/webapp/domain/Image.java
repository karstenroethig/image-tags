package karstenroethig.imagetags.webapp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
}
