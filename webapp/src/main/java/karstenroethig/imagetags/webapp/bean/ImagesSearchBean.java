package karstenroethig.imagetags.webapp.bean;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import karstenroethig.imagetags.webapp.dto.ImagesPageDto;
import karstenroethig.imagetags.webapp.dto.ImagesSearchDto;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Component
@Scope(value=WebApplicationContext.SCOPE_SESSION, proxyMode=ScopedProxyMode.TARGET_CLASS)
public class ImagesSearchBean
{
	private ImagesSearchDto searchParams;
	private List<Long> searchResultImageIds;
	private int currentPage;
	private String errorMessageKey;

	public ImagesSearchBean()
	{
		clear();
	}

	public void clear()
	{
		searchParams = null;
		searchResultImageIds = new ArrayList<>();
		currentPage = 0;
		errorMessageKey = null;
	}

	public ImagesPageDto getCurrentImagesPage()
	{
		ImagesPageDto imagesPageDto = new ImagesPageDto();
		imagesPageDto.setCurrentPageNumber(currentPage);
		imagesPageDto.setMaxPageNumber(searchResultImageIds.size());
		imagesPageDto.setErrorMessageKey(errorMessageKey);

		return imagesPageDto;
	}

	public Long getImageIdForCurrentPage()
	{
		if (searchResultImageIds == null || searchResultImageIds.isEmpty() || currentPage < 1)
		{
			errorMessageKey = MessageKeyEnum.IMAGES_SEARCH_EMPTY_RESULT.getKey();
			return null;
		}

		return searchResultImageIds.get(currentPage - 1);
	}

	public void switchToCurrentPage(int page)
	{
		if (page < 1)
		{
			page = 1;
		}

		currentPage = Math.min(page, searchResultImageIds.size());
	}

	public void removeImageFormSearchResult(Long imageId)
	{
		searchResultImageIds.remove(imageId);
	}
}
