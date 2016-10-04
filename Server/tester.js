var $ = require('jquery')
var http = require('http');


var options = {
	host: 'localhost',
	port: 3000,
	path: "/"
}

var post_data = {
	'phoneId': "testid",
	'location': "testloc",
	'timestamp': "testtimestamp"
}

var post_options = {
	host: 'localhost',
	port: 3000,
	path: "/",
	method: "POST",
	headers: {
		'Content-Type' : 'application/javascript'
	}
}


var html = '';

http.get(options, function (results){

	console.log("done!");

	var post_req = http.request(post_options, function(res) {
      res.setEncoding('utf8');
      res.on('data', function (chunk) {
          console.log('Response: ' + chunk);
	    });
	});

	post_req.write(post_data);
	post_req_end();

});