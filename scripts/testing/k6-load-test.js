import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '5s', target: 20 },  // Ramp-up to 20 virtual users
    { duration: '15s', target: 20 }, // Sustain load
    { duration: '5s', target: 0 },   // Ramp-down
  ],
  thresholds: {
    http_req_duration: ['p(95)<250', 'p(99)<500'], // 95% of requests under 250ms, 99% under 500ms
    http_req_failed: ['rate<0.01'],                 // Error rate < 1%
  },
};

const BASE_URL = __ENV.TARGET_URL || 'http://localhost:8080';
const EDGE_SECRET = __ENV.EDGE_SECRET || 'dev-local-secret-not-for-production';

export default function () {
  // 1. Health check
  const healthRes = http.get(`${BASE_URL}/api/health`);
  check(healthRes, {
    'health status is 200': (r) => r.status === 200,
  });

  // 2. Scenarios list
  const scenariosRes = http.get(`${BASE_URL}/api/scenarios`);
  check(scenariosRes, {
    'scenarios status is 200': (r) => r.status === 200,
  });

  // 3. Edge observation ingestion
  const payload = JSON.stringify({
    observations: [
      {
        id: `OBS_K6_${__VU}_${__ITER}`,
        sourceId: 'DEV_CCTV_WANKHEDE_01',
        zoneId: 'ZONE_WANKHEDE',
        metricType: 'CROWD_COUNT',
        value: 1200 + Math.floor(Math.random() * 500),
        confidence: 0.92,
        qualityStatus: 'FRESH',
        observedAt: new Date().toISOString(),
      },
    ],
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${EDGE_SECRET}`,
    },
  };

  const obsRes = http.post(`${BASE_URL}/api/observations`, payload, params);
  check(obsRes, {
    'ingestion status is 200': (r) => r.status === 200,
  });

  sleep(0.1);
}
