package karstenroethig.imagetags.webapp.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import karstenroethig.imagetags.webapp.service.impl.ImageImportServiceImpl;

@EnableScheduling
@Configuration
public class SchedulingConfig
{
	@Autowired private ImageImportServiceImpl imageImportService;

	@Scheduled(fixedRate = 30 /* executing every 30 minutes */, timeUnit = TimeUnit.MINUTES)
	public void executeImageImport()
	{
		imageImportService.execute();
	}
}
