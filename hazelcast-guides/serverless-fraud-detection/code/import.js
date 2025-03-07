const hazelcast = require('./hazelcast');
const aws = require('aws-sdk');

let sharedS3Client = null;

let getS3Client = () => {
    if (!sharedS3Client) {
        console.log("Creating S3 client...")
        sharedS3Client = new aws.S3();
    }
    // Reuse the global S3 client instance between the function invocations.
    return sharedS3Client;
};

exports.handle = async (event, context, callback) => {
    console.log('Got event: ' + JSON.stringify(event));
    context.callbackWaitsForEmptyEventLoop = false;

    // Get a client connection to the Hazelcast cluster
    let hazelcastClient = await hazelcast.getClient();
    let map = await hazelcastClient.getMap('airports');
    if (await map.isEmpty() && event.Records.length > 0) {
        // Process the incoming S3 event
        let srcBucket = event.Records[0].s3.bucket.name;
        console.log('Handling upload into bucket \'' + srcBucket + '\'...');

        let srcKey = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, " "));
        let s3Client = getS3Client();
        let object = await s3Client.getObject({Bucket: srcBucket, Key: srcKey}).promise();
        // Read the JSON contents of the uploaded S3 object
        let airports = JSON.parse(object.Body);
        // Deserialize the JSON into an array and then re-map it to key-value pairs 
        // before storing the contents in a Hazelcast map.
        await map.putAll(airports.map(airport => ([airport.code, airport])));
        console.log('Imported data about ' + airports.length + ' airports');

        // Invoke an Amazon Lambda callback to return the result.
        // https://docs.aws.amazon.com/lambda/latest/dg/nodejs-prog-model-handler.html
        return callback(null, true);
    }

    return callback(null, false);
};


