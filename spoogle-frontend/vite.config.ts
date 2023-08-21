import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
// eslint-disable-next-line import/no-unresolved
const tokenfile: { token: string } | undefined = await import('../testtoken.json')
    .then((it) => it)
    .catch(() => {
        /* Do nothing */
    });

// https://vitejs.dev/config/
export default defineConfig({
    plugins: [react()],
    server: {
        headers: {},
        proxy: {
            '/echo': {
                target: 'ws://localhost:8080',
                ws: true,
                secure: false,
            },
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                configure: (proxy, options) => {
                    options.headers = {
                        ...options.headers,
                        Authorization: `Bearer ${tokenfile?.token}`,
                    };
                },
            },
        },
    },
});
