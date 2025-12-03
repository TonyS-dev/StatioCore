export function isRecord(v: unknown): v is Record<string, unknown> {
  return typeof v === 'object' && v !== null;
}

export function getErrorMessage(err: unknown, fallback = 'An unexpected error occurred'): string {
  if (!err) return fallback;
  if (typeof err === 'string') return err;
  if (isRecord(err)) {
    const e = err as Record<string, unknown>;
    // Common fields
    if (typeof e.message === 'string') return e.message;
    if (typeof e.error === 'string') return e.error;
    if (typeof e.detail === 'string') return e.detail;
    // Axios-style: error.response?.data?.message
    const resp = e.response;
    if (isRecord(resp)) {
      const data = resp.data;
      if (typeof data === 'string') return data;
      if (isRecord(data)) {
        if (typeof data.message === 'string') return data.message;
        if (typeof data.error === 'string') return data.error;
        if (typeof data.detail === 'string') return data.detail;
      }
    }
    // Fallback to JSON string
    try {
      return JSON.stringify(e);
    } catch (e) {
      // swallow
    }
  }
  return String(err) || fallback;
}

export function getErrorMeta(err: unknown): { timestamp?: string; path?: string } {
  if (!isRecord(err)) return {};
  const resp = (err as Record<string, unknown>).response;
  if (!isRecord(resp)) return {};
  const data = resp.data;
  if (!isRecord(data)) return {};
  const timestamp = typeof data.timestamp === 'string' ? data.timestamp : undefined;
  const path = typeof data.path === 'string' ? data.path : undefined;
  return { timestamp, path };
}
