function stats() {
    $.ajax({
        url: 'maps/stats',
        success: function(data) {
            console.log(data);
            $("#map-stat").text(data);
        },
        complete: function() {
            setTimeout(stats, 500);
        }
    });
}

function clearAll() {
    $.ajax({
        url: 'maps/clear',
        success: function(data) {
            console.log(data);
        },
        error: function (error) {
            console.log(error)
        }
    });
}

function autoPilot() {
    $.ajax({
        url: 'maps/auto',
        success: function(data) {
            console.log(data);
        },
        error: function (error) {
            console.log(error)
        }
    });
}

function random(){
    var count = parseInt($('#random-count').val(), 10);

    if(count <= 0 || count > 10000){
        console.log('invalid random count');
        return;
    }

    $.ajax({
        url: 'maps/random/' + count,
        success: function(data) {
            console.log(data);
        },
        error: function (error) {
            console.log(error)
        }
    });
}