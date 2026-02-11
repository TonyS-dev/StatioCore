# Statio Core — Frontend

This folder contains the Vite + React + TypeScript frontend for the Statio Core project.

## Overview

- Framework: React 19 + Vite
- Language: TypeScript
- UI: TailwindCSS + shadcn/ui (Radix primitives)
- State: Zustand (auth), @tanstack/react-query (server state)
- HTTP: Axios (central instance in `src/services/api.ts`)

## Quick start (development)

1. Install dependencies

   ```bash
   cd frontend
   npm install
   ```

2. Development server

   ```bash
   npm run dev
   ```

   - Vite dev URL (default): http://localhost:5173

## Build for production

```bash
npm run build
npm run preview   # preview the production build
```

## Important files & folders

- `package.json` — npm scripts and dependencies
- `src/main.tsx` — app entry
- `src/App.tsx` — routes configuration and React Query provider
- `src/services/api.ts` — Axios instance (interceptors + base URL)
- `src/store/authStore.ts` — Zustand store for auth and user data
- `src/types/index.ts` — Shared DTO TypeScript interfaces used throughout the app
- `src/pages/` — top-level pages (auth, user, admin)
- `src/components/` — shared UI and layout components

## Environment

- The frontend uses Vite environment variables (prefixed with `VITE_`).
- Common variable used in code: `VITE_API_BASE_URL` (defaults to `http://localhost:8080/api`).

Example `.env` (frontend):

```
VITE_API_BASE_URL=http://localhost:8080/api
```

## Notes for developers

- The central Axios instance adds an `Authorization` header when a JWT is present in `localStorage`.
- Keep imports purely type-only when importing from `src/types` where possible (use `import type { ... } from '../types'`) to avoid runtime import expectations.
- If you see errors like `module '/src/types/index.ts' does not provide an export named 'ApiError'` it means a runtime import expected a value export; the types file provides interfaces only. Either use `import type` or export a harmless runtime placeholder.

## Testing & linting

- ESLint is configured as a dev dependency.

```bash
npm run lint
```

## Working with the backend

- The frontend expects the backend API at `VITE_API_BASE_URL`. For local full-stack development, use the root `docker-compose.yml` which starts the backend at `http://localhost:8080`.

## More

- See the root `README.md` for full-stack development instructions and the `docs/API_Documentation.md` for API reference used by the frontend.

