// var L = require('leaflet');
var $ = require('jquery');
var io = require('socket.io-client');

window.point = null;
window.polygon = null;

$(function () {
  window.map = L.map('map').setView([4.8143, -75.6946], 15);

  L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);

  /*
  L.marker([51.5, -0.09]).addTo(map)
    .bindPopup('Find Me.<br> Here.')
    .openPopup();


  var circle = L.circle([51.508, -0.11], 500, {
    color: 'red',
    fillColor: '#f03',
    fillOpacity: 0.5
  }).addTo(map);
  */

  var socket = io();
  socket.on('register', function(data) {
    // console.log(data);
    if (window.polygon)
      window.map.removeLayer(window.polygon);

    window.polygon = L.polygon(data).addTo(map);
  });

  socket.on('tracking', function(data) {
    // console.log(data);
    if (!window.point)
      window.point = L.marker(data).addTo(window.map);
    var nLL = new L.LatLng(data[0], data[1]);
    window.point.setLatLng(nLL);
  });
});

