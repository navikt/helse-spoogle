import {atom} from 'recoil';
import {Bruker} from '../types';
import {TreeResponse} from "../endepunkter";

export const søkState = atom<TreeResponse | null | undefined>({
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
