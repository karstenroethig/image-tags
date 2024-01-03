package karstenroethig.imagetags.webapp.model.dto.backup;

import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import lombok.Getter;

@Getter
public class BackupInfoDto extends AbstractProgressInfoDto
{
	public BackupInfoDto()
	{
		super(MessageKeyEnum.BACKUP_TASK_INITIALIZE);
	}

	public void intializeNewBackup()
	{
		intialize(MessageKeyEnum.BACKUP_TASK_INITIALIZE);
	}
}
