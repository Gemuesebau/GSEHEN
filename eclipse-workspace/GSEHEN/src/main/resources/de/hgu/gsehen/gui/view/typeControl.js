// from Google Maps API Doc, modified
function TypeControl(controlDiv, options, setTypeToValueFunction) {
  var controlUI = document.getElementById('types-container');
  controlDiv.appendChild(controlUI);
  var controlText = document.getElementById('types-box');

  var controlHTML = '';
  for (var i=0; i<options.length; i++) {
    var option = options[i];
    var selected = true;
    if (controlHTML != '') {
      controlHTML += ' ';
      selected = false; // select (only) the first radio button
    }
    controlHTML += (
      '<input type="radio" name="polygonType" value="' + option.key + '"' +
      (selected ? " checked" : "") +
      '>' + option.title + '</input>'
    );
  }
  controlText.innerHTML = controlHTML;

  var inputs = controlText.getElementsByTagName("input");
  for (var i=0; i<inputs.length; i++) {
    var input = inputs[i];
    input.addEventListener('change', function () { setTypeToValueFunction(this.value) });
  }
  controlUI.appendChild(controlText);
}
