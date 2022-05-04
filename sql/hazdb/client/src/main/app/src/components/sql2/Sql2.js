import React, {Component} from 'react';
import update from 'immutability-helper';

var rest = require('rest');
var mime = require('rest/interceptor/mime');

class Sql2 extends Component {
    constructor(props) {
        super(props);
        this.state = {
			query: 'UPDATE bundesliga SET points = 26 WHERE __key = \'Bayern Munich\'',
            message0: '',
            message0_style: {
                'color': 'green',
                'font-weight': 'bold'
            },
            message2: '',
            message2_style: {
                'color': 'yellow',
                'font-weight': 'bold'
            }
        };
        this.handleChange = this.handleChange.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
    }

    componentDidMount(){
		var text0 = '.';
        var text0_style = {visibility: 'hidden'};
        this.setState({ 
            message0: update(this.state.message0, {$set: text0}) 
        })
        this.setState({ 
            message0_style: update(this.state.message0_style, {$set: text0_style}) 
        })
        var text2 = '.';
        var text2_style = {visibility: 'hidden'};
        this.setState({ 
            message2: update(this.state.message2, {$set: text2}) 
        })
        this.setState({ 
            message2_style: update(this.state.message2_style, {$set: text2_style}) 
        })
    }

    handleChange(e) {
        if (e.target.name === 'query') {
            this.setState({query: e.target.value});
        }
    }
    
    handleSubmit(e) {
        console.log("Sql2.js", "handleSubmit()", this.state.query);
        e.preventDefault();
        setTimeout(() => {
            var client = rest.wrap(mime);
            var self = this;
                
            var restURL = '/rest/sqlUpdate?sql=' + this.state.query;

            client({path:restURL}).then(function(response) {
                console.log("Sql2.js", "handleSubmit()", "response.entity", response.entity);
                var payload = response.entity;
                        
                var text0 = '?';
                var text0_style = {};
                var text2 = '?';
                var text2_style = {};

                var error_message = payload.error;

                var info_style = {
                    'color': 'green',
                    'font-weight': 'bold'
                }
                var error_style = {
                	'color': 'red',
                    'font-weight': 'bold'
                }

                text0 = self.state.query;
                text0_style = info_style;
				text2 = 'OK';
                text2_style = info_style;
                        
                if (error_message.length > 0) {
                    text2 = error_message;
                    text2_style = error_style;
                }

                self.setState({
                    message0: update(self.state.message0, {$set: text0}) 
                });
                self.setState({
                    message0_style: update(self.state.message0_style, {$set: text0_style}) 
                });
                self.setState({
                    message2: update(self.state.message2, {$set: text2}) 
                });
                self.setState({
                    message2_style: update(self.state.message2_style, {$set: text2_style}) 
                });
            });
        }, 250)
        setTimeout(() => {
            var self = this;

            var text0 = '.';
            var text0_style = {visibility: 'hidden'};
            var text2 = '.';
            var text2_style = {visibility: 'hidden'};
                                
            self.setState({
                message0: update(self.state.message0, {$set: text0}) 
            });
            self.setState({
                message0_style: update(self.state.message0_style, {$set: text0_style}) 
            });
            self.setState({
                message2: update(self.state.message2, {$set: text2}) 
            });
            self.setState({
                message2_style: update(self.state.message2_style, {$set: text2_style}) 
            });
        }, 15000)
        window.location = "#";
    }
    
    render() {
        return (
             <div>
             	 <h3>SQL Update</h3>
                 <div>
                     <form>
                         <label for="query">Update:</label>
                         <select id="query" name="query"
                                onChange={this.handleChange}>
                             <option value="UPDATE bundesliga SET points = 26 WHERE __key = 'Bayern Munich'" selected
                                 >UPDATE bundesliga SET points = 26 WHERE __key = 'Bayern Munich'</option>
                             <option value="DELETE FROM bundesliga WHERE __key = 'Bayern Munich'"
                                 >DELETE FROM bundesliga WHERE __key = 'Bayern Munich'</option>
                             <option value="INSERT INTO bundesliga (__key, points) VALUES ('MyTeam', 50)"
                                 >INSERT INTO bundesliga (__key, points) VALUES ('MyTeam', 50)</option>
                         </select>
                        <button onClick={this.handleSubmit}>Submit</button>
                    </form>
                 </div>
                 <div>
                    <p style={this.state.message0_style}>{this.state.message0}</p>
                    <p style={this.state.message2_style}>{this.state.message2}</p>
                 </div>
             </div>
        );
    }
}

export default Sql2;
