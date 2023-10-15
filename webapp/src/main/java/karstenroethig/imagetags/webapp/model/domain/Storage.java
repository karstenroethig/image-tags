package karstenroethig.imagetags.webapp.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "storage")
public class Storage extends AbstractEntityId
{
	@Column(name = "storage_key", length = 100, nullable = false)
	private String key;

	@Column(name = "file_size", nullable = false)
	private Long size;
}
