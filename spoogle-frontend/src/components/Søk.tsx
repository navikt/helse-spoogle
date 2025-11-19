import { Search } from '@navikt/ds-react';
import React, { useContext, useState } from 'react';
import { søkestrengState } from '../state/state';
import { useRecoilState } from 'recoil';
import { søk } from '../endepunkter';
import { SøkContext } from '../state/providers';

export const Søk = () => {
    const { setSøkeresultat } = useContext(SøkContext);
    const [loading, setLoading] = useState(false)
    const [søkestreng, setSøkestreng] = useRecoilState(søkestrengState)
    return (
        <form onSubmit={async (e) => {
            e.preventDefault()
            setLoading(true)
            return søk(søkestreng)
                .then((response) => setSøkeresultat(response))
                .finally(() => setLoading(false));

        }}>
            <div className={'flex'}>
                <Search
                    htmlSize={50}
                    label="header søk"
                    variant={'secondary'}
                    autoFocus={true}
                    placeholder="Søk etter en id"
                    onChange={(value) => {setSøkestreng(value)}}>
                    <Search.Button type={"submit"} loading={loading}/>
                </Search>
            </div>

        </form>
    );
};
