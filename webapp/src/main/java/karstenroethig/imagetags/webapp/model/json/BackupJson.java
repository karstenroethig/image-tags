package karstenroethig.imagetags.webapp.model.json;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BackupJson
{
	private List<TagJson> tags;
	private List<AlbumJson> albums;
	private List<StorageJson> storages;
	private List<ImageJson> images;
}
