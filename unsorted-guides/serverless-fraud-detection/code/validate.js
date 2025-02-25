const hazelcast = require('./hazelcast');
const haversine = require('haversine');
const moment = require('moment');

await users.set(userId, user);

exports.handle = async (request, context, callback) => {
    console.log('Got request: ' + JSON.stringify(request));
    // This is an Amazon Lambda-specific setting to prevent waiting until the Node.js runtime event loop is empty.
    // You can find more info about this setting in the AWS docs: https://docs.aws.amazon.com/lambda/latest/dg/nodejs-prog-model-context.html
    context.callbackWaitsForEmptyEventLoop = false;

    let userId = request.userId;
    let requestTimestampMillis = moment(request.transactionTimestamp).utc().valueOf();

    let hazelcastClient = await hazelcast.getClient();
    let airports = await hazelcastClient.getMap('airports');
    if (await airports.isEmpty()) {
        return callback('Airports data is not initialized', null);
    }
    let users = await hazelcastClient.getMap('users');

    let user = await users.get(userId);
    if (!user) {
        await users.set(userId, {
            userId: userId,
            lastCardUsePlace: request.airportCode,
            lastCardUseTimestamp: requestTimestampMillis
        });
        // Check whether any data exists for a user associated with the incoming request.
        // If it’s a new user, save it for the future validations and return a corresponding result.
        return callback(null, {valid: true, message: 'User data saved for future validations'});
    }

    // If there is available data about the previous transaction, get details about the current and prior airports.
    // Skip the validation if the airports are the same.
    let [lastAirport, nextAirport] = await Promise.all([airports.get(user.lastCardUsePlace),
        airports.get(request.airportCode)]);
    if (lastAirport.code === nextAirport.code) {
        return callback(null, {valid: true, message: 'Transaction performed from the same location'});
    }

    let speed = getSpeed(lastAirport, user.lastCardUseTimestamp, nextAirport, request.transactionTimestamp);
    // Use the haversine formula to calculate a “user speed” between two transactions.
    // If it’s faster than an average plane’s speed, 800 km/hr == ~13000 m/min, the transaction is suspicious.
    let valid = speed <= 13000;
    let message = valid ? 'Transaction is OK' : 'Transaction is suspicious';

    // Store the data from the request for the future validations.
    user.lastCardUsePlace = request.airportCode;
    user.lastCardUseTimestamp = requestTimestampMillis;
    await users.set(userId, user);

    return callback(null, {valid: valid, message: message});
};

let getSpeed = (lastAirport, lastUseTimestamp, nextAirport, requestTimestamp) => {
    // Time
    let minutes = moment(requestTimestamp).diff(lastUseTimestamp, 'minutes');
    // Distance
    let meters = haversine(nextAirport, lastAirport, {unit: 'meter'});
    // Speed
    return meters / minutes;
};
