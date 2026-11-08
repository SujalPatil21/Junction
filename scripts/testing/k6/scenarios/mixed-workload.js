import http from 'k6/http';
import { check, sleep } from 'k6';
import { ENDPOINTS, THRESHOLDS } from '../config.js';
import { getPublicHeaders, getEdgeHeaders } from '../utils/auth.js';
import { generateObservationBatch, generateNugenState, generatePlanContext } from '../utils/test-data.js';

export const options = {
  stages: [
    { duration: '10s', target: 50 },   // Warm-up ramp
    { duration: '15s', target: 150 },  // Mid load
    { duration: '45s', target: 250 },  // Peak GeoWatch target (250 VUs)
    { duration: '10s', target: 0 },    // Cooldown
  ],
  thresholds: THRESHOLDS.MIXED,
};

export default function () {
  const rand = Math.random();

  // 1. High-speed cached reads (60%): Scenario, Weather, Signals
  if (rand < 0.60) {
    const selector = (rand * 100) % 3;
    if (selector < 1) {
      const res = http.get(ENDPOINTS.SCENARIOS, { headers: getPublicHeaders() });
      check(res, { 'scenario 200': (r) => r.status === 200 });
    } else if (selector < 2) {
      const res = http.get(ENDPOINTS.WEATHER, { headers: getPublicHeaders() });
      check(res, { 'weather 200': (r) => r.status === 200 });
    } else {
      const res = http.get(ENDPOINTS.SOCIAL_SIGNALS, { headers: getPublicHeaders() });
      check(res, { 'signals 200': (r) => r.status === 200 });
    }
  }
  // 2. High-speed Ingestion write (25%)
  else if (rand < 0.85) {
    const payload = generateObservationBatch(__VU, __ITER, 1);
    const res = http.post(ENDPOINTS.OBSERVATIONS, payload, { headers: getEdgeHeaders() });
    check(res, { 'ingestion 200': (r) => r.status === 200 });
  }
  // 3. Domain evaluation (10%)
  else if (rand < 0.95) {
    const payload = generateNugenState(__VU, __ITER);
    const res = http.post(ENDPOINTS.NUGEN_INTELLIGENCE, payload, { headers: getPublicHeaders() });
    check(res, { 'nugen 200': (r) => r.status === 200 });
  }
  // 4. Recommendation planning (5%)
  else {
    const payload = generatePlanContext(__VU, __ITER);
    const res = http.post(ENDPOINTS.RECOMMENDATION_PLAN, payload, { headers: getPublicHeaders() });
    check(res, { 'plan 200': (r) => r.status === 200 });
  }

  // High-performance pacing
  sleep(0.01);
}

