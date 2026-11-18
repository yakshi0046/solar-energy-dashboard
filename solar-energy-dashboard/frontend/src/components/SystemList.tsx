import type { Status, SystemSummary } from '../types';
import { kw, kwh, pct } from '../format';

const RANK: Record<Status, number> = { CRITICAL: 0, WARNING: 1, HEALTHY: 2 };
const LABEL: Record<Status, string> = { CRITICAL: 'Critical', WARNING: 'Warning', HEALTHY: 'Healthy' };

export const worstFirst = (systems: SystemSummary[]) =>
  [...systems].sort((a, b) => RANK[a.status] - RANK[b.status] || a.name.localeCompare(b.name));

interface Props {
  systems: SystemSummary[];
  selectedId: number | null;
  onSelect: (id: number) => void;
}

export default function SystemList({ systems, selectedId, onSelect }: Props) {
  return (
    <section aria-labelledby="systems-h" className="systems">
      <h2 id="systems-h">Systems, worst first</h2>
      <ul>
        {worstFirst(systems).map((s) => (
          <li key={s.id}>
            <button
              type="button"
              className={`system ${s.id === selectedId ? 'selected' : ''}`}
              aria-pressed={s.id === selectedId}
              onClick={() => onSelect(s.id)}
            >
              <span className="row">
                <span className={`dot ${s.status.toLowerCase()}`} aria-hidden="true" />
                <strong>{s.name}</strong>
                <span className="status-text">{LABEL[s.status]}</span>
              </span>
              <span className="meta">{s.location} · {kw(s.capacityKw)}</span>
              <span className="ratio">
                <span className="bar" role="img" aria-label={`Output is ${pct(s.performanceRatio)} of expected`}>
                  <span className={`fill ${s.status.toLowerCase()}`} style={{ width: `${Math.min(100, s.performanceRatio * 100)}%` }} />
                </span>
                <span>{pct(s.performanceRatio)} of expected</span>
              </span>
              <span className="meta">{kwh(s.energy24hKwh)} in 24 h</span>
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}
