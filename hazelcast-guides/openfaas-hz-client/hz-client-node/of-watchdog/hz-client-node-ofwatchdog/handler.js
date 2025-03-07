'use strict'
const { Client } = require('hazelcast-client');

function between(min, max) {
  return Math.floor(
    Math.random() * (max - min) + min
  )
}

let hz;
let map;

module.exports = async (event, context) => {
  const cfg = {
    network: {
        clusterMembers: [
            'hz-hazelcast.default'
        ]
    }
  };
  if(!hz){
    hz = await Client.newHazelcastClient(cfg);
  }

  // Get the Distributed Map from Cluster
  if(!map){
    map = await hz.getMap('map');
  }

  const number = between(1, 1000000);
  await map.put(`${number}`, `value-${number}`);
  const size = await map.size();
  
  return context
    .status(200)
    .succeed({"size" : size})
}