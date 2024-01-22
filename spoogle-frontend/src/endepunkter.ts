import { Bruker, Node } from './types';

export interface TreeResponse {
    path: string[];
    tree: Node;
}
export const søk = (id: string): Promise<TreeResponse | null> =>
    fetch(`/api/sok/${id}`)
        .then(async (response) => {
            if (!response.ok) return null
            return await response.json();
        })
        .then((data) => data as TreeResponse)
        .catch(() => null)
export const fetchBruker = () =>
    fetch('/api/bruker', {})
        .then((response) => response.json())
        .then((data) => data as Bruker);
