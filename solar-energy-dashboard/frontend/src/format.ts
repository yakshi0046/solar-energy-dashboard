export const kw = (v: number) => `${v.toLocaleString('en', { maximumFractionDigits: 1 })} kW`;
export const kwh = (v: number) => `${Math.round(v).toLocaleString('en')} kWh`;
export const pct = (v: number) => `${Math.round(v * 100)}%`;

export function timeAgo(iso: string): string {
  const mins = Math.max(0, Math.round((Date.now() - new Date(iso).getTime()) / 60000));
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins} min ago`;
  const hours = Math.round(mins / 60);
  return hours < 24 ? `${hours} h ago` : `${Math.round(hours / 24)} d ago`;
}

/** Axis label in the system's own time zone, e.g. "Mon 14:00". */
export function axisLabel(iso: string, zoneId: string): string {
  return new Intl.DateTimeFormat('en-GB', {
    timeZone: zoneId, weekday: 'short', hour: '2-digit', minute: '2-digit', hour12: false,
  }).format(new Date(iso));
}
