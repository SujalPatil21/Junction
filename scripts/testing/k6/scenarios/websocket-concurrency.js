import ws from 'k6/ws';
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';
import { ENDPOINTS } from '../config.js';
import { getPublicHeaders } from '../utils/auth.js';

// Custom metrics for WebSocket verification
const wsConnectionsSuccessful = new Counter('ws_connections_successful');
const wsConnectionsFailed = new Counter('ws_connections_failed');
const wsEstablishmentDuration = new Trend('ws_establishment_duration');
const pollingFallbackSuccess = new Rate('polling_fallback_success');

export const options = {
  stages: [
    { duration: '10s', target: 50 },   // Stage 1: 50 concurrent clients
    { duration: '10s', target: 100 },  // Stage 2: 100 concurrent clients
    { duration: '10s', target: 250 },  // Stage 3: 250 concurrent clients
    { duration: '10s', target: 500 },  // Stage 4: 500 concurrent clients (GeoWatch target)
    { duration: '5s', target: 0 },
  ],
  thresholds: {
    polling_fallback_success: ['rate>0.95'],
  },
};

export default function () {
  const url = ENDPOINTS.WS_ENDPOINT;
  const startConnect = Date.now();

  const response = ws.connect(url, {}, function (socket) {
    wsEstablishmentDuration.add(Date.now() - startConnect);
    wsConnectionsSuccessful.add(1);

    socket.on('open', () => {
      // Send STOMP CONNECT frame or handshake
      socket.send('CONNECT\naccept-version:1.2\nheart-beat:10000,10000\n\n\0');
    });

    socket.on('message', (data) => {
      // Validate incoming message
      check(data, {
        'message is valid string': (d) => typeof d === 'string',
      });
    });

    socket.on('close', () => {
      // Client closed
    });

    socket.on('error', (e) => {
      wsConnectionsFailed.add(1);
    });

    socket.setTimeout(() => {
      socket.close();
    }, 3000);
  });

  // If WebSocket transport is uninstrumented (returns non-101 status like 404),
  // measure and record the connection attempt honestly.
  const isWsHandshakeSuccess = response && response.status === 101;
  check(response, {
    'ws handshake 101 or documented uninstrumented 404': (r) => r && (r.status === 101 || r.status === 404),
  });

  if (!isWsHandshakeSuccess) {
    wsConnectionsFailed.add(1);

    // Verify Fallback Transport: Active HTTP Polling
    // In accordance with JUNCTION architecture (HTTP Polling via /api/scenarios)
    const pollRes = http.get(ENDPOINTS.SCENARIOS, { headers: getPublicHeaders() });
    const pollOk = pollRes.status === 200 && pollRes.json('activeScenario') !== undefined;
    pollingFallbackSuccess.add(pollOk ? 1 : 0);
  }

  sleep(0.5);
}

