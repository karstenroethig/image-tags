package karstenroethig.imagetags.webapp.util;

import lombok.Getter;

public enum MessageKeyEnum
{
	APPLICATION_VERSION( "application.version" ),

	/* ********************
	 * Tag.
	 * ********************
	 */
	/** "Tag > Hinzufügen": Eingaben nicht valide. */
	TAG_SAVE_INVALID( "tag.save.invalid" ),

	/** "Tag > Hinzufügen": Erfolgreich gespeichert. */
	TAG_SAVE_SUCCESS( "tag.save.success" ),

	/** "Tag > Hinzufügen": Unerwarteter Fehler ist aufgetreten. */
	TAG_SAVE_ERROR( "tag.save.error" ),

	/** "Tag > Bearbeiten": Eingaben nicht vailde. */
	TAG_UPDATE_INVALID( "tag.update.invalid" ),

	/** "Tag > Bearbeiten": Erfolgreich gespeichert. */
	TAG_UPDATE_SUCCESS( "tag.update.success" ),

	/** "Tag > Bearbeiten": Unerwarteter Fehler ist aufgetreten. */
	TAG_UPDATE_ERROR( "tag.update.error" ),

	/** "Tag > Löschen": Erfolgreich gelöscht. */
	TAG_DELETE_SUCCESS( "tag.delete.success" ),

	/** "Tag > Löschen": Unerwarteter Fehler ist aufgetreten. */
	TAG_DELETE_ERROR( "tag.delete.error" );

	@Getter
	private String key;

	private MessageKeyEnum( String key )
	{
		this.key = key;
	}
}
