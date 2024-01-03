package karstenroethig.imagetags.webapp.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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

	/** Backup settings. */
	private BackupSettings backup = new BackupSettings();

	@Getter
	@Setter
	@ToString
	public static class BackupSettings
	{
		/** Directory for backup files. */
		private Path backupDirectory = Paths.get("backups");

		/** Backup file prefix. */
		private String backupFilePrefix = "backup_";

		/** Backup file date pattern. */
		private String backupFileDatePattern = "yyyy-MM-dd_HH-mm";
	}
}
