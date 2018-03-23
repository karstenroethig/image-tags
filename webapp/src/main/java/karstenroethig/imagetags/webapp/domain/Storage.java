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
		@UniqueConstraint( columnNames = {"key" } )
	}
)
public class Storage
{
	@Column(
		length = 18,
		nullable = false,
		unique = true
	)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;

	@Column(
		length = 100,
		nullable = false
	)
	private String key;

	@Column(
		name = "file_size",
		nullable = false
	)
	private Long size;
}
