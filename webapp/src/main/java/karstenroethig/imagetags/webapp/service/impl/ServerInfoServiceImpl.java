package karstenroethig.imagetags.webapp.service.impl;

import java.util.Date;
import java.util.Locale;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import karstenroethig.imagetags.webapp.dto.info.MemoryInfoDto;
import karstenroethig.imagetags.webapp.dto.info.ServerInfoDto;
import karstenroethig.imagetags.webapp.dto.info.SystemInfoDto;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;

@Service
public class ServerInfoServiceImpl implements ApplicationContextAware
{
	@Autowired
	private MessageSource messageSource;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException
	{
		this.applicationContext = applicationContext;
	}

	public ServerInfoDto getInfo()
	{
		/*
		 * collect system infos
		 */
		SystemInfoDto systemInfo = new SystemInfoDto();

		String version = messageSource.getMessage( MessageKeyEnum.APPLICATION_VERSION.getKey(), null, LocaleContextHolder.getLocale() );

		long serverStartupTime = applicationContext.getStartupDate();
		long uptimeMilis = System.currentTimeMillis() - serverStartupTime;

		systemInfo.setVersion( version );
		systemInfo.setServerTime( new Date().toString() );
		systemInfo.setUptime( formatUptime( uptimeMilis ) );
		systemInfo.setJavaVersion( System.getProperty( "java.version" ) );
		systemInfo.setJavaVendor( System.getProperty( "java.vendor" ) );
		systemInfo.setJavaVm( System.getProperty( "java.vm.name" ) );
		systemInfo.setJavaVmVersion( System.getProperty( "java.vm.version" ) );
		systemInfo.setJavaRuntime( System.getProperty( "java.runtime.name" ) );
		systemInfo.setJavaHome( System.getProperty( "java.home" ) );
		systemInfo.setOsName( System.getProperty( "os.name" ) );
		systemInfo.setOsArchitecture( System.getProperty( "os.arch" ) );
		systemInfo.setOsVersion( System.getProperty( "os.version" ) );
		systemInfo.setFileEncoding( System.getProperty( "file.encoding" ) );
		systemInfo.setUserName( System.getProperty( "user.name" ) );
		systemInfo.setUserDir( System.getProperty( "user.dir" ) );
		systemInfo.setUserTimezone( System.getProperty( "user.timezone" ) );

		if ( ( System.getProperty( "user.country" ) != null ) && ( System.getProperty( "user.language" ) != null ) )
		{
			systemInfo.setUserLocale( new Locale( System.getProperty( "user.country" ), System.getProperty( "user.language" ) ).toString() );
		}

		/*
		 * collect memory infos
		 */
		MemoryInfoDto memoryInfo = new MemoryInfoDto();
		Runtime runtime = Runtime.getRuntime();

		long totalMemory = runtime.maxMemory();
		long freeMemory = runtime.freeMemory();
		long usedMemory = totalMemory - freeMemory;

		memoryInfo.setTotalFormated( formatMemory( totalMemory ) );
		memoryInfo.setUsedFormated( formatMemory( usedMemory ) );
		memoryInfo.setFreeFormated( formatMemory( freeMemory ) );
		memoryInfo.setFreePercentage( freeMemory * 100 / totalMemory );

		/*
		 * put system info and memory info to server info
		 */
		ServerInfoDto info = new ServerInfoDto();

		info.setSystemInfo( systemInfo );
		info.setMemoryInfo( memoryInfo );

		return info;
	}

	private static String formatUptime( long uptime )
	{
		long diffInSeconds = uptime / 1000;
		long[] diff = new long[] {0, 0, 0, 0 }; // sec
		diff[3] = ( ( diffInSeconds >= 60 ) ? ( diffInSeconds % 60 ) : diffInSeconds ); // min
		diff[2] = ( ( diffInSeconds = ( diffInSeconds / 60 ) ) >= 60 ) ? ( diffInSeconds % 60 ) : diffInSeconds; // hours
		diff[1] = ( ( diffInSeconds = ( diffInSeconds / 60 ) ) >= 24 ) ? ( diffInSeconds % 24 ) : diffInSeconds; // days
		diff[0] = ( diffInSeconds = ( diffInSeconds / 24 ) );

		return String.format( "%d day%s, %d hour%s, %d minute%s, %d second%s", diff[0], ( diff[0] != 1 ) ? "s" : "",
			diff[1], ( diff[1] != 1 ) ? "s" : "", diff[2], ( diff[2] != 1 ) ? "s" : "", diff[3],
			( diff[3] != 1 ) ? "s" : "" );
	}

	private static String formatMemory( long bytes )
	{
		if ( bytes > ( 1024L * 1024L ) )
		{
			return ( bytes / ( 1024L * 1024L ) ) + " MB";
		}
		else if ( bytes > 1024L )
		{
			return ( bytes / ( 1024L ) ) + " kB";
		}
		else
		{
			return bytes + " B";
		}
	}
}