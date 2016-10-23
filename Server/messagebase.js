//var User = require('./models/schema').User;

//var loggedInUsers = {};

//var messages = {}; //key = target_user, value = dictionary where key = src, value = array of message tuples (msg, date, self?) including self, msg


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
			socket.on('session', function(data){
				console.log("new phone connected: ", data);

				userData[data.player] = {'name': 'test', 'team': 'gray', 'coordinates': []};

				io.emit("gameStatusUpdate", captureData);
			});

			socket.on('playerUpdate', function(data) {
				console.log("playerUpdate called, data: ", data);
				player = data.address;
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
				console.log("emitting: ", userData);
				io.emit("playerUpdate_confirm", userData);
			});

			socket.on('capture', function(data) {
				console.log("someone just tried to capture something: ", data);

				capturePointKey = data.capturePoint;
				player = data.player;

				now = new Date();

				if (now - capturePointKey['lastCaptured'] > 30000) {

					if (captureData[capturePointKey]['ownedBy'] == null) {
						captureData[capturePointKey]['ownedBy'] = playerTimestamp;
						captureData[capturePointKey]['lastCaptured'] = now;
						io.emit("gameStatusUpdate", captureData); //only run this on success
					}
					else {
						//handle logic regarding team battle over a spawnpoint
						
						

						//if successful
						captureData[capturePointKey]['ownedBy'] = playerTimestamp;
						captureData[capturePointKey]['lastCaptured'] = now;
						io.emit("gameStatusUpdate", captureData); //only run this on success
					}
				}
			});

		});

		setInterval( () => {
			io.emit("session", "this is a timed message " + userData);
		}, 5000); // 1000 = 1 sec

		setInterval( () => {
			now = new Date();
			for (pIndex in userData) {
				playerTimestamp = userData[pIndex]['timestamp'];
				if (now - playerTimestamp > 60000) { //if last update was more than a minute ago
					io.emit("playerShutdown", userData[pIndex]); //emit to everyone that player x has been disconnected
				}
			}
		}, 20000);
	}

	that.login = function(username) {
		loggedInUsers[username] = true;
		if (io) {
			io.emit("login user", username);
		}
	}

	that.logout =  function(username) {
		loggedInUsers[username] = false;
		if (io) {
			io.emit("logout user", username);
		}
	}

	that.getActiveUsers = function() {
		return loggedInUsers;
	}

	that.getOfflineUsers = function(cb) {
		User.getAllUsers(function(err, result) {
			if (err) {
				console.error(err);
				cb(err);
			} else {
				if (result.length > 0) {
					var userList;
					if (result.length > 1) {
						userList = result.reduce(function(prev, cur, i, arr) {
							if (i == 1) {
								return [prev.username, cur.username]
							} else {
								return prev.concat(cur.username);
							}
						});
					} else {
						userList = [result[0].username];
					}
					var offlineUsers = userList.filter(function(user) {
						return Object.keys(loggedInUsers).indexOf(user) === -1 || !loggedInUsers[user];
					});
					cb(null, offlineUsers);
				} else {
					cb(null, []);
				}
			}
		});
	}

	//get all messages associated with the specified user
	that.getMessagesByUsername = function(username) {

		if (Object.keys(messages).indexOf(username) !== -1) {
			return messages[username];
		} else {
			return {};
		}
	}

	Object.freeze(that); //prevent any further modifications to the member fields and methods of this class
	return that;
})();

module.exports = Messagebase;