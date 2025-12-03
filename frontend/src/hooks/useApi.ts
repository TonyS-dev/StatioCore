import { useState, useCallback } from 'react';

export const useApi = <T,>(asyncFunction: () => Promise<T>) => {
  const [data, setData] = useState<T | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const execute = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const result = await asyncFunction();
      setData(result);
      return result;
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'An error occurred';
      setError(message);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, [asyncFunction]);

  return { data, isLoading, error, execute };
};
