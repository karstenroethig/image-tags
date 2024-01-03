package karstenroethig.imagetags.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import karstenroethig.imagetags.webapp.config.ApplicationProperties;
import karstenroethig.imagetags.webapp.controller.util.AttributeNames;
import karstenroethig.imagetags.webapp.controller.util.UrlMappings;
import karstenroethig.imagetags.webapp.controller.util.ViewEnum;
import karstenroethig.imagetags.webapp.model.dto.backup.RestoreDto;
import karstenroethig.imagetags.webapp.service.impl.BackupInitializerServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.BackupServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.RestoreInitializerServiceImpl;
import karstenroethig.imagetags.webapp.service.impl.RestoreServiceImpl;
import karstenroethig.imagetags.webapp.util.MessageKeyEnum;
import karstenroethig.imagetags.webapp.util.Messages;
import karstenroethig.imagetags.webapp.util.validation.ValidationResult;

@ComponentScan
@Controller
@RequestMapping(UrlMappings.CONTROLLER_BACKUP)
public class BackupController extends AbstractController
{
	@Autowired private ApplicationProperties applicationProperties;

	@Autowired private BackupServiceImpl backupService;
	@Autowired private BackupInitializerServiceImpl backupInitializerService;
	@Autowired private RestoreServiceImpl restoreService;
	@Autowired private RestoreInitializerServiceImpl restoreInitializerService;

	@GetMapping()
	public String backup(Model model)
	{
		model.addAttribute("backupSettings", applicationProperties.getBackup());
		model.addAttribute("backupInfo", backupService.getBackupInfo());

		return ViewEnum.BACKUP_INDEX.getViewName();
	}

	@GetMapping(value = "/now")
	public String backupNow(Model model, final RedirectAttributes redirectAttributes)
	{
		if (backupInitializerService.backupNow())
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES, Messages.createWithSuccess(MessageKeyEnum.BACKUP_NOW_SUCCESS));
		else
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES, Messages.createWithWarning(MessageKeyEnum.BACKUP_NOW_ERROR));

		return UrlMappings.redirect(UrlMappings.CONTROLLER_BACKUP);
	}

	@GetMapping(value = "/restore")
	public String restore(Model model)
	{
		model.addAttribute("restore", restoreService.create());

		return ViewEnum.BACKUP_RESTORE.getViewName();
	}

	@PostMapping(value = "/restore")
	public String restore(@ModelAttribute("restore") @Valid RestoreDto restore, BindingResult bindingResult,
		final RedirectAttributes redirectAttributes, Model model, HttpServletRequest request)
	{
		ValidationResult validationResult = restoreService.validate(restore);
		if (validationResult.hasErrors())
			addValidationMessagesToBindingResult(validationResult.getErrors(), bindingResult);

		if (bindingResult.hasErrors() || validationResult.hasErrors())
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.RESTORE_EXECUTE_INVALID));
			return ViewEnum.BACKUP_RESTORE.getViewName();
		}

		if (restoreInitializerService.performRestore(restore))
		{
			redirectAttributes.addFlashAttribute(AttributeNames.MESSAGES, Messages.createWithSuccess(MessageKeyEnum.RESTORE_EXECUTE_SUCCESS));
			return UrlMappings.redirect(UrlMappings.CONTROLLER_BACKUP, "/restore-status");
		}
		else
		{
			model.addAttribute(AttributeNames.MESSAGES, Messages.createWithError(MessageKeyEnum.RESTORE_EXECUTE_ERROR));
			return ViewEnum.BACKUP_RESTORE.getViewName();
		}
	}

	@GetMapping(value = "/restore-status")
	public String restoreStatus(Model model)
	{
		model.addAttribute("restoreInfo", restoreService.getRestoreInfo());

		return ViewEnum.BACKUP_RESTORE_STATUS.getViewName();
	}
}