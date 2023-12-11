package karstenroethig.imagetags.webapp.controller.exceptions;

public class ForbiddenException extends RuntimeException
{
	public ForbiddenException()
	{
		super();
	}

	public ForbiddenException(String message)
	{
		super(message);
	}
}
