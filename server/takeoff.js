var arDrone = require('ar-drone');
var client  = arDrone.createClient();

client.takeoff();

client
  .after(50000, function() {
    this.land();
  });
