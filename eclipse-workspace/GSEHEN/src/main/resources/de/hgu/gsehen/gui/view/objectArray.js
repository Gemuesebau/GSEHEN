function objectArray(javaObjectArray, objectConverter) {
	var result = [];
	for (var i=0; i<javaObjectArray.length; i++) {
		result.push(objectConverter(javaObjectArray[i]));
	}
	return result;
}
