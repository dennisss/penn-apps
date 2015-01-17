var autonomy = require('ardrone-autonomy');
var mission  = autonomy.createMission();

mission.client().stop();
mission.client().land();
