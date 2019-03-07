var arrayUtilities = {
	transformArray: function(arr, startIndex, transformFunc) {
		for (var i = startIndex; i < arr.length; i++) {
			arr[i] = transformFunc(arr[i]);
		}
		return arr;
	},
	arrayToObject: function(arr, propNamesArray) {
		var obj = {};
		for (var i = 0; i < arr.length; i++) {
			obj[propNamesArray[i]] = arr[i];
		}
		return obj;
	},
	objArrayAggregate: function(arr, propName, initVal, stepFunc) {
		if (arr == null || arr.length == 0) {
			return null;
		}
		var val = initVal;
		for (var i = 0; i < arr.length; i++) {
			val = stepFunc(val, arr[i][propName]);
		}
		return val;
	},
	objArrayMin: function(arr, propName) {
		return arrayUtilities.objArrayAggregate(arr, propName,
			Number.MAX_VALUE,
			function (currentValue, arrayItemValue) {
				if (arrayItemValue < currentValue) {
					return arrayItemValue;
				} else {
					return currentValue;
				}
		});
	},
	objArrayMax: function(arr, propName) {
		return arrayUtilities.objArrayAggregate(arr, propName,
			Number.MIN_VALUE,
			function (currentValue, arrayItemValue) {
				if (arrayItemValue > currentValue) {
					return arrayItemValue;
				} else {
					return currentValue;
				}
		});
	},
	objArraySum: function(arr, propName) {
		return arrayUtilities.objArrayAggregate(arr, propName,
			0,
			function (currentValue, arrayItemValue) {
				return currentValue + arrayItemValue;
		});
	},
	objArrayMean: function(arr, propName) {
		if (arr == null || arr.length == 0) {
			return null;
		}
		return arrayUtilities.objArraySum(arr, propName) / arr.length;
	}
}
