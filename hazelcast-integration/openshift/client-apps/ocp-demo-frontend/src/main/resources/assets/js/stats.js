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
    $("#processed-entry").text('0');
    $.ajax({
        url: 'maps/clear',
        success: function(data) {
            console.log(data);
        },
        error: function (error) {
            console.log(error);
            $.notify(error.responseText, "warn");
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
            console.log(error);
            $.notify(error.responseText, "warn");
        }
    });
}

function random(){
    var count = parseInt($('#random-count').val(), 10);

    if(count !== count || count <= 0 || count > 10000){
        $.notify('invalid random count', "warn");
        return;
    }

    $.ajax({
        url: 'maps/random/' + count,
        success: function(data) {
            console.log(data);
        },
        error: function (error) {
            console.log(error);
            $.notify(error.responseText, "warn");
        }
    });
}

function randomPositions() {
    var count = parseInt($('#random-positions').val(), 10);

    if(count !== count || count <= 0 || count > 10000){
        $.notify('invalid random position count', "warn");
        return;
    }

    $.ajax({
        url: 'maps/random/position/' + count,
        success: function(data) {
            console.log(data);
        },
        error: function (error) {
            console.log(error);
            $.notify(error.responseText, "warn");
        }
    });

}

function executeOnEntryProcessor() {
    var data = {};
    data["latitude"] = $("#latitude").val();
    data["longitude"] = $("#longitude").val();

    $.ajax({
        url: 'maps/entry/processor/distance',
        type: 'POST',
        contentType : "application/json",
        data : JSON.stringify(data),
        dataType : 'json',
        success: function(data) {
            console.log(data);
            $("#processed-entry").text(data);
        },
        error: function (error) {
            console.log(error);
            $("#processed-entry").text('0');
            $.notify(error.responseText, "warn");
        }
    });



}