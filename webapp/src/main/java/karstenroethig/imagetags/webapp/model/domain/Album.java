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
@Table(name = "album")
public class Album extends AbstractEntityId
{
	@Column(name = "name", length = 191, nullable = false)
	private String name;

	@Column(name = "author", length = 191, nullable = false)
	private String author;
}