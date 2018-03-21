package karstenroethig.imagetags.webapp.config.properties;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "imageData", ignoreUnknownFields = false)
@Getter
@Setter
public class ImageDataProperties
{
	private Path importDirectory = Paths.get("data/new");
	private Path zipPath = Paths.get("data/images.zip");
	private Path thumbsZipPath = Paths.get("data/images_thumbs.zip");
}
