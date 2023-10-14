package karstenroethig.imagetags.webapp.bean.search;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import jakarta.annotation.PostConstruct;
import karstenroethig.imagetags.webapp.model.dto.search.ImageSearchDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ImageSearchBean
{
	private ImageSearchDto imageSearchDto;

	@PostConstruct
	public void clear()
	{
		imageSearchDto = new ImageSearchDto();
	}
}
