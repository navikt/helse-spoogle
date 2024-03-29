import React from "react";
import {Node} from "../types";
import {Accordion, CopyButton, Tag} from "@navikt/ds-react";
import styles from './NodeComponent.module.css'
import classNames from "classnames";

export interface NodeComponentProps {
    node: Node,
    path: string[]
}

export const NodeComponent = ({node, path}: NodeComponentProps) => {
    const currentSøkestreng = path[path.length - 1]
    const isCurrentClass = currentSøkestreng === node.id ? 'current' : ''
    const isLeaf = node.children.length === 0
    return <div className={'w-full'}>
        <Accordion>
            <Accordion.Item defaultOpen={path.includes(node.id)}>
                <Accordion.Header className={classNames(isLeaf ? styles.RemoveExpandable : '', isCurrentClass ? '!bg-blue-50' : '', styles.FullWidth)}>
                    <div className={'flex flex-row items-center gap-2'}>
                        <Tag variant={'neutral'} className={finnVariant(node.type)}>{node.type}</Tag>
                        <p className={'flex-1'}>{node.id}</p>
                        <CopyButton onClick={(e) => e.stopPropagation()} copyText={node.id}/>
                    </div>
                </Accordion.Header>
                {!isLeaf &&
                    <Accordion.Content>
                        {node.children.map((it, index) => <NodeComponent key={index} node={it} path={path}/>)}
                    </Accordion.Content>
                }
            </Accordion.Item>
        </Accordion>

    </div>
}


const finnVariant = (type: string): string => {
    switch(type) {
        case 'FØDSELSNUMMER': return '!bg-blue-200 !border-blue-500'
        case 'ORGANISASJONSNUMMER': return '!bg-green-200 !border-green-500'
        case 'AKTØR_ID': return '!bg-orange-200 !border-orange-500'
        case 'VEDTAKSPERIODE_ID': return '!bg-limegreen-200 !border-limegreen-500'
        case 'UTBETALING_ID': return '!bg-purple-200 !border-purple-500'
        case 'SØKNAD_ID': return '!bg-deepblue-200 !border-deepblue-500'
        case 'INNTEKTSMELDING_ID': return '!bg-lightblue-100 !border-lightblue-500'
        default: return 'neutral'
    }
}