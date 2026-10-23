import { defineConfig } from 'vite';
import { resolve } from 'path';
import fs from 'fs';

export default defineConfig({
  root: '.',
  build: {
    outDir: 'dist',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        main: resolve(__dirname, 'index.html'),
      }
    }
  },
  plugins: [
    {
      name: 'copy-static-assets',
      closeBundle() {
        const copyList = [
          'robots.txt',
          'site.webmanifest',
          'favicon.ico',
          'favicon-16x16.png',
          'favicon-32x32.png',
          'apple-touch-icon.png',
          'android-chrome-192x192.png',
          'android-chrome-512x512.png',
          'js/config.js',
          'js/sqc.js',
          'js/jquery.buttonLoader.min.js'
        ];
        for (const file of copyList) {
          const src = resolve(__dirname, file);
          const dest = resolve(__dirname, 'dist', file);
          if (fs.existsSync(src)) {
            fs.mkdirSync(resolve(dest, '..'), { recursive: true });
            fs.copyFileSync(src, dest);
          }
        }
      }
    }
  ]
});