/**
 * Synthetic Test Data Generation for k6 Benchmarks
 * Generates isolated, realistic payloads without polluting production data.
 */

const ZONES = ['ZONE_WANKHEDE', 'ZONE_CHURCHGATE', 'ZONE_CSMT', 'ZONE_MARINE_DRIVE'];
const SOURCES = ['DEV_CCTV_WANKHEDE_01', 'DEV_CCTV_WANKHEDE_02', 'DEV_CCTV_GATE_1', 'DEV_CCTV_CHURCHGATE_01'];
const METRICS = ['CROWD_COUNT', 'DENSITY', 'INFLOW_RATE', 'OUTFLOW_RATE'];

export function generateObservationBatch(vu, iter, batchSize = 1) {
  const observations = [];
  const now = new Date().toISOString();

  for (let i = 0; i < batchSize; i++) {
    const zoneId = ZONES[(vu + iter + i) % ZONES.length];
    const sourceId = SOURCES[(vu + i) % SOURCES.length];
    const metricType = METRICS[i % METRICS.length];
    const id = `OBS_K6_VU${vu}_IT${iter}_B${i}_${Date.now()}_${Math.floor(Math.random() * 10000)}`;

    observations.push({
      id: id,
      sourceId: sourceId,
      zoneId: zoneId,
      metricType: metricType,
      value: 800 + Math.floor(Math.random() * 1200),
      confidence: 0.85 + Math.random() * 0.14,
      qualityStatus: 'FRESH',
      observedAt: now,
    });
  }

  return JSON.stringify({ observations });
}

export function generatePlanContext(vu, iter) {
  return JSON.stringify({
    scenario: 'POST_EVENT_SURGE',
    initiator: `K6_BENCH_VU_${vu}`,
    requestTimestamp: new Date().toISOString(),
    zones: [
      { id: 'ZONE_WANKHEDE', pressure: 88 + (iter % 10) },
      { id: 'ZONE_CHURCHGATE', pressure: 74 + (iter % 15) },
    ],
  });
}

export function generateNugenState(vu, iter) {
  return JSON.stringify({
    targetVenue: 'Wankhede Stadium',
    activeScenario: 'POST_EVENT_SURGE',
    zoneDensityMap: {
      ZONE_WANKHEDE: 91,
      ZONE_CHURCHGATE: 78,
    },
    concurrencyTag: `VU_${vu}_ITER_${iter}`,
  });
}

export function generateRoutingPayload() {
  return JSON.stringify({
    origin: { latitude: 18.9389, longitude: 72.8258 },
    destination: { latitude: 18.9355, longitude: 72.8272 },
    mode: 'WALKING',
    congestionAware: true,
  });
}

