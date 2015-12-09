var express = require('express');
var inside = require('point-in-polygon');

var router = express.Router();

var polygon = {},
    point   = {};

function lonlatToPoly(lonlat) {
  var a = [];
  for (var i = 0, f; f = lonlat[i]; ++i) {
    a.push([f.lat, f.lon]);
  }
  return a;
}

module.exports = function(app, io) {

  /* GET home page. */
  app.get('/', function(req, res, next) {
    res.render('index', { title: 'FindMe :D'});
  });

  app.post('/register', function(req, res) {
    console.log('register');
    polygon[req.body.id] = lonlatToPoly(req.body.poly);
    console.log(polygon);
    res.json({ok : true});
    io.emit('register', polygon[req.body.id]);
  });

  app.post('/tracking', function(req, res) {
    console.log('update pos');
    console.log(req.body);
    point[req.body.id] = [+req.body.lat, +req.body.lon];
    console.log(point);
    console.log(inside(point[req.body.id], polygon));
    res.json({ok : true});
    io.emit('tracking', point[req.body.id]);
  });

  app.get('/status', function(req, res) {
    var id = req.param('id');
    console.log(id);
    console.log(point[id]);
    console.log(polygon[id]);
    if (!point[id] || !polygon[id]) {
      return res.end("noinfo");
    }
    if (inside(point[id], polygon[id])) {
      res.end("true " + point[id][1] + " " + point[id][0]);
    } else {
      res.end("false " + point[id][1] + " " + point[id][0]);
    }
    //res.end("false");
  });
};
