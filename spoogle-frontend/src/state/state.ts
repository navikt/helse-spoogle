import {atom} from 'recoil';
import {Bruker} from '../types';

export const søkestrengState = atom<string>({
    key: 'søkestrengState',
    default: "",
});

export const brukerState = atom<Bruker | undefined>({
    key: 'brukerState',
    default: undefined,
});
