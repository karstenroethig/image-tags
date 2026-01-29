package karstenroethig.imagetags.webapp.controller.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import lombok.Getter;

public enum ViewEnum
{
	DASHBOARD("/dashboard"),

	SERVER_INFO("/server-info" ),

	TAG_LIST(ControllerEnum.TAG, ActionEnum.LIST),
	TAG_CREATE(ControllerEnum.TAG, ActionEnum.CREATE),
	TAG_EDIT(ControllerEnum.TAG, ActionEnum.EDIT),

	IMAGE_LIST(ControllerEnum.IMAGE, ActionEnum.LIST),
	IMAGE_SHOW(ControllerEnum.IMAGE, ActionEnum.SHOW),

	STATS_MOST_USED_TAGS_PERSON(ControllerEnum.STATS, "/most-used-tags-person"),
	STATS_MOST_USED_TAGS_CATEGORY(ControllerEnum.STATS, "/most-used-tags-category"),
	STATS_MOST_USED_FILESIZE_PER_TAG_PERSON(ControllerEnum.STATS, "/most-used-filesize-per-tag-person"),
	STATS_MOST_USED_FILESIZE_PER_TAG_CATEGORY(ControllerEnum.STATS, "/most-used-filesize-per-tag-category"),

	BACKUP_INDEX(ControllerEnum.BACKUP, "/index"),
	BACKUP_RESTORE(ControllerEnum.BACKUP, "/restore"),
	BACKUP_RESTORE_STATUS(ControllerEnum.BACKUP, "/restore-status");

	private static final String VIEW_SUBDIRECTORY = "views";

	@Getter
	private String viewName = StringUtils.EMPTY;

	private enum ControllerEnum
	{
		TAG,
		IMAGE,
		STATS,
		BACKUP;

		private String path = null;

		private ControllerEnum() {}

		private ControllerEnum(String path)
		{
			this.path = path;
		}

		public String getPath()
		{
			return path != null ? path : ("/" + name().toLowerCase());
		}
	}

	private enum ActionEnum
	{
		CREATE,
		EDIT,
		LIST,
		SHOW;

		private ActionEnum() {}

		public String getPath()
		{
			return "/" + name().toLowerCase();
		}
	}

	private ViewEnum(ControllerEnum subController, ControllerEnum controller, ActionEnum action)
	{
		this(subController, controller, action.getPath());
	}

	private ViewEnum(ControllerEnum controller, ActionEnum action)
	{
		this(null, controller, action.getPath());
	}

	private ViewEnum(ControllerEnum controller, String path)
	{
		this(null, controller, path);
	}

	private ViewEnum(String path)
	{
		this(null, null, path);
	}

	private ViewEnum(ControllerEnum subController, ControllerEnum controller, String path)
	{
		StringBuilder newViewName = new StringBuilder(VIEW_SUBDIRECTORY);

		if (subController != null)
			newViewName.append(subController.getPath());

		if (controller != null)
			newViewName.append(controller.getPath());

		if (path != null)
			newViewName.append(path);

		viewName = Strings.CS.removeStart(newViewName.toString(), "/"); // just in case if there is no view sub-directory
	}
}
