import http from 'k6/http';
import { check, sleep } from 'k6';
import { ENDPOINTS, THRESHOLDS } from '../config.js';
import { getPublicHeaders, getEdgeHeaders } from '../utils/auth.js';
import { generateObservationBatch } from '../utils/test-data.js';

export const options = {
  stages: [
    { duration: '10s', target: 10 },   // Warm-up baseline
    { duration: '5s', target: 200 },   // Instant spike (match ended / gate breach)
    { duration: '20s', target: 200 },  // Sustained spike
    { duration: '5s', target: 10 },    // Fast recovery
    { duration: '10s', target: 10 },   // Normalization
  ],
  thresholds: THRESHOLDS.SPIKE,
};

export default function () {
  const rand = Math.random();

  if (rand < 0.65) {
    const res = http.get(ENDPOINTS.SCENARIOS, { headers: getPublicHeaders() });
    check(res, { 'scenario 200': (r) => r.status === 200 });
  } else if (rand < 0.85) {
    const res = http.get(ENDPOINTS.WEATHER, { headers: getPublicHeaders() });
    check(res, { 'weather 200': (r) => r.status === 200 });
  } else {
    const payload = generateObservationBatch(__VU, __ITER, 1);
    const res = http.post(ENDPOINTS.OBSERVATIONS, payload, { headers: getEdgeHeaders() });
    check(res, { 'ingestion 200': (r) => r.status === 200 });
  }

  sleep(0.02);
}

