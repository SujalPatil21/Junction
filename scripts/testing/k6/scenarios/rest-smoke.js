import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { ENDPOINTS, THRESHOLDS } from '../config.js';
import { getEdgeHeaders, getPublicHeaders, getClerkHeaders } from '../utils/auth.js';
import { generateObservationBatch, generatePlanContext, generateNugenState } from '../utils/test-data.js';

export const options = {
  vus: 2,
  duration: '15s',
  thresholds: THRESHOLDS.SMOKE,
};

export default function () {
  group('1. Public Health & System Status', () => {
    const res = http.get(ENDPOINTS.HEALTH, { headers: getPublicHeaders() });
    check(res, {
      'health status is 200': (r) => r.status === 200,
      'health body contains status ok': (r) => r.json('status') === 'ok',
    });
  });

  group('2. Operational Scenarios & Digital Twin', () => {
    const res = http.get(ENDPOINTS.SCENARIOS, { headers: getPublicHeaders() });
    check(res, {
      'scenarios status is 200': (r) => r.status === 200,
      'has activeScenario': (r) => r.json('activeScenario') !== undefined,
    });
  });

  group('3. Meteorological Telemetry & Signals', () => {
    const weatherRes = http.get(ENDPOINTS.WEATHER, { headers: getPublicHeaders() });
    check(weatherRes, {
      'weather status is 200': (r) => r.status === 200,
      'weather success is true': (r) => r.json('success') === true,
    });

    const signalsRes = http.get(ENDPOINTS.SOCIAL_SIGNALS, { headers: getPublicHeaders() });
    check(signalsRes, {
      'social signals status is 200': (r) => r.status === 200,
      'signals array exists': (r) => Array.isArray(r.json('signals')),
    });
  });

  group('4. NUGEN Domain Intelligence', () => {
    const payload = generateNugenState(__VU, __ITER);
    const res = http.post(ENDPOINTS.NUGEN_INTELLIGENCE, payload, { headers: getPublicHeaders() });
    check(res, {
      'nugen intelligence status is 200': (r) => r.status === 200,
      'nugen returned intelligence object': (r) => r.json('intelligence.isAvailable') === true,
    });
  });

  group('5. Authoritative Recommendation Planning', () => {
    const payload = generatePlanContext(__VU, __ITER);
    const res = http.post(ENDPOINTS.RECOMMENDATION_PLAN, payload, { headers: getPublicHeaders() });
    check(res, {
      'plan status is 200': (r) => r.status === 200,
      'plan success is true': (r) => r.json('success') === true,
      'plan contains recommendations': (r) => r.json('plan.recommendations') !== undefined,
    });
  });

  group('6. Edge Device Observation Ingestion (Write)', () => {
    const payload = generateObservationBatch(__VU, __ITER, 2);
    const res = http.post(ENDPOINTS.OBSERVATIONS, payload, { headers: getEdgeHeaders() });
    check(res, {
      'ingestion status is 200': (r) => r.status === 200,
      'accepted count > 0': (r) => r.json('acceptedCount') > 0,
    });
  });

  sleep(0.5);
}

