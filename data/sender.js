var request = require('request');
var sleep = require('sleep');

var data = require('./path').data;

var i = 0;
function sp() {
  data[i].id = 'fBo3T-Cdr5E';
  request({
    uri: "http://maps.utp.edu.co:3001/tracking",
    method: "POST",
    json: data[i]
  }, function(error, response, body) {
    console.log("sent");
    console.log(data[i]);
    i = (i + 1) % data.length;
    sleep.usleep(90000);
    sp();
  });
};


sp();
