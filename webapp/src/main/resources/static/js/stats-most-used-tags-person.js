
$(document).ready(function() {

	var layout = {
		title: {
			text: 'Most used tags (person)'
		},
		xaxis: {
			title: {
				text: 'Usage'
			}
		},
		showlegend: false
	};

	var config = {
		responsive: true,
		displayModeBar: true
	};

	$.ajax({
		url: '/api/1.0/tag/chart/usage/PERSON',
		dataType: 'json',
		cache: false

	}).done(function(data) {
		var data = [{
			y: data.names,
			x: data.usage,
			type: 'bar',
			orientation: 'h'}];

		Plotly.newPlot( 'chartDiv', data, layout, config );

	}).fail(function(jqXHR, textStatus, errorThrown) {
//				alert(jqXHR.responseText);
		var responseObj = $.parseJSON(jqXHR.responseText);
		console.log(responseObj.message);
	});

});
