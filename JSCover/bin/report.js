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

      json.push(jscoverage_quote(file) + ':{"lineData":[' + array.join(',') + '],"branchData":' + convertBranchDataLinesToJSON(_$jscoverage[file].branchData) + '}');
    }
    json = '{' + json.join(',') + '}';

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
  }

}
