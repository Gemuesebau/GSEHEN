alert("Farm view (re)loaded, now running JavaScript");

var canvasElement = document.createElement("canvas");
canvasElement.width = webController.getCanvasWidth();
canvasElement.height = webController.getCanvasHeight();
document.body.appendChild(canvasElement);

var canvas = d3.select("canvas");
var canvasWidth = canvas.property("width");
var canvasHeight = canvas.property("height");
var viewport = webController.getLastViewport();
var viewportWidth = viewport.getRight().getLng() - viewport.getLeft().getLng();
var viewportHeight = viewport.getRight().getLat() - viewport.getLeft().getLat();
var factor = Math.min(canvasWidth / viewportWidth, canvasHeight / viewportHeight);
var context = canvas.node().getContext("2d");

alert("Viewport is north: " + viewport.getLeft().getLat() + ", south: " + viewport.getRight().getLat() + ", east: " + viewport.getRight().getLng() + ", west: " + viewport.getLeft().getLng());

canvas.call(d3.zoom()
  .scaleExtent([0.1, 10])
  .on("zoom", function () {
    drawPoints(d3.event.transform.x, d3.event.transform.y, d3.event.transform.k);
  })
);
drawPoints(0, 0, 1); // defaults

function transform(pointCoordValue, viewportCoordValue, factor, flipHeightOrNull) {
  var value = (pointCoordValue - viewportCoordValue) * factor;
  if (flipHeightOrNull == null) {
    return value;
  }
  else {
    return flipHeightOrNull - value;
  }
}

function lineAndLog(context, points, pointIndex, viewport, factor, canvasHeight) {
  var x = transform(points.get(pointIndex).getLng(), viewport.getLeft().getLng(), factor, null);
  var y = transform(points.get(pointIndex).getLat(), viewport.getLeft().getLat(), factor, canvasHeight);
  context.lineTo(x, y);
}

function drawPoints(x, y, k) {
  context.save();
  context.clearRect(0, 0, canvasWidth, canvasHeight);
  context.translate(x, y);
  context.scale(k, k);
  var drawables = webController.getDrawables();
  if (drawables == null) {
    alert("Got 'null' drawables!");
    return;
  }
  //alert("Got drawables: " + drawables);
  for (var j = 0; j < drawables.length; j++) {
    var drawable = drawables[j];
    context.beginPath();
    var points = drawable.getPolygon().getGeoPoints();
    if (points.size() < 3) {
      continue;
    }
    //alert("Got points: " + points);
    try {
      context.moveTo(
        transform(points.get(0).getLng(), viewport.getLeft().getLng(), factor, null),
        transform(points.get(0).getLat(), viewport.getLeft().getLat(), factor, canvasHeight)
      );
      // the actual lines
      for (var i = 1; i < points.size(); i++) {
        lineAndLog(context, points, i, viewport, factor, canvasHeight);
      }
      lineAndLog(context, points, 0, viewport, factor, canvasHeight);
      // color by type
      context.fillStyle = webController.getFillStyle(drawable);
      context.fill();
    }
    catch (e) {
      alert("Exception in draw block: " + e.message);
    }
  }
  context.restore();
}
