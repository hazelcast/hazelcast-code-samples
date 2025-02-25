# Run this app with `python app.py` and
# visit http://127.0.0.1:8050/ in your web browser.

from dash import Dash, html, dcc, Input, Output
import hazelcast

app = Dash(__name__)


app.layout = html.Div(style={"display" : "flex", "justify-content": "center", "align-items" : "center", "flex-direction": "column","color":"gray"}, children=[
    html.H1(children='Temperature Monitor'),

    html.Div([
        "Serial Number ",
        dcc.Input(id='sn_input', type='text', placeholder='serial number', debounce=True)
    ]),

    html.Div('warning temp: 999.9 / critical temp: 999.99', style={"color":"gray", "padding": "20px"}, id="limits"),

    html.Span(id="temperature", children="999.9 degrees"
    , style={  "color" : "red", "font-size": "30px", "max-width" : "300px", "padding" : "20px"}),

    dcc.Interval(id="interval", interval=2000)

])

@app.callback(
    Output(component_id="temperature", component_property="children"),
    Output(component_id="temperature", component_property="style"),
    Output(component_id="limits", component_property="children"),
    Input(component_id="interval", component_property="n_intervals"),
    Input(component_id="sn_input", component_property="value")
)
def update_temp(n_ticks, sn):
    new_color = "gray"
    new_temp = "0.0"
    warn_limit = "0.0"
    critical_limit = "0.0"

    if sn is not None:
        status_msg = status_map.get(sn)
        if status_msg is not None:
            words = status_msg.split(sep=",")
            new_temp = words[0]
            if (len(words) >= 4):
                warn_limit = words[1]
                critical_limit = words[2]
                new_color = words[3]

    if new_color == 'orange':
        new_temp = new_temp + ' !'

    if new_color == 'red':
        new_temp = new_temp + " !!"

    return f'{new_temp}', {  "color" : new_color, "font-size": "30px", "max-width" : "300px", "padding" : "20px"}, f'warning temp: {warn_limit}  / critical temp: {critical_limit}'


client = hazelcast.HazelcastClient( cluster_name="dev", cluster_members=["hz"])
status_map = client.get_map("machine_status").blocking()


if __name__ == '__main__':
    app.run_server(debug=True, host='0.0.0.0', port='8050', proxy='http://0.0.0.0:8050::http://localhost:8050')
