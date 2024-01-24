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

const App = () => {
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
