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
const port = window.location.port
const ws = new WebSocket(`ws://${hostname}:${port}/echo`)
const App = () => {
    ws.onopen = () => {
        console.log("Connection opened")
    }

    ws.onmessage = (event) => {
        console.log(event.data)
    }

    const [rootNode] = useRecoilState(søkState);

    return (
        <>
            <Header />
            <div className={'flex flex-1 self-center items-center flex-col w-[1000px] gap-10 my-20'}>
                <Søk />
                {rootNode ?
                    <NodeComponent node={rootNode}/>
                    : rootNode === null ? <Alert variant="info">
                        Det finnes ikke noe fødselsnummer knyttet til denne id-en i Spoogle
                    </Alert> : <></>
                }
            </div>

        </>
    );
};

export default App;
