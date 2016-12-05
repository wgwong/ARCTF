userData = {};
/*
	key = userKey
	value = {
		name: string
		team: string
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

treasureKey = null;

timeRemaining = null;

gameStarted = false;

gameScore = 0;

gameTimer = null;

var Messagebase = (function Messagebase() {
	var that = Object.create(Messagebase.prototype);

	var io = null;

	function getTreasureKey() {
		function getRandomInt(min, max) {
			return Math.floor(Math.random() * (max - min + 1)) + min;
		}
		var captureDataArray = Object.keys(captureData);
		var treasureIndex = getRandomInt(0, captureDataArray.length-1);

		treasureKey = captureDataArray[treasureIndex];

		console.log("treasure key set to: ", treasureKey); //debug
	}

	function startGameTimer(timeRemaining) {
		gameTimer = setTimeout(function() {
			//game end
			console.log("game over, final score: ", gameScore); //debug
			io.emit("gameOver", gameScore);
		}, timeRemaining);
	}

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
			'coordinates': [42.358566, -71.090742],
			'lastCaptured': new Date()
		}

		captureData['f'] = {
			'ownedBy': null,
			'coordinates': [42.363231, -71.099789],
			'lastCaptured': new Date()
		}
		/*captureData['a'] = {
			'ownedBy': null,
			'coordinates': [42.359048, -71.091650],
			'lastCaptured': new Date()
		}*/

		getTreasureKey();
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

				userData[player] = {'name': player}; //debug: is this line necessary?

				socket.emit("gameStatusPopulate", captureData);

				console.log("sent capture data");

				if (gameStarted) {
					console.log("game already started, calling gameStart"); //debug
					io.emit('gameStart', timeRemaining);
				}

				//setTimeout(function(){}, 3000);
			});

			socket.on('gameStart', function() {
				var minutesLeft = 5./60;
				var ToSeconds = minutesLeft * 60;
				var ToMilliseconds = ToSeconds * 1000;
				timeRemaining = ToMilliseconds;

				gameStarted = true;

				console.log("gameStart requested"); //debug

				//start timer, once timer runs out, emit a game over screen for all connected players
				startGameTimer(timeRemaining);

				io.emit('gameStart', timeRemaining);
			});

			socket.on('check', function(checkKeyString, playerName) {

				if (gameStarted) {
					var checkKey = checkKeyString;
					var player = playerName;
					if (checkKey == treasureKey) {
						console.log("player " + player + " found key!"); //debug
						//win & increase score
						gameScore += 1;
						io.emit("win", player, gameScore);
						//reset game, get new treasure key

						getTreasureKey();

					} else {
						//get coordinates from point being checked against and compare the distance and return it
						var checkCoordinates = captureData[checkKey].coordinates;
						var treasureCoordinates = captureData[treasureKey].coordinates;

						function getEuclideanDistance(x1, y1, x2, y2) {
							return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
						}

						function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {
						  var R = 6371; // Radius of the earth in km
						  var dLat = deg2rad(lat2-lat1);  // deg2rad below
						  var dLon = deg2rad(lon2-lon1); 
						  var a = 
						    Math.sin(dLat/2) * Math.sin(dLat/2) +
						    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
						    Math.sin(dLon/2) * Math.sin(dLon/2)
						    ; 
						  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
						  var d = R * c; // Distance in km
						  return d;
						}

						function deg2rad(deg) {
						  return deg * (Math.PI/180)
						}

						var euclideanDistance = getEuclideanDistance(checkCoordinates[0], checkCoordinates[1], treasureCoordinates[0], treasureCoordinates[1]);
						euclideanDistance *= 10000;
						euclideanDistance = getDistanceFromLatLonInKm(checkCoordinates[0], checkCoordinates[1], treasureCoordinates[0], treasureCoordinates[1]);
						euclideanDistance *= 1000;

						var heatSignal = null;

						console.log("euclideanDistance: ", euclideanDistance);

						if (euclideanDistance > 150) {
							heatSignal = 4; //"really far";
						} else if (euclideanDistance > 100) {
							heatSignal = 3; //"far";
						} else if (euclideanDistance > 50) {
							heatSignal = 2; //"close";
						} else {
							heatSignal = 1; //"nearby";
						}

						socket.emit("wrongPoint", checkKey, heatSignal);
					}
				} else {
					console.log("player tried to capture without starting game first"); //debug, should never reach here
				}
			});

		});

		/*
		setInterval( function() {
			now = new Date();
			for (pIndex in userData) {
				playerTimestamp = userData[pIndex]['timestamp'];
				if (now - playerTimestamp > 30000) { //if last update was more than 30s ago
					console.log("player disconnected: ", userData[pIndex]); //debug
					delete userData[pIndex];
					io.emit("playerShutdown", userData[pIndex]); //emit to everyone that player x has been disconnected
				}
			}
		}, 20000);
		*/

	}

	Object.freeze(that); //prevent any further modifications to the member fields and methods of this class
	return that;
})();

module.exports = Messagebase;