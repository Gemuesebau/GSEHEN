alert("Farm view (re)loaded, now running JavaScript");

#include("../../js/commons.js")

var canvas = d3.select("#d3canvas");
var context = canvas.node().getContext("2d");

var lastZoomParams = {
  x: 0, y: 0, k: 1
};

function redraw() {
  drawPoints(lastZoomParams.x, lastZoomParams.y, lastZoomParams.k);
}

function clearAndSetViewportByController() {
  redraw();
}

canvas.call(d3.zoom()
  .scaleExtent([0.1, 10])
  .on("zoom", function () {
    lastZoomParams = {
      x: d3.event.transform.x, y: d3.event.transform.y, k: d3.event.transform.k
    };
    redraw();
  })
);
redraw();

function transform(pointCoordValue, viewportCoordValue, factor, flipHeightOrNull) {
  var value = (pointCoordValue - viewportCoordValue) * factor;
  if (flipHeightOrNull == null) {
    return value;
  }
  else {
    return flipHeightOrNull - value;
  }
}

function reverseTransform(value, viewportCoordValue, factor, flipHeightOrNull) {
  return (flipHeightOrNull == null ? value : flipHeightOrNull - value) / factor + viewportCoordValue;
}

function lineAndLog(context, points, pointIndex, factor, canvasHeight, viewportLeftLng, viewportLeftLat) {
  var x = transform(points.get(pointIndex).getLng(), viewportLeftLng, factor, null);
  var y = transform(points.get(pointIndex).getLat(), viewportLeftLat, factor, canvasHeight);
  context.lineTo(x, y);
}

function drawPoints(x, y, k) {
  var splitPane = webController.getMainSplitPane();
  var canvasWidth = Math.floor(splitPane.getWidth() * (1 - splitPane.getDividerPositions()[0]) * 0.95);
  var canvasHeight = Math.floor(splitPane.getHeight() * 0.9);
  alert("canvasWidth=" + canvasWidth + ", canvasHeight=" + canvasHeight);
  var canvasElement = document.getElementById("d3canvas");
  canvasElement.width = canvasWidth;
  canvasElement.height = canvasHeight;
  alert("canvasWidth=" + canvasElement.width + ", canvasHeight=" + canvasElement.height);

  var viewport = webController.getLastViewport();
  var viewportLeftLng = viewport.getLeft().getLng();
  var viewportLeftLat = viewport.getLeft().getLat();
  var viewportWidth = viewport.getRight().getLng() - viewportLeftLng;
  var viewportHeight = viewport.getRight().getLat() - viewportLeftLat;

  var factor = Math.min(canvasWidth / viewportWidth, canvasHeight / viewportHeight);

  alert("Viewport is north: " + viewportLeftLat + ", south: " + viewport.getRight().getLat() +
          ", east: " + viewport.getRight().getLng() + ", west: " + viewportLeftLng);

  context.save();
  context.clearRect(0, 0, canvasWidth, canvasHeight);
  context.fillStyle = "white";
  context.fillRect(0, 0, canvasWidth, canvasHeight);
  context.translate(x, y);
  context.scale(k, k);

  var drawables = webController.getDrawables();
  if (drawables == null) {
    alert("Got 'null' drawables!");
    return;
  }
  for (var j = 0; j < drawables.length; j++) {
    var drawable = drawables[j];
    context.beginPath();
    var points = drawable.getPolygon().getGeoPoints();
    if (points.size() < 3) {
      continue;
    }
    try {
      context.moveTo(
        transform(points.get(0).getLng(), viewportLeftLng, factor, null),
        transform(points.get(0).getLat(), viewportLeftLat, factor, canvasHeight)
      );
      for (var i = 1; i < points.size(); i++) {
        lineAndLog(context, points, i, factor, canvasHeight, viewportLeftLng, viewportLeftLat);
      }
      lineAndLog(context, points, 0, factor, canvasHeight, viewportLeftLng, viewportLeftLat);
      context.fillStyle = webController.getFillStyle(drawable);
      context.fill();
    }
    catch (e) {
      alert("Exception in draw block: " + e.message);
    }
  }

  context.restore();

  eventsDebouncer.debounce('farmBoundsChanged', function () {
    webController.farmBoundsChanged(
      viewportWidth / k,
      viewportHeight / k,
      reverseTransform(x, viewportLeftLng, factor, null),
      reverseTransform(y, viewportLeftLat, factor, canvasHeight)
    );
  });
}
