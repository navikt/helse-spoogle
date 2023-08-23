import {Search} from '@navikt/ds-react';
import React, {useState} from 'react';
import {søkestrengState, søkState} from '../state/state';
import {useRecoilState, useSetRecoilState} from 'recoil';
import {søk} from "../endepunkter";

export const Søk = () => {
    const setSøk = useSetRecoilState(søkState);
    const [loading, setLoading] = useState(false)
    const [søkestreng, setSøkestreng] = useRecoilState(søkestrengState)
    return (
        <form onSubmit={async (e) => {
            e.preventDefault()
            setLoading(true)
            return søk(søkestreng).then((response) => {
                response === undefined ? setSøk(null) : setSøk(response);
            }).finally(() => setLoading(false));

        }}>
            <div className={'flex'}>
                <Search
                    htmlSize={50}
                    label="header søk"
                    variant={'secondary'}
                    placeholder="Søk etter en id"
                    onChange={(value) => {setSøkestreng(value)}}>
                    <Search.Button type={"submit"} loading={loading}/>
                </Search>
            </div>

        </form>
    );
};
