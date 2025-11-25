import { TreeResponse } from '../endepunkter';
import React, { createContext, PropsWithChildren, useContext, useState } from 'react';

export const enum Resultattype {
    SøkIkkeUtførtType,
    ResponsUtenPersonType,
    ResponsMedPersonType,
}

export const SøkIkkeUtført = { type: Resultattype.SøkIkkeUtførtType } as const;
export const ResponsUtenPerson = { type: Resultattype.ResponsUtenPersonType } as const;
export type ResponsMedPerson = { type: Resultattype.ResponsMedPersonType; person: TreeResponse };

type Søkeresultat = typeof SøkIkkeUtført | typeof ResponsUtenPerson | ResponsMedPerson;

type SøkContextType = {
    søkeresultat: Søkeresultat;
    setSøkeresultat: (response: TreeResponse | null) => void;
};

const SøkContext = createContext<SøkContextType | null>(null);

const useSøkContext = () => {
    const context = useContext(SøkContext);
    if (context == null) {
        throw 'Må ha en SøkContext.Provider i komponenttreet for å bruke SøkContext';
    }
    return context;
};

export const useSøkeresultat = () => useSøkContext().søkeresultat;
export const useSetSøkeresultat = () => useSøkContext().setSøkeresultat;

export function SøkContextProvider({ children }: PropsWithChildren) {
    const [søkeresultat, setSøkeresultat] = useState<Søkeresultat>(SøkIkkeUtført);

    return (
        <SøkContext.Provider
            value={{
                søkeresultat,
                setSøkeresultat: (person: TreeResponse | null) =>
                    person != null
                        ? setSøkeresultat({ type: Resultattype.ResponsMedPersonType, person })
                        : setSøkeresultat(ResponsUtenPerson),
            }}
        >
            {children}
        </SøkContext.Provider>
    );
}
