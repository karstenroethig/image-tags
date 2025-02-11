package karstenroethig.imagetags.webapp.controller.util;

import org.apache.commons.lang3.StringUtils;

public class UrlMappings
{
	private static final String REDIRECT_PREFIX = "redirect:";

	public static final String HOME = "/";
	public static final String DASHBOARD = "/dashboard";
	public static final String SERVER_INFO = "/server-info";

	public static final String CONTROLLER_TAG = "/tag";
	public static final String CONTROLLER_IMAGE = "/image";
	public static final String CONTROLLER_STATS = "/stats";
	public static final String CONTROLLER_BACKUP = "/backup";

	public static final String ACTION_LIST = "/list";
	public static final String ACTION_SHOW = "/show/{id}";
	public static final String ACTION_CREATE = "/create";
	public static final String ACTION_EDIT = "/edit/{id}";
	public static final String ACTION_DELETE = "/delete/{id}";
	public static final String ACTION_SAVE = "/save";
	public static final String ACTION_UPDATE = "/update";
	public static final String ACTION_SEARCH = "/search";
	public static final String ACTION_CONTENT = "/content/{id}";

	private UrlMappings() {}

	public static String redirect(String controllerPath, String actionPath)
	{
		return redirect(controllerPath + actionPath);
	}

	public static String redirectWithId(String controllerPath, String actionPath, Long id)
	{
		String idString = id == null ? StringUtils.EMPTY : id.toString();
		String path = StringUtils.replace(controllerPath + actionPath, "{id}", idString);
		return redirect(path);
	}

	public static String redirect(String path)
	{
		return REDIRECT_PREFIX + path;
	}
}
