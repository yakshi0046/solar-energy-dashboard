import type { AlertView } from '../types';
import { timeAgo } from '../format';

interface Props {
  alerts: AlertView[];
  checking: boolean;
  onSelect: (systemId: number) => void;
  onRecheck: () => void;
}

const TITLES = { OUTAGE: 'Outage', UNDERPERFORMANCE: 'Underperforming' } as const;

export default function AlertList({ alerts, checking, onSelect, onRecheck }: Props) {
  return (
    <section aria-labelledby="alerts-h" className="alerts">
      <div className="section-head">
        <h2 id="alerts-h">Open alerts ({alerts.length})</h2>
        <button type="button" className="btn" onClick={onRecheck} disabled={checking}>
          {checking ? 'Checking…' : 'Re-check now'}
        </button>
      </div>
      {alerts.length === 0 ? (
        <p className="empty">No open alerts. Every system is producing at least 80% of its expected output.</p>
      ) : (
        <ul className="alert-list">
          {alerts.map((a) => (
            <li key={a.id} className={`alert ${a.severity.toLowerCase()}`}>
              <span className="badge">
                <span aria-hidden="true">{a.severity === 'CRITICAL' ? '▲' : '●'}</span>
                {a.severity === 'CRITICAL' ? 'Critical' : 'Warning'}
              </span>
              <div>
                <button type="button" className="link" onClick={() => onSelect(a.systemId)}>
                  {a.systemName}
                </button>
                <span className="kind"> {TITLES[a.type]}</span>
                <p>{a.message}</p>
              </div>
              <time dateTime={a.createdAt}>{timeAgo(a.createdAt)}</time>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
