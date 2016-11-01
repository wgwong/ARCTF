userData = {};
/*
	key = userKey
	value = {
		name: string
		team: string
		coordinates: tuple of floats
		timestamp: datetime
	}
*/
captureData = {};
/*
	key = unique id for spawnpoint
	value = {
		ownedBy: string //team name
		coordinates: tuple of floats
		lastCaptured: datetime
	}
*/

teams = {};

teamScores = {};
/*
	key = team key
	value = int for team score
*/

var Messagebase = (function Messagebase() {
	var that = Object.create(Messagebase.prototype);

	var io = null;

	var COORD_SIZE_CAP = 20;

	that.setUp = function() {
		captureData['a'] = {
			'ownedBy': null,
			'coordinates': [42.357836,-71.093371],
			'lastCaptured': new Date()
		}

		captureData['b'] = {
			'ownedBy': null,
			'coordinates': [42.358328, -71.093843],
			'lastCaptured': new Date()
		}

		captureData['c'] = {
			'ownedBy': null,
			'coordinates': [42.357202, -71.092641],
			'lastCaptured': new Date()
		}

		captureData['d'] = {
			'ownedBy': null,
			'coordinates': [42.357242, -71.095119],
			'lastCaptured': new Date()
		}

		captureData['e'] = {
			'ownedBy': null,
			'coordinates': [42.359613, -71.091231],
			'lastCaptured': new Date()
		}

		captureData['f'] = {
			'ownedBy': null,
			'coordinates': [42.363231, -71.099789],
			'lastCaptured': new Date()
		}

		teams["red"] = {};
		teams["blue"] = {};
		teamScores["red"] = 0;
		teamScores["blue"] = 0;
	}

	that.setIO = function(IO) {

		that.setUp();

		io = IO;

		//io connection
		io.on('connection', function(socket){
			console.log("connection established");

			socket.on('disconnect', function(){
			});

			//on initial connection, create player data
			//then send current state of the game
			socket.on('session', function(player){
				console.log("new phone connected: ", player);

				teamAssignment = "";

				if (Object.keys(teams["red"]).length <= Object.keys(teams["blue"]).length) {
					teamAssignment = "red";
					teams["red"][player] = true;

				}
				else {
					teamAssignment = "blue";
					teams["blue"][player] = true;
				}

				/*
				if (Object.keys(teams["blue"]).length <= Object.keys(teams["red"]).length) {
					teamAssignment = "blue";
					teams["blue"][player] = true;
				}
				else {
					teamAssignment = "red";
					teams["red"][player] = true;
				}*/

				userData[player] = {'name': player, 'team': teamAssignment, 'coordinates': []};

				console.log("team assignments: ", teams);

				setTimeout(function(){}, 3000);

				console.log("first session: capture data: ", captureData); //debug
				console.log("first sessiion: team scores now: ", teamScores); //debug

				io.emit("gameStatusPopulate", captureData);
				io.emit("scoreUpdate", teamScores);
			});

			socket.on('playerUpdate', function(data) {
				//console.log("playerUpdate called, data: ", data);
				player = data.player;
				latitude = data.latitude;
				longitude = data.longitude;

				if (!(player in userData)) {
					userData[player] = {'name': 'test', 'team': 'gray', 'coordinates': []};
					//should never happen since we must call on session first
				}

				if (userData[player]['coordinates'].length > COORD_SIZE_CAP) {
					userData[player]['coordinates'].pop(0);
				}
				userData[player]['coordinates'].push([latitude, longitude]);
				userData[player]['timestamp'] = new Date();

				//DEBUG
				//console.log("emitting: ", userData);
				io.emit("playerUpdate_confirm", userData);
			});

			socket.on('capture', function(data) {
				console.log("someone just tried to capture something: ", data);

				capturePointKey = data.capturePoint;
				player = data.player;

				now = new Date();

				console.log("this is now: ", now); //debug
				console.log("this is lastcaptured: ", captureData[capturePointKey]['lastCaptured']); //debug

				console.log("?", now - captureData[capturePointKey]['lastCaptured']); //debug				

				if (now - captureData[capturePointKey]['lastCaptured'] > /*30000*/ 1000) {

					console.log("after 1 second"); //debug

					if (captureData[capturePointKey]['ownedBy'] == null) {

						console.log("neutral flag"); //debug

						captureData[capturePointKey]['ownedBy'] = userData[player]['team'];
						captureData[capturePointKey]['lastCaptured'] = now;

						teamScores[userData[player]['team']] += 1;

						console.log("capture data now: ", captureData); //debug
						console.log("team scores now: ", teamScores); //debug

						io.emit("gameStatusUpdate", captureData); //only run this on success
						io.emit("scoreUpdate", teamScores);
					}
					else {
						//handle logic regarding team battle over a spawnpoint
						
						console.log("someone else had this flag before we took it"); //debug

						//if successful

						teamScores[captureData[capturePointKey]['ownedBy']] -= 1;

						captureData[capturePointKey]['ownedBy'] = userData[player]['team'];
						captureData[capturePointKey]['lastCaptured'] = now;

						teamScores[userData[player]['team']] += 1;


						console.log("capture data now: ", captureData); //debug
						console.log("team scores now: ", teamScores); //debug

						io.emit("gameStatusUpdate", captureData); //only run this on success
						io.emit("scoreUpdate", teamScores);
					}
				}
			});

		});

		
		setInterval( function() {
			//io.emit("session", "this is a timed message " + userData);
		}, 5000); // 1000 = 1 sec

		setInterval( function() {
			now = new Date();
			for (pIndex in userData) {
				playerTimestamp = userData[pIndex]['timestamp'];
				if (now - playerTimestamp > 60000) { //if last update was more than a minute ago
					io.emit("playerShutdown", userData[pIndex]); //emit to everyone that player x has been disconnected
				}
			}
		}, 20000);

	}

	Object.freeze(that); //prevent any further modifications to the member fields and methods of this class
	return that;
})();

module.exports = Messagebase;