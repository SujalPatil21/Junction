import http from 'k6/http';
import { check, sleep } from 'k6';
import { ENDPOINTS, THRESHOLDS } from '../config.js';
import { getPublicHeaders, getEdgeHeaders } from '../utils/auth.js';
import { generateObservationBatch, generateNugenState } from '../utils/test-data.js';

export const options = {
  stages: [
    { duration: '15s', target: 50 },   // Ramp-up
    { duration: '60s', target: 50 },   // Soak plateau
    { duration: '15s', target: 0 },    // Ramp-down
  ],
  thresholds: THRESHOLDS.SOAK,
};

export default function () {
  const rand = Math.random();

  if (rand < 0.50) {
    const res = http.get(ENDPOINTS.SCENARIOS, { headers: getPublicHeaders() });
    check(res, { 'status is 200': (r) => r.status === 200 });
  } else if (rand < 0.80) {
    const res = http.get(ENDPOINTS.WEATHER, { headers: getPublicHeaders() });
    check(res, { 'weather status is 200': (r) => r.status === 200 });
  } else if (rand < 0.95) {
    const payload = generateObservationBatch(__VU, __ITER, 1);
    const res = http.post(ENDPOINTS.OBSERVATIONS, payload, { headers: getEdgeHeaders() });
    check(res, { 'ingestion status is 200': (r) => r.status === 200 });
  } else {
    const payload = generateNugenState(__VU, __ITER);
    const res = http.post(ENDPOINTS.NUGEN_INTELLIGENCE, payload, { headers: getPublicHeaders() });
    check(res, { 'nugen status is 200': (r) => r.status === 200 });
  }

  sleep(0.05);
}

