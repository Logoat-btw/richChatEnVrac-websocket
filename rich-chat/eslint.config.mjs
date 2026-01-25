import globals from 'globals';
import pluginJs from '@eslint/js';
import pluginReact from 'eslint-plugin-react';
import babelParser from '@babel/eslint-parser';
import stylistic from '@stylistic/eslint-plugin';
import hooksPlugin from 'eslint-plugin-react-hooks';
import jsxA11y from 'eslint-plugin-jsx-a11y';

/** @type {import('eslint').Linter.Config[]} */
export default [
  { ignores: ['webpack.js', 'babel.config.js', 'build/', 'buildInfos/', 'node_modules/'] },
  { files: ['**/*.{js,mjs,cjs,jsx}'] },
  { languageOptions: { parser: babelParser, globals: globals.browser } },
  pluginJs.configs.recommended,
  stylistic.configs['recommended'],
  pluginReact.configs.flat.recommended,
  pluginReact.configs.flat['jsx-runtime'],
  jsxA11y.flatConfigs.recommended,
  {
    settings: {
      react: {
        version: 'detect',
      },
    },
    plugins: {
      'react-hooks': hooksPlugin,
    },
    rules: {
      ...hooksPlugin.configs.recommended.rules,
      '@stylistic/semi': ['error', 'always'],
      'no-console': ['error', { allow: ['warn', 'error'] }],
    },
    languageOptions: {
      globals: {
        APP_ENV_APP_PUBLIC_PATH: 'readonly',
        APP_ENV_APP_TITLE: 'readonly',
        APP_ENV_API_PATH: 'readonly',
      },
    },
  },
  {
    files: ['webpack.js', 'babel.config.js'],
    languageOptions: { sourceType: 'module', globals: globals.node },
  },
];
