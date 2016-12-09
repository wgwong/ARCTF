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
			gameStarted = false;
			console.log("game over, final score: ", gameScore); //debug
			io.emit("gameOver", gameScore);
		}, timeRemaining);
	}

	that.setUp = function() {
		
		/*
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
		}*/

		/*captureData['a'] = {
			'ownedBy': null,
			'coordinates': [42.359048, -71.091650],
			'lastCaptured': new Date()
		}*/

				captureData['d'] = {
			'ownedBy': null,
			'coordinates': [42.35967288982897, -71.09133494916446],
			'lastCaptured': new Date()
		}

		captureData['a'] = {
			'ownedBy': null,
			'coordinates': [42.35798746159235, -71.09066459142902],
			'lastCaptured': new Date()
		}

		captureData['b'] = {
			'ownedBy': null,
			'coordinates': [42.358917775710246, -71.09128395564585],
			'lastCaptured': new Date()
		}

		captureData['c'] = {
			'ownedBy': null,
			'coordinates': [42.35850234356859, -71.09163832727943],
			'lastCaptured': new Date()
		}

		captureData['d'] = {
			'ownedBy': null,
			'coordinates': [42.35876385636331, -71.09125884736018],
			'lastCaptured': new Date()
		}

		captureData['e'] = {
			'ownedBy': null,
			'coordinates': [42.35849443372667, -71.09119565159523],
			'lastCaptured': new Date()
		}

		captureData['f'] = {
			'ownedBy': null,
			'coordinates': [42.35816602806608, -71.09086348535153],
			'lastCaptured': new Date()
		}

		captureData['g'] = {
			'ownedBy': null,
			'coordinates': [42.35792556920111, -71.09086268510657],
			'lastCaptured': new Date()
		}

		captureData['h'] = {
			'ownedBy': null,
			'coordinates': [42.35922805828082, -71.09171210896669],
			'lastCaptured': new Date()
		}

		captureData['i'] = {
			'ownedBy': null,
			'coordinates': [42.35952681105997, -71.09232465926713],
			'lastCaptured': new Date()
		}

		captureData['j'] = {
			'ownedBy': null,
			'coordinates': [42.358615153009055, -71.09193098300373],
			'lastCaptured': new Date()
		}

		captureData['k'] = {
			'ownedBy': null,
			'coordinates': [42.35880991687168, -71.09163784194301],
			'lastCaptured': new Date()
		}

		captureData['l'] = {
			'ownedBy': null,
			'coordinates': [42.3586906019411, -71.09058653479643],
			'lastCaptured': new Date()
		}

		captureData['m'] = {
			'ownedBy': null,
			'coordinates': [42.358441750045245, -71.0920536484671],
			'lastCaptured': new Date()
		}

		captureData['n'] = {
			'ownedBy': null,
			'coordinates': [42.358950883297524, -71.09188746521635],
			'lastCaptured': new Date()
		}

		captureData['o'] = {
			'ownedBy': null,
			'coordinates': [42.35778971493674, -71.0908547388364],
			'lastCaptured': new Date()
		}

		captureData['p'] = {
			'ownedBy': null,
			'coordinates': [42.35859532556448, -71.09089433787226],
			'lastCaptured': new Date()
		}

		captureData['q'] = {
			'ownedBy': null,
			'coordinates': [42.357861875195916, -71.09229477489245],
			'lastCaptured': new Date()
		}

		captureData['r'] = {
			'ownedBy': null,
			'coordinates': [42.359053882990146, -71.09062123132311],
			'lastCaptured': new Date()
		}

		captureData['s'] = {
			'ownedBy': null,
			'coordinates': [42.358942503784256, -71.09147513105336],
			'lastCaptured': new Date()
		}

		captureData['t'] = {
			'ownedBy': null,
			'coordinates': [42.35918868149386, -71.09202936145739],
			'lastCaptured': new Date()
		}

		captureData['u'] = {
			'ownedBy': null,
			'coordinates': [42.357839829860296, -71.09069780795802],
			'lastCaptured': new Date()
		}

		captureData['v'] = {
			'ownedBy': null,
			'coordinates': [42.359305645535095, -71.09219766822173],
			'lastCaptured': new Date()
		}

		captureData['w'] = {
			'ownedBy': null,
			'coordinates': [42.35849142425614, -71.09217769476007],
			'lastCaptured': new Date()
		}

		captureData['x'] = {
			'ownedBy': null,
			'coordinates': [42.35867443953944, -71.09192164902686],
			'lastCaptured': new Date()
		}

		captureData['y'] = {
			'ownedBy': null,
			'coordinates': [42.35793400965218, -71.09216784661794],
			'lastCaptured': new Date()
		}

		captureData['z'] = {
			'ownedBy': null,
			'coordinates': [42.35910684023733, -71.09047596174382],
			'lastCaptured': new Date()
		}

		captureData['aa'] = {
			'ownedBy': null,
			'coordinates': [42.359455496257894, -71.09044191715647],
			'lastCaptured': new Date()
		}

		captureData['ab'] = {
			'ownedBy': null,
			'coordinates': [42.35871537682867, -71.09088811713104],
			'lastCaptured': new Date()
		}

		captureData['ac'] = {
			'ownedBy': null,
			'coordinates': [42.35802891414787, -71.09069030832025],
			'lastCaptured': new Date()
		}
		

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
				if (!gameStarted) {
					var minutesLeft = 8;
					var ToSeconds = minutesLeft * 60;
					var ToMilliseconds = ToSeconds * 1000;
					timeRemaining = ToMilliseconds;

					gameStarted = true;
					gameScore = 0; //reset gameScore in case this was invoked on a game restart

					console.log("gameStart requested"); //debug

					//start timer, once timer runs out, emit a game over screen for all connected players
					startGameTimer(timeRemaining);

					io.emit('gameStart', timeRemaining);
				} else {
					//emit a message saying game already started
					console.log("game already started!"); //debug
				}
			});

			socket.on('check', function(checkKeyString, playerName) {

				if (gameStarted) {
					var checkKey = checkKeyString;
					var player = playerName;
					if (checkKey == treasureKey) {
						console.log("player " + player + " found key!"); //debug
						//win & increase score
						gameScore += 100;
						io.emit("win", player, gameScore, treasureKey);
						//reset game, get new treasure key

						getTreasureKey();

					} else {
						//get coordinates from point being checked against and compare the distance and return it
						var checkCoordinates = captureData[checkKey].coordinates;
						var treasureCoordinates = captureData[treasureKey].coordinates;

						/*
						function getEuclideanDistance(x1, y1, x2, y2) {
							return Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
						}*/

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