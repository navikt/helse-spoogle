import { readFile } from 'node:fs/promises';
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// testtoken.json er valgfri og finnes kun lokalt, så den leses ved kjøring i stedet for å importeres
const tokenfile: { token: string } | undefined = await readFile(new URL('../testtoken.json', import.meta.url), 'utf-8')
    .then((it) => JSON.parse(it))
    .catch(() => undefined);

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
