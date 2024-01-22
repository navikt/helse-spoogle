import './App.css';
import '@navikt/ds-css';
import '@navikt/ds-css-internal';
import React from 'react';
import {Header} from './components/Header';
import {Søk} from "./components/Søk";
import {useRecoilState} from "recoil";
import {søkState} from "./state/state";
import {NodeComponent} from "./components/NodeComponent";
import {Alert} from "@navikt/ds-react";

const hostname = window.location.hostname
const isDevelopment = window.location.host.includes("0.0.0.0") || window.location.host.includes("localhost")
const port = window.location.port
const url = isDevelopment ? `ws://${hostname}:${port}` : `wss://${hostname}`
const ws = new WebSocket(`${url}/echo`)

const App = () => {
    ws.onopen = () => {
        console.log("Connection opened")
    }

    ws.onmessage = (event) => {
        console.log(event.data)
    }

    ws.onerror = (err) => {
        console.log(err)
    }

    const sseSource = new EventSource("/sse");

    sseSource.addEventListener('open', (event) => {
        console.log("Sse connection opened")
    })

    sseSource.addEventListener('message', (event) => {
        console.log(event)
    })

    const [response] = useRecoilState(søkState);

    return (
        <>
            <Header />
            <div className={'flex flex-1 self-center items-center flex-col w-[1000px] gap-10 my-20'}>
                <Søk />
                {response ?
                    <NodeComponent node={response.tree} path={response.path}/>
                    : response === null ? <Alert variant="info">
                        Det finnes ikke noe fødselsnummer knyttet til denne id-en i Spoogle
                    </Alert> : <></>
                }
            </div>

        </>
    );
};

export default App;
