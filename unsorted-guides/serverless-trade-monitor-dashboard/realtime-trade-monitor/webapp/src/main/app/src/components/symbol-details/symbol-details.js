import React, {Component, Fragment} from 'react'

import Websocket from 'react-websocket';
import ReactTable from 'react-table';
import 'react-table/react-table.css';
import Pagination from '../Pagination';

class SymbolDetails extends Component {

    constructor(props) {
        super(props);
        this.state = {
            symbol: [],
            loading: true,
        };

        this.sendMessage = this.sendMessage.bind(this);
        this.handleData = this.handleData.bind(this);
        this.onOpen = this.onOpen.bind(this);
        this.renderPagination = this.renderPagination.bind(this);
    }

    sendMessage(message) {
        this.refWebSocket.sendMessage(message);
    }

    onOpen() {
        this.sendMessage('DRILL_SYMBOL ' + this.props.symbol);
    }

    handleData(data) {
        let result = JSON.parse(data);
        this.setState(prevState => ({
            symbol: prevState.symbol.concat(result.data),
            loading: false,
        }));
    }

    renderPagination(paginationProps) {
        return <Pagination {...paginationProps} extraData={<span>{this.props.symbol} has {this.state.symbol.length} records</span>}/>
    }

    render() {
        const {symbol} = this.state;
        const WS_HOST = process.env.REACT_APP_WS_HOST;

        const columns = [
            {
                Header: 'ID',
                accessor: 'id'
            },
            {
                Header: 'Time',
                accessor: 'timestamp',
                Cell: ({ value }) => (
                    <span className="Table-highlightValue">{new Date(value).toLocaleString("en-US")}</span>
                )
            },
            {
                Header: 'Symbol',
                accessor: 'symbol',
                Cell: ({ value }) => (
                    <span className="Table-highlightValue">{value}</span>
                )

            },
            {
                Header: 'Quantity',
                accessor: 'quantity',
                Cell: ({ value }) => (
                    <span className="Table-highlightValue">{value.toLocaleString()}</span>
                )

            },
            {
                Header: 'Price',
                accessor: 'price',
                Cell: ({ value }) => (
                    <span className="Table-highlightValue Table-price">{(value / 100).toLocaleString("en-US", {
                        style: "currency",
                        currency: "USD"
                    })}</span>
                )
            }
        ];

        return <Fragment>
        <Websocket url={WS_HOST} onOpen={this.onOpen}
        onMessage={this.handleData}
        reconnect={true} debug={true}
        ref={Websocket => {
            this.refWebSocket = Websocket;
        }}/>
        <ReactTable
        data={symbol}
        columns={columns}
        defaultSorted={[
            {
                id: "timestamp",
                desc: true
            }
        ]}
        defaultPageSize={10}
        noDataText={this.state.loading ? 'Loading data...' : 'No rows found'}
        expanded={this.state.expanded}
        onExpandedChange={expanded => this.setState({expanded})}
        className="Table-subtable"
        PaginationComponent={this.renderPagination}
        />
        </Fragment>
    }
}

export default SymbolDetails;
