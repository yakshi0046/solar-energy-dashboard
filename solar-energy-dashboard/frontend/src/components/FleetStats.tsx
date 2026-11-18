import type { FleetSummary } from '../types';
import { kw, kwh } from '../format';

export default function FleetStats({ summary }: { summary: FleetSummary }) {
  const items: [string, string][] = [
    ['Systems', String(summary.systems)],
    ['Installed', kw(summary.totalCapacityKw)],
    ['Producing now', kw(summary.currentPowerKw)],
    ['Last 24 hours', kwh(summary.energy24hKwh)],
  ];
  return (
    <dl className="fleet">
      {items.map(([label, value]) => (
        <div key={label}>
          <dt>{label}</dt>
          <dd>{value}</dd>
        </div>
      ))}
    </dl>
  );
}
