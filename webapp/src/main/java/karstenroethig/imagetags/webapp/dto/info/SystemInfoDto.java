package karstenroethig.imagetags.webapp.dto.info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SystemInfoDto
{
	private String version;
	private String revision;
	private String buildDate;

	private String serverTime;
	private String uptime;

	private String javaVersion;
	private String javaVendor;
	private String javaVm;
	private String javaVmVersion;
	private String javaRuntime;
	private String javaHome;
	private String osName;
	private String osArchitecture;
	private String osVersion;
	private String fileEncoding;
	private String userName;
	private String userDir;
	private String userTimezone;
	private String userLocale;
}