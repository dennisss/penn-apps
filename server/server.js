var express = require('express'),
	app = express(),
	server = require('http').Server(app),
	io = require('socket.io').listen(server);

app.use(express.static(__dirname + '/../web'));

server.listen(3000);

var started = false;
var arDrone = require('ar-drone');
var client  = arDrone.createClient();
var normalOrientation;
var currentOrientation;

var forward = false;
var numPeople = 0;
var lastDeflect;

var people = {};

io.on('connection', function(socket){
	console.log('connect');

	var id = undefined; //++numPeople;

	socket.on('disconnect', function(){
		console.log('disconnected');

		if(id !== undefined)
			delete people[id];

	});

	// Start the game
	socket.on('start', function(){
		console.log('starting')

		//numPeople = 0;
		lastDeflect = undefined;

		client.stop();

		forward = false;
		client.takeoff();
		client.up(0.3);

		setTimeout(function(){
			client.stop();

			//client.calibrate(0);

			setTimeout(function(){
				started = true;
				normalOrientation = currentOrientation;
			}, 3000);

		}, 5000);
	});

	// Stop the game
	socket.on('stop', function(){
		console.log('stop');
		client.stop();
		client.land();
	});


	var timeout = null;

	// Cause the copter to start moving in the opposite direction
	socket.on('deflect', function(data){
		if(id === undefined){
			if(Object.keys(people).length > 2){
				console.log('Warning: More than two people');
			}

			id = ++numPeople;
			people[id] = {
				socket: socket,
				score: 0
			};
		}



		if(lastDeflect != id)
		{
			lastDeflect = id;
			console.log('deflect' + id);
			forward = !forward;
			if(forward)
			{
			    client.front(0.15);
			}
			else
			{
			    client.back(0.15);
			}
			var angle = data.angle;
			if(!forward)
			{
				normalOrientation += angle;
			}
			else
			{
				normalOrientation -=angle;
			}

			if(timeout){
				clearTimeout(timeout);
				timeout = null
			}

			timeout = setTimeout(function(){

				// Miss
				console.log('MISS');
				socket.emit('stop');
				people[id].score++;

				for(var k in people){
					if(people.hasOwnProperty(k)){

						var s = [people[k].score];


						for(var j in people){
							if(people.hasOwnProperty(j)){

								if(j != k)
									s.push(people[j].score);
							}
						}

						people[k].socket.emit('score', {s: s});

					}
				}


			}, 10000);

		}

	});


	socket.on('miss', function(){


	});

	socket.on('check_boundries', function()
	{

	});

});


client.on('navdata', function(data)
{
	if(!data || !data.demo)
	{
		return;
	}

	//console.log(data.demo.rotation);

	currentOrientation = data.demo.rotation.clockwise;

	if(started)
	{
		var diff =  normalOrientation - currentOrientation;
		diff = ((diff + 180) % 360) - 180;

		console.log("normal: " + normalOrientation + ", current: " +currentOrientation + ", diff: " + diff);

		if(Math.abs(diff) < 5){
			// Zero velocity
			//client.clockwise(0);
			client.stop();
			if(forward)
			{	
				client.front(.15);
			}
			else
			{	
				client.back(.15);
			}
		}
		else if(diff < 0){
			// ccw
			console.log('GOING CCW');
			client.counterClockwise(0.1);
		}
		else{
			// cw
			console.log('GOING CW');
			client.clockwise(0.1);
		}
	}
	else
	{
		console.log("BATTERY: " + data.demo.batteryPercentage);
	}

});



process.on('SIGINT', function(){
	console.log('Landing/Exiting...');

	client.land();

	setTimeout(function(){
		process.exit();
	}, 2000);

});
