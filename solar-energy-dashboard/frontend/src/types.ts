export type Status = 'HEALTHY' | 'WARNING' | 'CRITICAL';

export interface FleetSummary {
  systems: number;
  totalCapacityKw: number;
  currentPowerKw: number;
  energy24hKwh: number;
  openAlerts: number;
}

export interface SystemSummary {
  id: number;
  name: string;
  location: string;
  zoneId: string;
  capacityKw: number;
  currentPowerKw: number;
  energy24hKwh: number;
  expected24hKwh: number;
  performanceRatio: number;
  status: Status;
}

export interface ReadingPoint {
  timestamp: string;
  powerKw: number;
  expectedKw: number;
}

export interface AlertView {
  id: number;
  systemId: number;
  systemName: string;
  type: 'UNDERPERFORMANCE' | 'OUTAGE';
  severity: 'WARNING' | 'CRITICAL';
  message: string;
  createdAt: string;
}
