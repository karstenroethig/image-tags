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
@Table(name = "image")
public class Image extends AbstractEntityId
{
	@Column(name = "title", length = 191, nullable = false)
	private String title;

	@Column(name = "description", nullable = true)
	private String description;
}
