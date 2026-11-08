/**
 * JUNCTION Performance & Load Testing Configuration
 * Defines target environments, credentials, endpoints, and SLA thresholds.
 */

export const BASE_URL = __ENV.TARGET_URL || __ENV.BASE_URL || 'http://localhost:8080';
export const EDGE_SECRET = __ENV.EDGE_SECRET || __ENV.JUNCTION_EDGE_DEVICE_SECRET || 'dev-local-secret-not-for-production';
export const CLERK_JWT = __ENV.CLERK_JWT || '';

export const ENDPOINTS = {
  HEALTH: `${BASE_URL}/api/health`,
  ACTUATOR_HEALTH: `${BASE_URL}/actuator/health`,
  ACTUATOR_METRICS: `${BASE_URL}/actuator/metrics`,
  SCENARIOS: `${BASE_URL}/api/scenarios`,
  OBSERVATIONS: `${BASE_URL}/api/observations`,
  WEATHER: `${BASE_URL}/api/weather`,
  SOCIAL_SIGNALS: `${BASE_URL}/api/social-signals`,
  NUGEN_INTELLIGENCE: `${BASE_URL}/api/nugen/intelligence`,
  RECOMMENDATION_PLAN: `${BASE_URL}/api/recommendations/plan`,
  RECOMMENDATIONS: `${BASE_URL}/api/recommendations`,
  ZONES: `${BASE_URL}/api/zones`,
  FORECAST: `${BASE_URL}/api/forecast`,
  HOTSPOTS: `${BASE_URL}/api/hotspots`,
  ROUTING: `${BASE_URL}/api/routing`,
  WS_ENDPOINT: `${BASE_URL.replace('http', 'ws')}/ws`,
  STOMP_ENDPOINT: `${BASE_URL.replace('http', 'ws')}/stomp`,
};

export const THRESHOLDS = {
  SMOKE: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<300', 'p(99)<600'],
  },
  PROGRESSIVE_LOAD: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
  STRESS: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<1500'],
  },
  SPIKE: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<2000'],
  },
  SOAK: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500', 'p(99)<1000'],
  },
  MIXED: {
    http_req_failed: ['rate<0.02'],
    http_req_duration: ['p(95)<600', 'p(99)<1200'],
  },
};

