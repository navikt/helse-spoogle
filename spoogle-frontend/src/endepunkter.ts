import { Bruker, Node } from './types';

export interface TreeResponse {
    path: string[];
    tree: Node | undefined;
}
export const søk = (id: string): Promise<TreeResponse | undefined> =>
    fetch(`/api/sok/${id}`)
        .then(async (response) => {
            return response.json();
        })
        .then((data) => !isObjectEmpty(data) ? data as TreeResponse : undefined)
        .catch(() => undefined)

const isObjectEmpty = (objectName: any) => {
    return Object.keys(objectName).length === 0
}

export const fetchBruker = () =>
    fetch('/api/bruker', {})
        .then((response) => response.json())
        .then((data) => data as Bruker);
