var arDrone = require('ar-drone');
var client = arDrone.createClient();

client.on('navdata', function(data)
{
	if(!data || !data.demo)
	{
		return;
	}	
	console.log(data.demo.rotation.clockwise);
});

