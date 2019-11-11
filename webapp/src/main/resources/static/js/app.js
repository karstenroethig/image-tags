
$( document ).ready( function() {

	$('.select2-multiple').select2();

	// delete modals: transfer the id to the modal form
	$( '#deleteModal' ).on( 'show.bs.modal', function( event ) {
		var button = $( event.relatedTarget ); // Button that triggered the modal
		var id = button.data( 'id' ); // Extract info from data-* attributes

		// Update the modal's content.
		var modal = $( this );
		var link = modal.find( '.btn-danger' );
		var template = link.data( 'href-template' );
		link.attr( 'href', template.replace( '{id}', id ) );
	});
});