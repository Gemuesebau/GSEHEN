// from Google Maps API Doc, modified
function TypeControl(controlDiv, options, setTypeToValueFunction) {
    // Set CSS for the control border.
    var controlUI = document.createElement('div');
    controlUI.style.backgroundColor = '#fff';
    controlUI.style.border = '2px solid #fff';
    controlUI.style.borderRadius = '3px';
    controlUI.style.boxShadow = '0 2px 6px rgba(0,0,0,.3)';
    controlUI.style.cursor = 'pointer';
    controlUI.style.marginBottom = '22px';
    controlUI.style.textAlign = 'center';
    //controlUI.title = 'Click to recenter the map';
    controlDiv.appendChild(controlUI);

    // Set CSS for the control interior.
    var controlText = document.createElement('div');
    controlText.style.color = 'rgb(25,25,25)';
    controlText.style.fontFamily = 'Roboto,Arial,sans-serif';
    controlText.style.fontSize = '14px';
    controlText.style.lineHeight = '38px';
    controlText.style.paddingLeft = '5px';
    controlText.style.paddingRight = '5px';
    var controlHTML = '';
    for (var i=0; i<options.length; i++) {
    	var option = options[i];
    	var selected = true;
    	if (controlHTML != '') {
    		controlHTML += ' ';
        	selected = false;
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
    	input.addEventListener('change', function() { setTypeToValueFunction(this.value) });
    }
    controlUI.appendChild(controlText);
}
