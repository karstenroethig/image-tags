package karstenroethig.imagetags.webapp.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode

@MappedSuperclass
public abstract class AbstractEntityId
{
	@Column(length = 18, nullable = false, unique = true)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
}
