package karstenroethig.imagetags.webapp.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app", ignoreUnknownFields = false)
@Validated
@Getter
@Setter
public class ApplicationProperties
{
	@NotNull
	private Path importDirectory = Paths.get("data/new");

	@NotNull
	private Path storageDirectory = Paths.get("data");
}
