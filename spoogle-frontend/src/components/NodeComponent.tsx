import {Node} from "../types";
import {Accordion, Tag} from "@navikt/ds-react";
import styles from './NodeComponent.module.css'

export interface NodeComponentProps {
    node: Node
}

export const NodeComponent = ({node}: NodeComponentProps) => {
    const isLeaf = node.children.length === 0
    return <div className={'w-full'}>
        <Accordion>
            <Accordion.Item>
                <Accordion.Header className={isLeaf ? styles.RemoveExpandable : ''}>
                    <div className={'flex flex-row items-center gap-2'}>
                        <Tag variant={finnVariant(node.type)}>{node.type}</Tag>
                        {node.id}
                    </div>
                </Accordion.Header>
                {!isLeaf &&
                    <Accordion.Content>
                        {node.children.map((it, index) => <NodeComponent key={index} node={it}/>)}
                    </Accordion.Content>
                }
            </Accordion.Item>
        </Accordion>

    </div>
}


const finnVariant = (type: string): 'info' | 'alt2' | 'warning' | 'success' | 'alt1' | 'neutral' => {
    switch(type) {
        case 'FØDSELSNUMMER': return 'info'
        case 'ORGANISASJONSNUMMER': return 'alt2'
        case 'AKTØR_ID': return 'warning'
        case 'VEDTAKSPERIODE_ID': return 'success'
        case 'UTBETALING_ID': return 'alt1'
        default: return 'neutral'
    }
}