alert("Farm view (re)loaded, now running JavaScript");

var canvasElement = document.createElement("canvas");
canvasElement.width = webController.getCanvasWidth();
canvasElement.height = webController.getCanvasHeight();
document.body.appendChild(canvasElement);

var canvas = d3.select("canvas");
var context = canvas.node().getContext("2d");

canvas.call(d3.zoom()
  .scaleExtent([0.2, 5])
  .on("zoom", function () {
    drawPoints(d3.event.transform.x, d3.event.transform.y, d3.event.transform.k);
  })
);
drawPoints(0, 0, 1); // defaults

function transform(pointCoordValue, viewPortCoordValue, factor, flipHeightOrNull) {
  var value = (pointCoordValue - viewPortCoordValue) * factor;
  if (flipHeightOrNull == null) {
    return value;
  }
  else {
    return flipHeightOrNull - value;
  }
}

function lineAndLog(context, points, pointIndex, viewPort, factor, canvasHeight) {
  var x = transform(points.get(pointIndex).getLng(), viewPort.getLeft().getLng(), factor, null);
  var y = transform(points.get(pointIndex).getLat(), viewPort.getLeft().getLat(), factor, canvasHeight);
  context.lineTo(x, y);
  //alert("*** Added line end point: [" + x + ", " + y + "]");
}

function drawPoints(x, y, k) {
  context.save();
  var canvasWidth = canvas.property("width");
  var canvasHeight = canvas.property("height");
  context.clearRect(0, 0, canvasWidth, canvasHeight);
  var viewPort = webController.getLastViewPort();
  var viewPortWidth = viewPort.getRight().getLng() - viewPort.getLeft().getLng();
  var viewPortHeight = viewPort.getRight().getLat() - viewPort.getLeft().getLat();
  var factor = Math.min(canvasWidth / viewPortWidth, canvasHeight / viewPortHeight);
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
      return;
    }
    //alert("Got points: " + points);
    try {
      context.moveTo(
        transform(points.get(0).getLng(), viewPort.getLeft().getLng(), factor, null),
        transform(points.get(0).getLat(), viewPort.getLeft().getLat(), factor, canvasHeight)
      );
      // the actual lines
      for (var i = 1; i < points.size(); i++) {
        lineAndLog(context, points, i, viewPort, factor, canvasHeight);
      }
      lineAndLog(context, points, 0, viewPort, factor, canvasHeight);
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
