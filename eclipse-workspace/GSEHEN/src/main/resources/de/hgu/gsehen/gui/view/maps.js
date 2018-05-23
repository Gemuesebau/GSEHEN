initialize({
	center:            new google.maps.LatLng(52.266344, 10.519835),
	zoom:              16,
	fullscreenControl: false
}, [ { title: 'Farm', key: 'Farm' }, { title: 'Field', key: 'Field' }, { title: 'Plot', key: 'Plot' } ]);
draw();
captureDrawing();
