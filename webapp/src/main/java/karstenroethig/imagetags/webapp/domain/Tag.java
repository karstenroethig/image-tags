package karstenroethig.imagetags.webapp.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import karstenroethig.imagetags.webapp.domain.enums.TagTypeEnum;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode

@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint( columnNames = {"id" } ),
		@UniqueConstraint( columnNames = {"name" } )
	}
)
public class Tag
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
		length = 255,
		nullable = false
	)
	private String name;

	@Column( nullable = false )
	private Integer type;

	@Transient
	public TagTypeEnum getTypeEnum()
	{
		return TagTypeEnum.getTypeForKey(type);
	}

	public void setTypeEnum(TagTypeEnum tagTypeEnum)
	{
		if (tagTypeEnum != null)
		{
			setType(tagTypeEnum.getKey());
		}
		else
		{
			setType(null);
		}
	}
}
