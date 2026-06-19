import { defineConfig, type Plugin } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// The SPA router mounts under /app (to match how Spring serves it). In dev the
// Vite server has no such redirect, so hitting the root renders blank. This
// mirrors the backend's SpaForwardingRouter, which 302s / -> /app/.
function devRedirectRootToApp(): Plugin {
  return {
    name: 'dev-redirect-root-to-app',
    apply: 'serve',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (req.url === '/' || req.url === '') {
          res.statusCode = 302
          res.setHeader('Location', '/app/')
          res.end()
          return
        }
        next()
      })
    },
  }
}

export default defineConfig({
  base: '/',
  plugins: [devRedirectRootToApp(), react(), tailwindcss()],
  server: {
    // Matches `make run` / `make run-inmemory`, which start the backend on 8080.
    proxy: { '/api': 'http://localhost:8080' },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/test/setup.ts',
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      exclude: ['src/generated/**', 'src/test/**', '**/*.d.ts', '**/*.config.*'],
    },
  },
})
