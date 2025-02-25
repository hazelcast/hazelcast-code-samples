'use strict';

const { Client } = require('hazelcast-client');

const clientConfig = {
    network: {
        clusterMembers: [
            'hz-hazelcast'
        ]
    }
};

(async () => {
    try {
        const client = await Client.newHazelcastClient(clientConfig);
        const map = await client.getMap('map');
        await map.put('key', 'value');
        const res = await map.get('key');
        if (res !== 'value') {
            throw new Error('Connection failed, check your configuration.');
        }
        console.log('Successful connection!');
        console.log('Starting to fill the map with random entries.');
        while (true) {
            const randomKey = Math.floor(Math.random() * 100000);
            await map.put('key' + randomKey, 'value' + randomKey);
            if (randomKey % 100 === 0) {
                const size = await map.size();
                console.log(`Current map size: ${size}`);
                await new Promise((resolve) => setTimeout(resolve, 1000));
            }
        }
    } catch (err) {
        console.error('Error occurred:', err);
    }
})();
