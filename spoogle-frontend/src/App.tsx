import './App.css';
import '@navikt/ds-css';
import React, { useContext } from 'react';
import { Header } from './components/Header';
import { Søk } from './components/Søk';
import { NodeComponent } from './components/NodeComponent';
import { Alert } from '@navikt/ds-react';
import { SøkContext, SøkContextProvider } from './state/providers';

const App = () => {
    return (
        <>
            <Header />
            <div className={'flex flex-1 self-center items-center flex-col w-[1000px] gap-10 my-20'}>
                <SøkContextProvider>
                    <Søk />
                    <Main />
                </SøkContextProvider>
            </div>
        </>
    );
};

const Main = () => {
    const response = useContext(SøkContext).søkeresultat;
    return response === undefined ? (
        <></>
    ) : response === null ? (
        <Alert variant="info">Det finnes ikke noe fødselsnummer knyttet til denne id-en i Spoogle</Alert>
    ) : (
        <NodeComponent node={response.tree} path={response.path} />
    );
}

export default App;
