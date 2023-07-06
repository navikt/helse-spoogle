import './App.css';
import '@navikt/ds-css';
import '@navikt/ds-css-internal';
import React from 'react';
import {Header} from './components/Header';
import {Søk} from "./components/Søk";
import {useRecoilState} from "recoil";
import {søkState} from "./state/state";
import {NodeComponent} from "./components/NodeComponent";

const App = () => {
    const [rootNode] = useRecoilState(søkState);

    return (
        <>
            <Header />
            <div className={'flex flex-1 self-center items-center flex-col w-[1000px] gap-10 my-20'}>
                <Søk />
                {rootNode &&
                    <NodeComponent node={rootNode}/>
                }
            </div>

        </>
    );
};

export default App;
