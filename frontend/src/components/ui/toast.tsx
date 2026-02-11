/* eslint-disable react-refresh/only-export-components */
import React, { createContext, useContext, useState, useCallback } from 'react';

export type ToastVariant = 'success' | 'error' | 'info' | 'warning';

export interface Toast { id: string; message: string; variant?: ToastVariant; duration?: number; }

interface ToastContextValue {
  toasts: Toast[];
  push: (toast: Omit<Toast, 'id'>) => void;
  remove: (id: string) => void;
}

const ToastContext = createContext<ToastContextValue | undefined>(undefined);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const push = useCallback((toast: Omit<Toast, 'id'>) => {
    const id = `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`;
    const newToast: Toast = { id, ...toast };
    setToasts((t) => [...t, newToast]);
    if (toast.duration !== 0) {
      const dur = toast.duration ?? 4000;
      setTimeout(() => {
        setToasts((t) => t.filter((x) => x.id !== id));
      }, dur);
    }
  }, []);

  const remove = useCallback((id: string) => {
    setToasts((t) => t.filter((x) => x.id !== id));
  }, []);

  return (
    <ToastContext.Provider value={{ toasts, push, remove }}>
      {children}
      <div aria-live="polite" className="fixed bottom-4 right-4 flex flex-col items-end space-y-2" style={{ zIndex: 999 }}>
        {toasts.map((t) => (
          <div key={t.id} className={`max-w-md w-full px-4 py-3 rounded shadow-md text-sm text-white ${t.variant === 'success' ? 'bg-green-600' : t.variant === 'error' ? 'bg-red-600' : t.variant === 'warning' ? 'bg-yellow-600 text-black' : 'bg-teal-600'}`}>
            <div className="flex items-start justify-between gap-3">
              <div className="whitespace-normal break-words flex-1">{t.message}</div>
              <button className="opacity-80 hover:opacity-100 flex-shrink-0" onClick={() => remove(t.id)}>✕</button>
            </div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within ToastProvider');
  return ctx;
};
