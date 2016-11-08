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
/*
	key = team key
	val = list of users in that team
*/

teamScores = {};
/*
	key = team key
	value = int for team score
*/

underCapture = {};
/*
	key = capture key, unique id for spawnpoint
	val = {
		team: {} //key = team name, val = # of people currently capturing it
		startedBy: //val = team that started request
		startTime: val //time when capture first started
	}
*/

captureTimers = {};
/*
	key = capture key, unique id for spawnpoint
	val = {
		team: {} //key = team name, val = # of people currently capturing it
		timer: val //time 	
	}
*/

captureAssignment = {};
/*
	maps player to their current capture assignment
	key = player id
	val = capture key
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
			'coordinates': [42.358566, -71.090742],
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

				io.emit("teamPopulate", teamAssignment);
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
				team = userData[player]['team'];


				if (capturePointKey in underCapture && 'team' in underCapture[capturePointKey] && team in underCapture[capturePointKey]['team'] && player in underCapture[capturePointKey]['team'][team]) {
					console.log("ignoring request");
					return;
				} else {
					console.log("checking request");
					console.log("capturePointKey in underCapture: ", capturePointKey in underCapture);
					if (capturePointKey in underCapture) {
						console.log("'team' in underCapture[capturePointKey]: ", 'team' in underCapture[capturePointKey]);
						if ('team' in underCapture[capturePointKey]) {
							console.log("team in underCapture[capturePointKey]['team']: ", team in underCapture[capturePointKey]['team']);
							if (team in underCapture[capturePointKey]['team']) {
								console.log("player in underCapture[capturePointKey]['team'][team]: ", player in underCapture[capturePointKey]['team'][team]);
							}
						}
					}
				}

				//reject if their team has already captured it and it's not currently under attack by the opposite team
				if (captureData[capturePointKey]['ownedBy'] === team &&
					(!capturePointKey in underCapture ||
						(capturePointKey in underCapture && captureTimers[capturePointKey] === null))) {
					console.log("team already captured/contesting it!");
					return;
				}

				now = new Date();

				
				console.log("this is now: ", now); //debug
				console.log("this is lastcaptured: ", captureData[capturePointKey]['lastCaptured']); //debug

				if (now - captureData[capturePointKey]['lastCaptured'] > /*30000*/ 1000) {

					console.log("capture point ready to be contested!"); //debug


					if (!(capturePointKey in underCapture)) {
						underCapture[capturePointKey] = {
							'team': {},
							'startTime': null
						}
					}

					if (!(team in underCapture[capturePointKey]['team'])) {
						underCapture[capturePointKey]['team'][team] = new Set();
					}
					underCapture[capturePointKey]['team'][team].add(player);
					console.log("adding player to list of players contesting the point")


					function capturePoint() {
						console.log("capturing point: ", capturePointKey);
						if (captureData[capturePointKey]['ownedBy'] !== null) {
							teamScores[captureData[capturePointKey]['ownedBy']] -= 1;
							console.log("someone else had this flag before we took it"); //debug
						}
						captureData[capturePointKey]['ownedBy'] = team;
						captureData[capturePointKey]['lastCaptured'] = new Date();
						underCapture[capturePointKey] = {
							'team': {},
							'startTime': null
						}

						teamScores[userData[player]['team']] += 1;

						console.log("capture data now: ", captureData); //debug
						console.log("team scores now: ", teamScores); //debug

						io.emit("gameStatusUpdate", captureData); //only run this on success
						io.emit("scoreUpdate", teamScores);
					}

					//clear previous timer
					function clearCaptureTimer() {
						clearTimeout(captureTimers[capturePointKey]);
						captureTimers[capturePointKey] = null
					}

					//start timer for capture function like overwatch
					function startCaptureTimer() {
						captureTime = new Date();
						underCapture[capturePointKey]['startTime'] = captureTime

						clientCaptureUpdater = setInterval(function() {
							io.emit("underCaptureUpdate", capturePointKey, underCapture[capturePointKey]);
						}, 500); //update clients of point under capture every half second

						captureTimer = setTimeout(function() {
							underCapture[capturePointKey] = {
								'team': {},
								'startTime': null
							};
							clearInterval(clientCaptureUpdater);
							capturePoint(capturePointKey, team); //upon successful timeout of 5 seconds, capture point
							clearTimeout(captureTimer);
							captureTimers[capturePointKey] = null;
						}, 5000);
						captureTimers[capturePointKey] = captureTimer;
					}

					if (captureTimers[capturePointKey] !== null && captureTimers[capturePointKey] !== undefined) {
						//TODO, helper function to get opposite team
						oppositeTeam = null
						if (team === 'red') {
							oppositeTeam = 'blue';
						} else {
							oppositeTeam = 'red';
						}

						if (!(oppositeTeam in underCapture[capturePointKey]['team'])) {
							console.log("oppositeTeam set doesn't exist, creating");
							underCapture[capturePointKey]['team'][oppositeTeam] = new Set();
						}

						console.log("our team count capture: ", underCapture[capturePointKey]['team'][team]);
						console.log("opp team count capture: ", underCapture[capturePointKey]['team'][oppositeTeam]);

						console.log("our team count capture: ", underCapture[capturePointKey]['team'][team].size);
						console.log("opp team count capture: ", underCapture[capturePointKey]['team'][oppositeTeam].size);

						if (underCapture[capturePointKey]['team'][team].size > underCapture[capturePointKey]['team'][oppositeTeam].size && underCapture[capturePointKey]['startedBy'] !== team) {
							console.log("us " + team + " have more people, restart capture timer on our side");
							clearCaptureTimer();
							startCaptureTimer();
						} //assume we only have 2 teams
						else {
							console.log("we don't have enough people yet to overtake the timer?");
						}
					} else {
						console.log("no previous timer in progress, start capture timer")
						underCapture[capturePointKey]['startedBy'] = team;
						console.log("capture request started by team: ", team);
						startCaptureTimer();
					}

				} else {
					console.log("capture point not ready to be contested yet, please wait");
				}
			});

		});

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

	}

	Object.freeze(that); //prevent any further modifications to the member fields and methods of this class
	return that;
})();

module.exports = Messagebase;