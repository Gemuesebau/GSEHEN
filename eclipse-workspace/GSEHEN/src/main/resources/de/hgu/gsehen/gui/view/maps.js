alert("Maps (re)loaded, now running custom JavaScript");

#include("typeControl.js")
#include("searchControl.js")
#include("objectArray.js")

#include("../../js/commons.js")

function setSelectedType(type) {
  alert("Setze Typ auf: " + type);
  selectedType = type;
  var style = webController.getFillStyle(type);
  var overlayType = google.maps.drawing.OverlayType;
  var controlPosition = google.maps.ControlPosition;
  drawingManager.setOptions({
    drawingMode:     overlayType.POLYGON,
    drawingControl:  true,
    drawingControlOptions: {
      position:        controlPosition.TOP_RIGHT,
      drawingModes:    [ overlayType.POLYGON ]
    },
    polygonOptions: {
      editable:      true,
      draggable:     true,
      strokeColor:   style,
      fillColor:     style
    }
  });
}

var selectedType = null;

function initialize(mapOptions, typeOptions) {
  // in UI, also the first option is initially checked
  if (typeOptions != null && typeOptions.length > 0) {
	  setSelectedType(typeOptions[0].key);
  }
  map = new google.maps.Map(document.getElementById('mapcanvas'), mapOptions);
  // from Google Maps API Doc, modified
  var typeControlDiv = document.createElement('div');
  var typeControl = new TypeControl(typeControlDiv, typeOptions, setSelectedType);
  //typeControlDiv.index = 1;
  map.controls[google.maps.ControlPosition.TOP_CENTER].push(typeControlDiv);
}

function draw() {
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

var drawables = webController.getDrawables();
if (drawables == null) {
  alert("Got 'null' drawables!");
}
else {
  //alert("Drawables for map: " + drawables);
  for (var j = 0; j < drawables.length; j++) {
    var drawable = drawables[j];
    //alert("drawable.getPolygon(): " + drawable + " (type of this drawable.getPolygon(): " + (typeof drawable.getPolygon()) + ")");
    var style = webController.getFillStyle(drawable);
    var mapsPolygon = new google.maps.Polygon({
      paths: buildJavaScriptPolygon(drawable.getPolygon(), null),
      strokeColor: style,
      fillColor: style
    });
    mapsPolygon.setMap(map);
  }
}

initAutocomplete();
draw();
captureDrawing();
