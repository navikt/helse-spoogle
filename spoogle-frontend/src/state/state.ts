import {atom} from 'recoil';
import {Bruker, Node} from '../types';

export const søkState = atom<Node | null | undefined>({
    key: 'søkState',
    default: undefined,
});
export const søkestrengState = atom<string>({
    key: 'søkestrengState',
    default: "",
});

export const brukerState = atom<Bruker | undefined>({
    key: 'brukerState',
    default: undefined,
});
