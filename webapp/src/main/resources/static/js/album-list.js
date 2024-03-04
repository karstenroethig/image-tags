
$(document).ready(function() {

	setTimeout( function() {

		$('.ajaxFetchUsage').each(function() {

			var element = $(this);
			element.html('<i class="fa fa-spinner fa-spin"></i>');

			$.ajax({
				url: '/api/1.0/album/' + element.data('id') + '/usage',
				dataType: 'json',
				cache: false

			}).done(function(data) {
				element.html(data.usage);

			}).fail(function(jqXHR, textStatus, errorThrown) {
//				alert(jqXHR.responseText);
				var responseObj = $.parseJSON(jqXHR.responseText);
				element.html('<i class="fa fa-exclamation-circle" style="color: red;" title="' + responseObj.message + '"></i>');
			});
		});
	}, 5000);

	setTimeout( function() {

		$('.ajaxFetchFilesize').each(function() {

			var element = $(this);
			element.html('<i class="fa fa-spinner fa-spin"></i>');

			$.ajax({
				url: '/api/1.0/album/' + element.data('id') + '/filesize',
				dataType: 'json',
				cache: false

			}).done(function(data) {
				element.html(data.filesize);

			}).fail(function(jqXHR, textStatus, errorThrown) {
//				alert(jqXHR.responseText);
				var responseObj = $.parseJSON(jqXHR.responseText);
				element.html('<i class="fa fa-exclamation-circle" style="color: red;" title="' + responseObj.message + '"></i>');
			});
		});
	}, 5000);
});
