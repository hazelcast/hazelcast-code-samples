import React, { Component } from "react";
import Websocket from "react-websocket";
import ReactTable from "react-table";
import "react-table/react-table.css";

import Page from "../Page";
import Pagination from "../Pagination";
import SymbolDetails from "../symbol-details";
import "../../Table.css";

class Home extends Component {
    constructor(props) {
        super(props);
        this.state = {
            symbols: [],
            expanded: {}
        };

        this.sendMessage = this.sendMessage.bind(this);
        this.handleData = this.handleData.bind(this);
        this.onOpen = this.onOpen.bind(this);
    }

    sendMessage(message) {
        this.refWebSocket.sendMessage(message);
    }

    onOpen() {
        setInterval(() => this.sendMessage("LOAD_SYMBOLS"), 1000);
    }

    handleData(data) {
        let result = JSON.parse(data);
        for (let i = 0; i < result.symbols.length; i++) {
            let oldPrice;
            if (typeof this.state.symbols[i] === "undefined") {
                oldPrice = 0;
            } else {
                oldPrice = this.state.symbols[i].price;
            }
            result.symbols[i]["oldPrice"] = oldPrice;
        }
        this.setState({ symbols: result.symbols });
    }

    render() {
        const { symbols } = this.state;

        const columns = [
            {
                Header: "Symbol",
                accessor: "symbol",
                width: 300,
                Cell: ({ value }) => (
                    <span className="Table-highlightValue">{value}</span>
                    )
                },
                {
                    Header: "Name",
                    accessor: "name"
                },
                {
                    Header: "Price",
                    accessor: "price",
                    width: 300,
                    Cell: ({ value, columnProps: { className } }) => (
                        <span className={`Table-highlightValue Table-price ${className}`}>
                        {(value / 100).toLocaleString("en-US", {
                            style: "currency",
                            currency: "USD"
                        })}
                        </span>
                        ),
                        getProps: (state, ri, column) => {
                            if (!ri) {
                                return {};
                            }
                            // console.log(ri.row);
                            const changeUp = ri.row.price > ri.row._original.oldPrice;
                            const changeDown = ri.row.price < ri.row._original.oldPrice;
                            const className = changeUp
                            ? "Table-changeUp"
                            : changeDown
                            ? "Table-changeDown"
                            : "";

                            return {
                                className
                            };
                        }
                    },
                    {
                        Header: "Volume",
                        accessor: "volume"
                    }
                ];
        const WS_HOST = process.env.REACT_APP_WS_HOST;
        return (
                    <Page header="Trade Monitor Dashboard">
                        <ReactTable
                        className="Table-main"
                        data={symbols}
                        columns={columns}
                        defaultPageSize={25}
                        expanded={this.state.expanded}
                        onExpandedChange={expanded => this.setState({ expanded })}
                        PaginationComponent={Pagination}
                        getTrProps={(state, rowInfo) => ({
                            className:
                            rowInfo && state.expanded[rowInfo.viewIndex] ? "Table-expanded" : ""
                        })}
                        getTdProps={(state, rowInfo) => {
                            return {
                                onClick: (e, handleOriginal) => {
                                    const { viewIndex } = rowInfo;
                                    this.setState(prevState => ({
                                        expanded: {
                                            ...prevState.expanded,
                                            [viewIndex]: !prevState.expanded[viewIndex]
                                        }
                                    }));
                                }
                            };
                        }}
                        SubComponent={original => (
                            <SymbolDetails symbol={original.row.symbol} />
                            )}
                            />

                            <Websocket
                            url={WS_HOST}
                            onOpen={this.onOpen}
                            onMessage={this.handleData}
                            reconnect={true}
                            debug={true}
                            ref={Websocket => {
                                this.refWebSocket = Websocket;
                            }}
                            />
                        </Page>
                        );
                    }
                }

                export default Home;
