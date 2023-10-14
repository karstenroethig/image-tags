package karstenroethig.imagetags.webapp.model.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)

@MappedSuperclass
public abstract class AbstractEntityCreatedInfo extends AbstractEntityId
{
	@Column(name = "created_datetime", nullable = false)
	private LocalDateTime createdDatetime;
}
