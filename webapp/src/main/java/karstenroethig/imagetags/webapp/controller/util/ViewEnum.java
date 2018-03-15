package karstenroethig.imagetags.webapp.controller.util;

import org.apache.commons.lang3.StringUtils;

public enum ViewEnum
{
	DASHBOARD("dashboard"),
	IMAGE("image"),

	ADMIN_SERVER_INFO( ControllerEnum.admin, "server-info" ),

	GALLERY_LIST(ControllerEnum.gallery, ActionEnum.list),
	GALLERY_SHOW(ControllerEnum.gallery, ActionEnum.show),

	TAG_CREATE(ControllerEnum.tag, ActionEnum.create),
	TAG_EDIT(ControllerEnum.tag, ActionEnum.edit),
	TAG_LIST(ControllerEnum.tag, ActionEnum.list);

	private static final String VIEW_SUBDIRECTORY = "views";

	private String viewName = StringUtils.EMPTY;

	private enum ControllerEnum
	{
		admin, gallery, tag;
	}

	private enum ActionEnum
	{
		create, edit, list, show;
	}

	private ViewEnum(ControllerEnum controller, ActionEnum action)
	{
		this(controller, action.name());
	}

	private ViewEnum(ControllerEnum controller, String action)
	{
		viewName += VIEW_SUBDIRECTORY;

		if (StringUtils.isNoneBlank(viewName))
		{
			viewName += "/";
		}

		viewName += controller.name();
		viewName += "/";
		viewName += action;
	}

	private ViewEnum(String viewPath)
	{
		viewName += VIEW_SUBDIRECTORY;

		if (StringUtils.isNoneBlank(viewName))
		{
			viewName += "/";
		}

		viewName += viewPath;
	}

	public String getViewName()
	{
		return viewName;
	}
}
