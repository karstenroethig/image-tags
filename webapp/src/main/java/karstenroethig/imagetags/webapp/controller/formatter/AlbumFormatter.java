package karstenroethig.imagetags.webapp.controller.formatter;

import karstenroethig.imagetags.webapp.model.dto.AlbumDto;

public class AlbumFormatter extends AbstractIdFormatter<AlbumDto>
{
	@Override
	protected AlbumDto createInstance()
	{
		return new AlbumDto();
	}
}
