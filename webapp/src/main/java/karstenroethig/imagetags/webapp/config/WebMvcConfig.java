package karstenroethig.imagetags.webapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import karstenroethig.imagetags.webapp.controller.formatter.TagFormatter;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{
	@Override
	public void addFormatters(FormatterRegistry formatterRegistry)
	{
		formatterRegistry.addFormatter(new TagFormatter());
	}
}
