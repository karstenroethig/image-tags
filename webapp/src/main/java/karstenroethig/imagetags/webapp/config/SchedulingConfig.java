package karstenroethig.imagetags.webapp.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import karstenroethig.imagetags.webapp.service.impl.CleanupServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.ImageImportServiceImpl;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@EnableScheduling
@Configuration
public class SchedulingConfig
{
	@Autowired private ImageImportServiceImpl imageImportService;
	@Autowired private CleanupServiceImpl cleanupService;

	@Scheduled(fixedRate = 30 /* executing every 30 minutes */, timeUnit = TimeUnit.MINUTES)
	public void executeBackgroundTasks()
	{
		log.info("Background task: Import new images (START)");
		imageImportService.execute();
		log.info("Background task: Import new images (END)");

		log.info("Background task: Cleanup deleted images (START)");
		cleanupService.execute();
		log.info("Background task: Cleanup deleted images (END)");
	}
}
