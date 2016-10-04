var express = require('express');
var router = express.Router();
//var Schema = require('../models/schema');

/*
	GET home page.

	GET /
	No request parameters
	Response:
		- appropriately rendered homepage (index.ejs) depending on whether the user is logged in or not
*/
router.get('/', function(req, res) {
	//check if logged in and render with the appropriate value
	res.render('index', {
		userProfile: undefined
	});
	/*
	if (req.currentUser) {
		Schema.Request.getRequestByFilter('Open', null, null, function(err, requests) {
		    if (err) {
		      utils.sendErrResponse(res, 500, 'An unknown error occurred.');
		    } else {
		    	requests.forEach(function(request) {
		    		request.candidates = request.candidates.map(function (ele) {
		    			return ele.username;
			    	});

			    	request.helpers = request.helpers.map(function (ele) {
			    		return ele.username;
			    	});
		    	});

		    	res.render('index', {
					userProfile: req.currentUser,
					requests: requests,
					colors: {"Open": "mediumseagreen", "In progress": "lightcoral", "Completed": "lightskyblue"} //color code
				});
		    }
		 });
	}
	//if not logged in, render with no userProfile data
	else {
		res.render('index', {
			userProfile: undefined
		});
	}*/
});

/*
	GET /payment/success
	Params:
		None
	Redirects to successful payment page.
*/

/*
	POST /
	Params:
		passedData: the requests to be posted onto the home page
*/

var phoneDatas = {};

router.post('/', function(req, res) {

	console.log("post / hit");

	phoneData = req.body.data;

	console.log("phone data: ", phoneData);

	if (phoneData.phoneId in phoneDatas) {
		phoneDatas[phoneData.phoneId].append({
			'location': phoneData.location,
			'timestamp': phoneData.timestamp
		})
	} else {
		phoneDatas[phoneData.phoneId] = [];
	}

	//req.body.

	//check if logged in and render with the appropriate value
	/*
	if (req.currentUser) {

		res.render('index', {
			userProfile: req.currentUser,
			requests: req.body.passedData,
			colors: {"Open": "mediumseagreen", "In progress": "lightcoral", "Completed": "lightskyblue"} //color code
		}, function (err, html) {
			res.send(html);
		});
	}
	//if not logged in, render with no userProfile data
	else {

		res.render('index', {
			userProfile: undefined
		});
	}*/
});

module.exports = router;