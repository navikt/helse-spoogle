import { defineConfig, globalIgnores } from "eslint/config";
import { fixupConfigRules, fixupPluginRules } from "@eslint/compat";
import react from "eslint-plugin-react";
import typescriptEslint from "@typescript-eslint/eslint-plugin";
import globals from "globals";
import path from "node:path";
import { fileURLToPath } from "node:url";
import js from "@eslint/js";
import { FlatCompat } from "@eslint/eslintrc";
import typescriptParser from "@typescript-eslint/parser";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
    baseDirectory: __dirname,
    recommendedConfig: js.configs.recommended,
    allConfig: js.configs.all
});

export default defineConfig([
    globalIgnores(["**/node_modules/", "**/.prettierrc", "**/.eslintrc.json", "**/dist/"]),
    {
        extends: fixupConfigRules(compat.extends(
            "eslint:recommended",
            "plugin:react/recommended",
            "plugin:import/recommended",
            "plugin:jsx-a11y/recommended",
            "plugin:@typescript-eslint/recommended",
            "eslint-config-prettier",
        )),

        plugins: {
            react: fixupPluginRules(react),
            "@typescript-eslint": fixupPluginRules(typescriptEslint),
        },

        languageOptions: {
            globals: {
                ...globals.browser,
            },

            parser: typescriptParser,
            ecmaVersion: 12,
            sourceType: "module",

            parserOptions: {
                ecmaVersion: "latest",
                sourceType: "module",
            },
        },

        settings: {
            react: {
                version: "detect",
            },

            "import/resolver": {
                typescript: {
                    alwaysTryTypes: true,
                    project: "./tsconfig.json",
                },
            },
        },

        rules: {
            "react/prop-types": "off",
            "jsx-a11y/no-autofocus": "off",
        },
    },
]);
