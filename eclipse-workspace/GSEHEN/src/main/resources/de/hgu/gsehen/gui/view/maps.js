alert("Maps (re)loaded, now running custom JavaScript");

#include("typeControl.js")
#include("objectArray.js")

var selectedType = null;

function initialize(mapOptions, typeOptions) {
  // in UI, also the first option is initially checked
  if (typeOptions != null && typeOptions.length > 0) {
	  selectedType = typeOptions[0].key;
  }
  map = new google.maps.Map(document.getElementById('mapcanvas'), mapOptions);
  // from Google Maps API Doc, modified
  var typeControlDiv = document.createElement('div');
  var typeControl = new TypeControl(typeControlDiv, typeOptions);
  //typeControlDiv.index = 1;
  map.controls[google.maps.ControlPosition.TOP_CENTER].push(typeControlDiv);
}

function draw() {
  var overlayType = google.maps.drawing.OverlayType;
  drawingManager.setOptions({
    drawingMode : google.maps.drawing.OverlayType.POLYGON,
    drawingControl : true,
    drawingControlOptions : {
      position : google.maps.ControlPosition.TOP_RIGHT,
      drawingModes : [ overlayType.POLYGON ]
    },
    rectangleOptions : {
      strokeColor : 'red',
      fillColor : 'red',
      editable: true,
      draggable: true
    },
    polygonOptions : {
      editable: true,
      draggable: true
    }
  });
  drawingManager.setMap(map);
}

function captureDrawing() {
  google.maps.event.addListener(
    drawingManager,
    'overlaycomplete',
    function (polygon) {
	  var javaPolygon = webController.getEmptyPolygon();
	  var polygonLatLngArray = polygon.overlay.getPath().getArray();
	  for (var i=0; i<polygonLatLngArray.length; i++) {
	    javaPolygon.addGeoPointByCoords(polygonLatLngArray[i].lat(), polygonLatLngArray[i].lng());
	  }
      webController.polygonDrawn(javaPolygon, selectedType);
    }
  );
}

initialize({
	center:            new google.maps.LatLng(webController.getCenterLat(), webController.getCenterLng()),
	zoom:              webController.getZoom(),
	fullscreenControl: false
}, objectArray(webController.getLocalizedTypes(), function(pair) { return { key: pair.getLeft(), title: pair.getRight() }; }));
draw();
captureDrawing();
