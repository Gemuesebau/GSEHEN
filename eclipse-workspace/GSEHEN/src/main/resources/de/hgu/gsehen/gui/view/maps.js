alert("Maps (re)loaded, now running custom JavaScript");

#include("typeControl.js")
#include("searchControl.js")
#include("objectArray.js")

#include("../../js/commons.js")

var timeoutObjects = {};

function debounce(timeoutObjKey, delegateFunc, delegateFuncArgsAsArray) {
  if (timeoutObjects[timeoutObjKey] != null) {
    window.clearTimeout(timeoutObjects[timeoutObjKey]);
    timeoutObjects[timeoutObjKey] = null;
  }
  if (delegateFuncArgsAsArray == null || delegateFuncArgsAsArray.length == 0) {
    timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500);
  }
  else if (delegateFuncArgsAsArray.length == 1) {
    timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500,
      delegateFuncArgsAsArray[0]
    );
  }
  else if (delegateFuncArgsAsArray.length == 2) {
    timeoutObjects[timeoutObjKey] = window.setTimeout(delegateFunc, 500,
      delegateFuncArgsAsArray[0],
      delegateFuncArgsAsArray[1]
    );
  }
  else {
    alert("No debounce implementation for " + delegateFuncArgsAsArray.length +
      " delegate function arguments!");
  }
}

function addPolygonOptions(obj, style) {
  obj.editable = true;
  obj.draggable = true;
  obj.strokeColor = style;
  obj.fillColor = style;
  return obj;
}

function setSelectedType(type) {
  alert("Setting type for new objects to: " + type);
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
    polygonOptions: addPolygonOptions({}, style)
  });
}

var selectedType = null;

function initialize(mapOptions, typeOptions) {
  // in UI, also the first option is initially checked
  if (typeOptions != null && typeOptions.length > 0) {
    setSelectedType(typeOptions[0].key);
  }
  map = new google.maps.Map(document.getElementById('mapcanvas'), mapOptions);
  var typeControlDiv = document.createElement('div');
  var typeControl = new TypeControl(typeControlDiv, typeOptions, setSelectedType);
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
  google.maps.event.addListener(
    map, 'bounds_changed', function() {
    	debounce('mapBoundsChanged', function() {
    		var bounds = map.getBounds().toJSON();
    		webController.mapBoundsChanged(
    				bounds.north, // 49.40646986706016
    				bounds.south, // 49.39669051179038
    				bounds.east,  // 8.355449915406325
    				bounds.west   // 8.335816145417311
    		)
    	});
    }
  );
}

/**
 * Extends viewportBounds by the green part of Geisenheim Campus
 */
function setDefaultViewportBounds() {
  viewportBounds.extend(new google.maps.LatLng(49.984605, 7.959441));
  viewportBounds.extend(new google.maps.LatLng(49.983059, 7.964469));
}

initialize({ fullscreenControl: false }, objectArray(webController.getLocalizedTypes(),
  function objectConverter(pair) {
    return { key: pair.getLeft(), title: pair.getRight() };
  }
));

var viewportBounds = new google.maps.LatLngBounds();
var drawables = webController.getDrawables();
if (drawables == null) {
  alert("Got 'null' drawables!");
  setDefaultViewportBounds();
}
else {
  if (drawables.length == 0) {
    alert("Got no drawables");
    setDefaultViewportBounds();
  }
  else {
    //alert("Drawables for map: " + drawables);
    for (var j = 0; j < drawables.length; j++) {
      var drawable = drawables[j];
      var style = webController.getFillStyle(drawable);
      var mapsPolygon = new google.maps.Polygon(addPolygonOptions({
        paths: buildJavaScriptPolygonLatLngCallback(drawable.getPolygon(), function (lat, lng) {
          viewportBounds.extend(new google.maps.LatLng(lat, lng));
        })
      }, style));
      mapsPolygon.setMap(map);
    }
  }
}
map.fitBounds(viewportBounds);

initAutocomplete();
draw();
captureDrawing();
