var debug = true;


// Report coverage for each test case
if (! window.jscoverage_report) {
  window.jscoverage_report = function jscoverage_report(dir) {
    var createRequest = function () {
      if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
      }
      else if (window.ActiveXObject) {
        return new ActiveXObject("Microsoft.XMLHTTP");
      }
    };

    var pad = function (s) {
      return '0000'.substr(s.length) + s;
    };

    var json = [];
    for (var file in _$jscoverage) {
      var coverage = _$jscoverage[file].lineData;

      var array = [];
      var length = coverage.length;
      for (var line = 0; line < length; line++) {
        var value = coverage[line];
        if (value === undefined || value === null) {
          value = 'null';
        }
        array.push(value);
      }

      var jsonStr = jscoverage_quote(file)
                  + ':{"lineData":['
                  + array.join(',')
                  + '],"branchData":'
                  + convertBranchDataLinesToJSON(_$jscoverage[file].branchData) 
                  + ', "switchData":'
                  + convertSwitchDataToJSON(_$jscoverage[file].switchData)
                  + '}';

      json.push(jsonStr);
    }
    json = '{' + json.join(',') + '}';

    if(debug){
      // Output debug information (coverage data for each test)
      console.log("============================== " + dir + " ====================================");
      console.log(json);
      console.log("===============================================================================");
      console.log("");
    }

    var request = createRequest();
    var url = '/jscoverage-store';
    if (dir) {
      url += '/' + encodeURIComponent(dir);
    }
    request.open('POST', url, false);
    request.setRequestHeader('Content-Type', 'application/json');
    request.send(json);
    if (request.status === 200 || request.status === 201 || request.status === 204) {
      // Clear coverage for the current test case
      clearCoverage();
      return request.responseText;
    }
    else {
      throw request.status;
    }
  };
}

//Report initialization report data before any test
if (! window.jscoverage_init_report) {
  window.jscoverage_init_report = function jscoverage_init_report() {
    var createRequest = function () {
      if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
      }
      else if (window.ActiveXObject) {
        return new ActiveXObject("Microsoft.XMLHTTP");
      }
    };

    var pad = function (s) {
      return '0000'.substr(s.length) + s;
    };

    var json = [];
    for (var file in _$jscoverage) {
      var coverage = _$jscoverage[file].lineData;

      var array = [];
      var length = coverage.length;
      for (var line = 0; line < length; line++) {
        var value = coverage[line];
        if (value === undefined || value === null) {
          value = 'null';
        }
        array.push(value);
      }

      var jsonStr = jscoverage_quote(file)
                  + ':{"lineData":['
                  + array.join(',')
                  + '],"branchData":'
                  + convertBranchDataLinesToJSON(_$jscoverage[file].branchData) 
                  + ', "switchData":'
                  + convertSwitchDataToJSON(_$jscoverage[file].switchData)
                  + '}';

      json.push(jsonStr);
    }
    json = '{' + json.join(',') + '}';

    if(debug){
      // Output debug information (coverage data for each test)
      console.log("============================== Initialization  ====================================");
      console.log(json);
      console.log("===============================================================================");
      console.log("");
    }

    var request = createRequest();
    var url = '/jscoverage-store/initialize';
 
    request.open('POST', url, false);
    request.setRequestHeader('Content-Type', 'application/json');
    request.send(json);
    if (request.status === 200 || request.status === 201 || request.status === 204) {
      return request.responseText;
    }
    else {
      throw request.status;
    }
  };
}

// Report coverage for all test cases
if (! window.jscoverage_reportall) {
  window.jscoverage_reportall = function jscoverage_reportall() {
    var createRequest = function () {
      if (window.XMLHttpRequest) {
        return new XMLHttpRequest();
      }
      else if (window.ActiveXObject) {
        return new ActiveXObject("Microsoft.XMLHTTP");
      }
    };

    var json = {"test" : "ok"};
    var request = createRequest();
    var url = '/jscoverage-store/storeAll';
    request.open('POST', url, false);
    request.setRequestHeader('Content-Type', 'application/json');
    request.send(json);
    if (request.status === 200 || request.status === 201 || request.status === 204) {
      return request.responseText;
    }
    else {
      throw request.status;
    }
  };
}

// Clear statement and branch coverage
function clearCoverage(){
  for (var file in _$jscoverage) {
      var coverage = _$jscoverage[file].lineData;
      var branchCoverage = _$jscoverage[file].branchData;

      var array = [];
      var length = coverage.length;
      for (var line = 0; line < length; line++) {
        var value = coverage[line];
        // Reset line coverage
        if (value === undefined || value === null) {
          _$jscoverage[file].lineData[line] = null;
        }else{
          _$jscoverage[file].lineData[line] = 0;
        }

        // Reset branch coverage
        var branchValue = branchCoverage[line];
        if(branchValue != undefined && branchValue != null){
          _$jscoverage[file].branchData[line][1].evalFalse = 0;
          _$jscoverage[file].branchData[line][1].evalTrue = 0;
        }     
      }

      // Reset switch coverage
      var switchCoverage = _$jscoverage[file].switchData;
      for(var line =0; line < switchCoverage.length; line++){
        var perSwitchData = switchCoverage[line];
        for(var i=1; i<perSwitchData.length; i++){
          var value = perSwitchData[i];
          if(value != undefined && value != null){
            _$jscoverage[file].switchData[line][i] = 0;
          }
        }
      }
  }

}

// Convert switch coverage data([][]) to json string
function convertSwitchDataToJSON(switchData){
  if(switchData === undefined)
    return '[]';

  var json = [];
  var perSwitch = [];
  for(var i=0; i<switchData.length; i++){
	var line1 = switchData[i];
	for(var j=0; j<line1.length; j++){
		var value = line1[j];
		 if (value === undefined || value === null) {
	          value = 0;
	        }
		 perSwitch.push(value);
	}
    var line = '[' + perSwitch.join(',') + ']';
    json.push(line);
  }

  return '[' + json.join(',') + ']';
}
