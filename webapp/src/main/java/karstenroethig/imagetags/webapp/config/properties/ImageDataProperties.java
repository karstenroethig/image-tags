package karstenroethig.imagetags.webapp.config.properties;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "image-data", ignoreUnknownFields = false)
@Getter
@Setter
public class ImageDataProperties
{
	private Path importDirectory = Paths.get("data/new");
	private Path storageDirectory = Paths.get("data");
}
