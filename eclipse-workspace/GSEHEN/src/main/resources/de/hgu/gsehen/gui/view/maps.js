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
    drawingManager, 'overlaycomplete',
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

function setDefaultViewPortBounds() {
  // green part of Geisenheim Campus
  viewPortBounds.extend(new google.maps.LatLng(49.984605, 7.959441));
  viewPortBounds.extend(new google.maps.LatLng(49.983059, 7.964469));
}

initialize({
  //center:            new google.maps.LatLng(webController.getCenterLat(), webController.getCenterLng()),
  //zoom:              webController.getZoom(),
  fullscreenControl: false
}, objectArray(webController.getLocalizedTypes(), function(pair) { return { key: pair.getLeft(), title: pair.getRight() }; }));

var viewPortBounds = new google.maps.LatLngBounds();
var drawables = webController.getDrawables();
if (drawables == null) {
  alert("Got 'null' drawables!");
  setDefaultViewPortBounds();
}
else {
  if (drawables.length == 0) {
    alert("Got no drawables");
    setDefaultViewPortBounds();
  }
  else {
    //alert("Drawables for map: " + drawables);
    for (var j = 0; j < drawables.length; j++) {
      var drawable = drawables[j];
      //alert("drawable.getPolygon(): " + drawable +
      // " (type of this drawable.getPolygon(): " + (typeof drawable.getPolygon()) + ")");
      var style = webController.getFillStyle(drawable);
      var mapsPolygon = new google.maps.Polygon({
        paths: buildJavaScriptPolygonLatLngCallback(drawable.getPolygon(), function (lat, lng) {
          viewPortBounds.extend(new google.maps.LatLng(lat, lng));
        }),
        strokeColor: style,
        fillColor: style
      });
      mapsPolygon.setMap(map);
    }
  }
}
map.fitBounds(viewPortBounds);

initAutocomplete();
draw();
captureDrawing();
