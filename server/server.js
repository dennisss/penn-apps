var io = require('socket.io').listen(3000),
	autonomy = require('ardrone-autonomy');;

var mission = autonomy.createMission(),
	control = mission.control(),
	client = mission.client();



io.on('connection', function(socket){
	console.log('connect');

	socket.on('disconnect', function(){
		console.log('disconnected');
	});


	// Start the game
	socket.on('start', function(){
		console.log('starting')
		control.zero();
		client.takeoff(function(){
			control.altitude(0.2);
		});
	});

	// Stop the game
	socket.on('stop', function(){
		mission.client().stop();
		mission.client().land();
	});

	// Cause the copter to start moving in the opposite direction
	socket.on('deflect', function(data){

		var angle = data.angle;

	});


	socket.on('miss', function(){


	});


});



process.on('SIGINT', function(){
	mission.client().stop();
	mission.client().land(function(){
	});


	setTimeout(function(){
		process.exit();
	}, 6000);

});


/*
mission.takeoff()
       .zero()       // Sets the current state as the reference
       .altitude(1)  // Climb to altitude = 1 meter
       .forward(2)
       .right(2)
       .backward(2)
       .left(2)
       .hover(1000)  // Hover in place for 1 second
       .land();

mission.run(function (err, result)
    if (err) {
        console.trace("Oops, something bad happened: %s", err.message);
        mission.client().stop();
        mission.client().land();
    } else {
        console.log("Mission success!");
        process.exit(0);
    }
});
*/
