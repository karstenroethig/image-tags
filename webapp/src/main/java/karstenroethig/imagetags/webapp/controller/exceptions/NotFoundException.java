package karstenroethig.imagetags.webapp.controller.exceptions;

public class NotFoundException extends RuntimeException
{
	public NotFoundException(String message)
	{
		super(message);
	}
}
