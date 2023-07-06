import React from 'react';
import styles from './Separator.module.css';
import classNames from 'classnames';

export const Separator: React.FC<React.HTMLAttributes<HTMLDivElement>> = ({ className, ...divProps }) => {
    return <div className={classNames(styles.Separator, className)} {...divProps} />;
};
