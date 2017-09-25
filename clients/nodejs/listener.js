let listener = {
    added: function (key, oldVal, newVal) {
        console.log('added key: ' + key + ', old value: ' + oldVal + ', new value: ' + newVal);
    },
    removed: function (key, oldVal, newVal) {
        console.log('removed key: ' + key + ', old value: ' + oldVal + ', new value: ' + newVal);
    }
};

module.exports = listener;