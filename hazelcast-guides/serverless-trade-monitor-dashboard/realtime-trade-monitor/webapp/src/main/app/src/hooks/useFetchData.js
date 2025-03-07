import {useEffect, useReducer} from 'react'

const initialState = {
    loading: false,
    error  : false,
    data   : []
}

const dataReducer = (state, action) => {
    switch (action.type) {
        case 'LOADING':
            return {
                ...state,
                 loading: true
            }
        case 'ERROR':
            return {
                ...state,
                loading: false,
                error  : action.error
            }
        case 'SET_DATA':
            return {
                ...state,
                loading: false,
                data   : action.data
            }
        default:
            return {
                ...state
            }
    }
}

const useFetchData = ({url}) => {
    const [state, dispatch] = useReducer(dataReducer, initialState)

    useEffect(() => {
        const getInitialData = async () => {
            try {
                dispatch({
                    type: 'LOADING'
                })
                const response = await fetch(url)
                switch (response.status) {
                    case 200:
                        const data = await response.json()
                        dispatch({
                            type: 'SET_DATA',
                            data
                        })
                        break;
                    case 404:
                        throw new Error("Not found")
                    default:
                        throw new Error(`Unexpected response ${response.status}`)
                }
            } catch (error) {
                dispatch({
                    type: 'ERROR',
                    error
                })
            }
        }
        getInitialData()
    }, [url])

    return [state, dispatch]
}

export default useFetchData
