import http from 'k6/http';
import { check, sleep } from 'k6';
import { ENDPOINTS, THRESHOLDS } from '../config.js';
import { getEdgeHeaders, getPublicHeaders } from '../utils/auth.js';
import { generateObservationBatch, generateNugenState } from '../utils/test-data.js';

export const options = {
  stages: [
    { duration: '15s', target: 50 },
    { duration: '20s', target: 150 },
    { duration: '20s', target: 250 },
    { duration: '30s', target: 350 },  // Over-saturation breaking point
    { duration: '15s', target: 0 },
  ],
  thresholds: THRESHOLDS.STRESS,
};

export default function () {
  const rand = Math.random();

  if (rand < 0.70) {
    const res = http.get(ENDPOINTS.SCENARIOS, { headers: getPublicHeaders() });
    check(res, { 'status is 200': (r) => r.status === 200 });
  } else if (rand < 0.90) {
    const payload = generateObservationBatch(__VU, __ITER, 1);
    const res = http.post(ENDPOINTS.OBSERVATIONS, payload, { headers: getEdgeHeaders() });
    check(res, { 'ingestion accepted': (r) => r.status === 200 });
  } else {
    const payload = generateNugenState(__VU, __ITER);
    const res = http.post(ENDPOINTS.NUGEN_INTELLIGENCE, payload, { headers: getPublicHeaders() });
    check(res, { 'intelligence evaluated': (r) => r.status === 200 });
  }

  sleep(0.02);
}

