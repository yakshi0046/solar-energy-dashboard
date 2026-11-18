import type { AlertView, FleetSummary, ReadingPoint, SystemSummary } from './types';

// Empty in development (Vite proxies /api). In production set VITE_API_URL to the deployed API.
const BASE = (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, '') ?? '';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(BASE + path, init);
  if (!res.ok) throw new Error(`${res.status} ${res.statusText}`);
  return (await res.json()) as T;
}

export const api = {
  summary: () => request<FleetSummary>('/api/summary'),
  systems: () => request<SystemSummary[]>('/api/systems'),
  alerts: () => request<AlertView[]>('/api/alerts'),
  readings: (id: number, hours: number) =>
    request<ReadingPoint[]>(`/api/systems/${id}/readings?hours=${hours}`),
  evaluate: () => request<{ changes: number }>('/api/alerts/evaluate', { method: 'POST' }),
};
