import React, {useEffect} from 'react';
import {InternalHeader as DSHeader} from '@navikt/ds-react';
import {useRecoilState} from 'recoil';
import {brukerState} from '../state/state';
import {fetchBruker} from '../endepunkter';

export const Header = () => {
    const [bruker, setBruker] = useRecoilState(brukerState);

    useEffect(() => {
        fetchBruker().then((bruker) => setBruker(bruker));
    }, []);

    return (
        <DSHeader className={'flex w-full'}>
            <DSHeader.Title as={'h1'}>Spoogle</DSHeader.Title>
            <span className={'mr-auto'}/>
            <DSHeader.User name={bruker?.navn ?? 'Ikke innlogget'} description={bruker?.ident} />
        </DSHeader>
    );
};
