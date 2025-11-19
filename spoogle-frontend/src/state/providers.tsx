import { TreeResponse } from '../endepunkter';
import React, { createContext, Dispatch, ReactNode, SetStateAction, useState } from 'react';

type Søkeresultat = TreeResponse | null | undefined;

type SøkContextValue = {
    søkeresultat: Søkeresultat;
    setSøkeresultat: Dispatch<SetStateAction<Søkeresultat>>;
};

type SøkContextProviderProps = { children: ReactNode };

export const SøkContext = createContext<SøkContextValue>({ søkeresultat: null, setSøkeresultat: () => {} });

export function SøkContextProvider({ children }: SøkContextProviderProps) {
    const [søkeresultat, setSøkeresultat] = useState<Søkeresultat>(undefined);
    return <SøkContext.Provider value={{ søkeresultat, setSøkeresultat: setSøkeresultat }}>{children}</SøkContext.Provider>;
}
