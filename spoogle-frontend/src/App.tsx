import './App.css';
import '@navikt/ds-css';
import React from 'react';
import { Header } from './components/Header';
import { Søk } from './components/Søk';
import { NodeComponent } from './components/NodeComponent';
import { Alert } from '@navikt/ds-react';
import { Resultattype, SøkContextProvider, useSøkeresultat } from './state/providers';

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
    const søkeresultat = useSøkeresultat();
    switch (søkeresultat.type) {
        case Resultattype.SøkIkkeUtførtType:
            return <></>;
        case Resultattype.ResponsUtenPersonType:
            return <Alert variant="info">Det finnes ikke noe fødselsnummer knyttet til denne id-en i Spoogle</Alert>;
        case Resultattype.ResponsMedPersonType:
            return <NodeComponent node={søkeresultat.person.tree} path={søkeresultat.person.path} />;
    }
};

export default App;
