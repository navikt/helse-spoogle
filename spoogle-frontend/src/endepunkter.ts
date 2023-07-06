import { Bruker, Node } from './types';

export const søk = (id: string) =>
    fetch(`/api/sok/${id}`)
        .then(async (response) => {
            return response.json();
        })
        .then((data) => !isObjectEmpty(data) ? data as Node : undefined)
        .catch(() => undefined)

const isObjectEmpty = (objectName: any) => {
    return Object.keys(objectName).length === 0
}

export const fetchBruker = () =>
    fetch('/api/bruker', {})
        .then((response) => response.json())
        .then((data) => data as Bruker);
