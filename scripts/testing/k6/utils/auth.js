/**
 * Authentication and Header Utilities for k6
 */
import { EDGE_SECRET, CLERK_JWT } from '../config.js';

export function getEdgeHeaders() {
  return {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
    'Authorization': `Bearer ${EDGE_SECRET}`,
  };
}

export function getClerkHeaders(customToken) {
  const token = customToken || CLERK_JWT;
  const headers = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

export function getPublicHeaders() {
  return {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };
}

