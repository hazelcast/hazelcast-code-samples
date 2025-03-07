import React    from 'react'

import { Link } from 'react-router-dom'

import Page     from '../Page'

const NotFound = () => (
    <Page header="Not Found">
        <div className="error">
            <h2 className="error-message">Page not found</h2>
            <span><Link className="App-link" to="/">Home</Link></span>
        </div>
    </Page>
)

export default NotFound
