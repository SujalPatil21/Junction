import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { ENDPOINTS, THRESHOLDS } from '../config.js';
import { getEdgeHeaders, getPublicHeaders } from '../utils/auth.js';
import { generateObservationBatch, generatePlanContext, generateNugenState } from '../utils/test-data.js';

export const options = {
  stages: [
    { duration: '10s', target: 5 },    // Stage 0: Smoke (5 VUs)
    { duration: '20s', target: 10 },   // Stage 1: Baseline (10 VUs)
    { duration: '20s', target: 25 },   // Stage 2: Load 1 (25 VUs)
    { duration: '30s', target: 50 },   // Stage 3: Load 2 (50 VUs)
    { duration: '30s', target: 100 },  // Stage 4: Load 3 (100 VUs)
    { duration: '30s', target: 150 },  // Stage 5: Load 4 (150 VUs)
    { duration: '40s', target: 250 },  // Stage 6: Load 5 (250 VUs - Peak GeoWatch Target)
    { duration: '15s', target: 0 },    // Ramp-down
  ],
  thresholds: THRESHOLDS.PROGRESSIVE_LOAD,
};

export default function () {
  const rand = Math.random();

  // 1. High-frequency reads (60% traffic): scenarios, weather, social signals
  if (rand < 0.60) {
    const selector = rand % 3;
    if (selector < 1) {
      const res = http.get(ENDPOINTS.SCENARIOS, { headers: getPublicHeaders() });
      check(res, { 'scenarios 200': (r) => r.status === 200 });
    } else if (selector < 2) {
      const res = http.get(ENDPOINTS.WEATHER, { headers: getPublicHeaders() });
      check(res, { 'weather 200': (r) => r.status === 200 });
    } else {
      const res = http.get(ENDPOINTS.SOCIAL_SIGNALS, { headers: getPublicHeaders() });
      check(res, { 'signals 200': (r) => r.status === 200 });
    }
  }
  // 2. High-throughput edge observation ingestion writes (25% traffic)
  else if (rand < 0.85) {
    const payload = generateObservationBatch(__VU, __ITER, 2);
    const res = http.post(ENDPOINTS.OBSERVATIONS, payload, { headers: getEdgeHeaders() });
    check(res, {
      'ingestion 200': (r) => r.status === 200,
      'observations accepted': (r) => r.json('acceptedCount') > 0,
    });
  }
  // 3. Domain Intelligence Evaluation (10% traffic)
  else if (rand < 0.95) {
    const payload = generateNugenState(__VU, __ITER);
    const res = http.post(ENDPOINTS.NUGEN_INTELLIGENCE, payload, { headers: getPublicHeaders() });
    check(res, { 'nugen 200': (r) => r.status === 200 });
  }
  // 4. Multi-Horizon Recommendation Planning (5% traffic)
  else {
    const payload = generatePlanContext(__VU, __ITER);
    const res = http.post(ENDPOINTS.RECOMMENDATION_PLAN, payload, { headers: getPublicHeaders() });
    check(res, { 'plan 200': (r) => r.status === 200 });
  }

  // Small pacing to emulate real-world client interaction intervals
  sleep(0.05);
}

