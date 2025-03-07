'use strict';

const { Client } = require('hazelcast-client');

const clientConfig = {
  network: {
    clusterMembers: [
      '<EXTERNAL-IP>'
    ],
    ssl: {
      enabled: true,
      sslOptionsFactoryProperties: {
        caPath: 'example.crt',
        servername: 'example',
      }
    }
  }
};

(async () => {
  try {
    const client = await Client.newHazelcastClient(clientConfig);
    console.log('Successful connection!');
  } catch (err) {
    console.error('Error occurred:', err);
  }
})();
