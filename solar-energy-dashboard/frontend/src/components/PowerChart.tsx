import { useEffect, useMemo, useState } from 'react';
import { Area, CartesianGrid, ComposedChart, Line, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts';
import { api } from '../api';
import { axisLabel, kw } from '../format';
import type { ReadingPoint, SystemSummary } from '../types';

const RANGES = [
  { label: '24 hours', hours: 24 },
  { label: '48 hours', hours: 48 },
  { label: '7 days', hours: 168 },
];

export default function PowerChart({ system }: { system: SystemSummary }) {
  const [hours, setHours] = useState(48);
  const [points, setPoints] = useState<ReadingPoint[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    setPoints(null);
    setError(null);
    api.readings(system.id, hours)
      .then((p) => { if (!cancelled) setPoints(p); })
      .catch((e: unknown) => { if (!cancelled) setError(e instanceof Error ? e.message : 'Request failed'); });
    return () => { cancelled = true; };
  }, [system.id, hours]);

  const data = useMemo(
    () => (points ?? []).map((p) => ({
      ts: p.timestamp,
      actual: p.powerKw,
      expected: p.expectedKw,
      shortfall: Math.max(0, p.expectedKw - p.powerKw),
    })),
    [points],
  );

  return (
    <section aria-labelledby="chart-h" className="chart">
      <div className="section-head">
        <h2 id="chart-h">{system.name}</h2>
        <div className="seg" role="group" aria-label="Time range">
          {RANGES.map((r) => (
            <button key={r.hours} type="button" aria-pressed={r.hours === hours} onClick={() => setHours(r.hours)}>
              {r.label}
            </button>
          ))}
        </div>
      </div>
      <p className="legend">
        <span><i className="swatch actual" /> Produced</span>
        <span><i className="swatch shortfall" /> Shortfall vs expected</span>
        <span><i className="swatch expected" /> Expected (weather-adjusted)</span>
      </p>
      {error && <p className="error" role="alert">Could not load readings: {error}</p>}
      {!error && points === null && <p className="empty">Loading readings…</p>}
      {points && (
        <div role="img" aria-label={`Power output of ${system.name} against expected output over the last ${hours} hours`}>
          <ResponsiveContainer width="100%" height={320}>
            <ComposedChart data={data} margin={{ top: 8, right: 12, bottom: 0, left: 0 }}>
              <CartesianGrid vertical={false} stroke="var(--line)" />
              <XAxis dataKey="ts" tickFormatter={(v: string) => axisLabel(v, system.zoneId)} minTickGap={48}
                     tick={{ fill: 'var(--muted)', fontSize: 12 }} stroke="var(--line)" />
              <YAxis width={72} tickFormatter={(v: number) => `${v} kW`} tick={{ fill: 'var(--muted)', fontSize: 12 }} stroke="var(--line)" />
              <Tooltip
                contentStyle={{ background: 'var(--surface)', border: '1px solid var(--line)', borderRadius: 6, color: 'var(--ink)' }}
                labelFormatter={(v: string) => axisLabel(v, system.zoneId)}
                formatter={(value: number, name: string) => [kw(value), name === 'actual' ? 'Produced' : name === 'expected' ? 'Expected' : 'Shortfall']}
              />
              <Area type="monotone" dataKey="actual" stackId="p" stroke="var(--actual)" fill="var(--actual-fill)" strokeWidth={2} isAnimationActive={false} />
              <Area type="monotone" dataKey="shortfall" stackId="p" stroke="none" fill="var(--shortfall)" isAnimationActive={false} />
              <Line type="monotone" dataKey="expected" stroke="var(--expected)" strokeDasharray="5 4" strokeWidth={1.5} dot={false} isAnimationActive={false} />
            </ComposedChart>
          </ResponsiveContainer>
        </div>
      )}
    </section>
  );
}
