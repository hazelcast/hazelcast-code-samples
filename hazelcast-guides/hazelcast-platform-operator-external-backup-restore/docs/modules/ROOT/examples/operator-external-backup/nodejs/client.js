'use strict';

const { Client } = require('hazelcast-client');

const clientConfig = {
    network: {
        clusterMembers: [
            '<EXTERNAL-IP>'
        ],
        smartRouting: false
    }
};

(async () => {
    try {
        if (process.argv.length === 2) {
            console.error('You should pass an argument to run: fill or size');
        } else if (!(process.argv[2] === 'fill' || process.argv[2] === 'size')) {
            console.error('Wrong argument, you should pass: fill or size');
        } else {
            const client = await Client.newHazelcastClient(clientConfig);
            const map = await client.getMap('persistent-map');
            await map.put('key', 'value');
            const res = await map.get('key');
            if (res !== 'value') {
                throw new Error('Connection failed, check your configuration.');
            }
            console.log('Successful connection!');
            if (process.argv[2] === 'fill'){
                console.log('Starting to fill the map with random entries.');
                while (true) {
                    const randomKey = Math.floor(Math.random() * 100000);
                    await map.put('key' + randomKey, 'value' + randomKey);
                    const size = await map.size();
                    console.log(`Current map size: ${size}`);
                }
            } else {
                const size = await map.size();
                console.log(`Current map size: ${size}`);
            }
        }
    } catch (err) {
        console.error('Error occurred:', err);
    }
})();
