import { useCallback, useEffect, useState } from 'react';
import { api } from './api';
import AlertList from './components/AlertList';
import FleetStats from './components/FleetStats';
import PowerChart from './components/PowerChart';
import SystemList, { worstFirst } from './components/SystemList';
import type { AlertView, FleetSummary, SystemSummary } from './types';

interface Data {
  summary: FleetSummary;
  systems: SystemSummary[];
  alerts: AlertView[];
}

export default function App() {
  const [data, setData] = useState<Data | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [slow, setSlow] = useState(false);
  const [checking, setChecking] = useState(false);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const load = useCallback(async () => {
    setError(null);
    const timer = setTimeout(() => setSlow(true), 4000);
    try {
      const [summary, systems, alerts] = await Promise.all([api.summary(), api.systems(), api.alerts()]);
      setData({ summary, systems, alerts });
      setSelectedId((prev) => prev ?? worstFirst(systems)[0]?.id ?? null);
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Unknown error');
    } finally {
      clearTimeout(timer);
      setSlow(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const recheck = async () => {
    setChecking(true);
    try {
      await api.evaluate();
      await load();
    } finally {
      setChecking(false);
    }
  };

  const selected = data?.systems.find((s) => s.id === selectedId) ?? null;

  return (
    <div className="page">
      <header>
        <h1>Solar fleet monitor</h1>
        {data && <FleetStats summary={data.summary} />}
      </header>

      {error && (
        <div className="error" role="alert">
          <p>The API did not respond: {error}. Check that the backend is running, then try again.</p>
          <button type="button" className="btn" onClick={() => void load()}>Try again</button>
        </div>
      )}
      {!data && !error && (
        <p className="empty" aria-live="polite">
          {slow ? 'Waking up the API. Free hosting can take up to a minute on the first visit…' : 'Loading fleet data…'}
        </p>
      )}

      {data && (
        <main>
          <AlertList alerts={data.alerts} checking={checking} onSelect={setSelectedId} onRecheck={() => void recheck()} />
          <div className="split">
            <SystemList systems={data.systems} selectedId={selectedId} onSelect={setSelectedId} />
            {selected && <PowerChart key={selected.id} system={selected} />}
          </div>
        </main>
      )}

      <footer>
        Demo with synthetic data, regenerated on every server start. Expected output is weather-adjusted.
      </footer>
    </div>
  );
}
