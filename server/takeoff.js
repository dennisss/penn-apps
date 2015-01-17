var autonomy = require('ardrone-autonomy');
var mission  = autonomy.createMission();

mission.takeoff().altitude(1).hover(50000);

mission.run(function (err,result)
{
});
