module.exports = {
  parserOptions: {
    ecmaFeatures: {
      jsx: true,
    },
    ecmaVersion: 'latest',
    sourceType: 'module',
  },
  parser: '@typescript-eslint/parser',
  plugins: ['react', 'react-native', '@typescript-eslint'],
  rules: {
    'react-native/no-unused-styles': 2,
    'react-native/split-platform-components': 2,
    'react-native/no-single-element-style-arrays': 2,
    'react-native/no-inline-styles': 0,
    'react/prop-types': 0,
    'react-native/no-color-literals': 0,
    'react/display-name': 0,
    'react-native/sort-styles': 0,
    'react-native/no-raw-text': 0,
    'no-unused-vars': 1,
    'react/no-unstable-nested-components': 0,
    'no-unused-vars': 0,
  },
};
