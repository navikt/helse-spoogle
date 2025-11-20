import React, { useEffect, useState } from 'react';
import { InternalHeader as DSHeader } from '@navikt/ds-react';
import { fetchBruker } from '../endepunkter';
import { Bruker } from '../types';

export const Header = () => {
    const [bruker, setBruker] = useState<Bruker | null>(null);

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
