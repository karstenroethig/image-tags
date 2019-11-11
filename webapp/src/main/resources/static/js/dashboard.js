$(document).ready( function() {

	var words;

	$.when(

		$.ajax( {
			url: '/api/1.0/allTagsWithOccurrences',
			dataType: 'json',
			cache: false
		}).done( function( data ) {
			words = convertTagsToWords( data );
		}).fail( function( jqXHR, textStatus, errorThrown ) {
			alert( jqXHR.responseText );
		})

	).then( function() {
		$('#wordCloud').jQCloud(words);
	});

	/**
	 * tags example: [{"id":1, "name":"scrum", "amount":25},{"id":2, "name":"kanban", "amount":3}]
	 * 
	 * var words = [
	 * 		{text: "Lorem", weight: 13, link: "http://github.com/mistic100/jQCloud"},
	 * 		{text: "Ipsum", weight: 100, link: "http://www.strangeplanet.fr"},
	 * 		{text: "Dolor", weight: 9.4, link: "http://piwigo.org"}
	 * ];
	 */
	function convertTagsToWords( tags )
	{
		var words = [];

		for( var index in tags )
		{
			words.push( { "text": tags[index].name, "weight": tags[index].amount, "link": "/image/tag/"+tags[index].id } );
		}

		return words;
	}
});