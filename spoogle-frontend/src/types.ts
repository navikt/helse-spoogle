export declare type Bruker = {
    epostadresse: string;
    navn: string;
    ident: string;
    oid: string;
};

export type Node = {
    id: string
    type: string
    children: Node[]
    ugyldig_fra: string | null
}