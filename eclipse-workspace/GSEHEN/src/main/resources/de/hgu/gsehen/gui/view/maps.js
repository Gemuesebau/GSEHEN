alert("Maps (re)loaded, now running custom JavaScript");

#include("typeControl.js")
#include("searchControl.js")
#include("objectArray.js")

// eventsDebouncer ...
#include("../../js/commons.js")

function addPolygonOptions(obj, style) {
  obj.editable = true;
  obj.draggable = true;
  obj.strokeColor = style;
  obj.fillColor = style;
  return obj;
}

var selectedType = null;

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

function initialize(typeOptions) {
  // in UI, also the first option is initially checked
  if (typeOptions != null && typeOptions.length > 0) {
    setSelectedType(typeOptions[0].key);
  }
  map = new google.maps.Map(document.getElementById('mapcanvas'), { fullscreenControl: false });
  var typeControlDiv = document.createElement('div');
  var typeControl = new TypeControl(typeControlDiv, typeOptions, setSelectedType);
  map.controls[google.maps.ControlPosition.TOP_CENTER].push(typeControlDiv);
}

function addEventListeners() {
  google.maps.event.addListener(
    drawingManager, 'overlaycomplete', function (polygon) {
      var drawable = webController.getDrawableWithEmptyPolygon(selectedType);
      var javaPolygon = drawable.getPolygon();
      var polygonLatLngArray = polygon.overlay.getPath().getArray();
      for (var i=0; i<polygonLatLngArray.length; i++) {
        javaPolygon.addGeoPointByCoords(polygonLatLngArray[i].lat(), polygonLatLngArray[i].lng());
      }
      webController.drawableDone(drawable);
      polygons[drawable.getUuid()] = polygon.overlay;
    }
  );
  google.maps.event.addListener(
    map, 'bounds_changed', function () {
      eventsDebouncer.debounce('mapBoundsChanged', function () {
        var bounds = map.getBounds().toJSON();
        webController.mapBoundsChanged(bounds.north, bounds.south, bounds.east, bounds.west);
      });
    }
  );
}

var viewportBounds = new google.maps.LatLngBounds();

/**
 * Extends viewportBounds by the green part of Geisenheim Campus.
 */
function setDefaultViewportBounds() {
  viewportBounds.extend(new google.maps.LatLng(49.984605, 7.959441));
  viewportBounds.extend(new google.maps.LatLng(49.983059, 7.964469));
}

/**
 * Extends viewportBounds by the given viewport.
 */
function setViewportBounds(viewport) {
  viewportBounds.extend(new google.maps.LatLng(viewport.getLeft().getLat(), viewport.getLeft().getLng()));
  viewportBounds.extend(new google.maps.LatLng(viewport.getRight().getLat(), viewport.getRight().getLng()));
}

/**
 * Extends viewportBounds by controller's last viewport.
 */
function setViewportByController() {
  setViewportBounds(webController.getLastViewport());
}

/**
 * Sets viewportBounds by controller's last viewport.
 */
function clearAndSetViewportByController() {
  viewportBounds = new google.maps.LatLngBounds();
  setViewportByController();
  map.fitBounds(viewportBounds);
}

initialize(objectArray(
  webController.getLocalizedTypes(),
  function objectConverter(pair) {
    return { key: pair.getLeft(), title: pair.getRight() };
  }
));

var polygons = {};

function forAllPolygons(handler) {
  for (var uuid in polygons) {
    if (polygons.hasOwnProperty(uuid)) {
      handler(uuid, polygons[uuid]);
    }
  }
}

var drawables = null;

function redraw() {
  forAllPolygons(function (uuid, polygon) {
    alert("Removing polygon with UUID=" + uuid);
    polygon.setMap(null);
  });
  polygons = {};
  drawables = webController.getDrawables();
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
      for (var j = 0; j < drawables.length; j++) {
        var drawable = drawables[j];
        var style = webController.getFillStyle(drawable);
        var mapsPolygon = new google.maps.Polygon(addPolygonOptions({
          paths: buildJavaScriptPolygon(drawable.getPolygon(), null)
        }, style));
        polygons[drawable.getUuid()] = mapsPolygon;
        mapsPolygon.setMap(map);
      }
      setViewportByController();
    }
  }
  map.fitBounds(viewportBounds);
}

redraw();
initAutocomplete();
drawingManager.setMap(map);
addEventListeners();
