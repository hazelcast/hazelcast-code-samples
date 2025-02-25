const Client = require('hazelcast-client').Client;
const fs = require('fs');
const path = require('path');

let sharedHazelcastClient = null;

let createClientConfig = () => {
    return {
        // Cluster connection settings are configured via environment variables.
        network: {
            hazelcastCloud: {
                discoveryToken: process.env.DISCOVERY_TOKEN
            },
            ssl: {
                enabled: true,
                sslOptions: {
                    ca: [fs.readFileSync(path.resolve(path.join(__dirname, 'ca.pem')))],
                    cert: [fs.readFileSync(path.resolve(path.join(__dirname, 'cert.pem')))],
                    key: [fs.readFileSync(path.resolve(path.join(__dirname, 'key.pem')))],
                    passphrase: process.env.KEYSTORE_PASSWORD,
                    checkServerIdentity: () => null
                }
            }
        },
        clusterName: process.env.CLUSTER_NAME,
        properties: {
            'hazelcast.client.cloud.url': 'https://api.viridian.hazelcast.com',
            'hazelcast.client.statistics.enabled': true,
            'hazelcast.client.statistics.period.seconds': 1,
        }
    }
};

module.exports.getClient = async () => {
    if (!sharedHazelcastClient) {
        console.log('Creating Hazelcast client...');
        sharedHazelcastClient = await Client.newHazelcastClient(createClientConfig());
    }

    return sharedHazelcastClient;
};

   
