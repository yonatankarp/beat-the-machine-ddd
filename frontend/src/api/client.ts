import { Configuration } from '../generated'

// Same origin: the SPA is served by Spring, so the base path is empty and the
// browser resolves /api/... against the current host.
export const apiConfig = new Configuration({ basePath: '' })
