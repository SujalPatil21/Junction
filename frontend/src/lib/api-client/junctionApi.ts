/**
 * JUNCTION Typed REST API Client
 *
 * Connects the Next.js presentation frontend directly to the Spring Boot application backend.
 * Base URL defaults to http://localhost:8080 in dev, or NEXT_PUBLIC_API_URL in production.
 */

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export interface ApiClientOptions {
  token?: string;
  headers?: HeadersInit;
}

async function request<T>(path: string, options: RequestInit & ApiClientOptions = {}): Promise<T> {
  const { token, headers: customHeaders, ...fetchOptions } = options;

  const headers = new Headers(customHeaders);
  if (!headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const url = `${BASE_URL}${path}`;
  const response = await fetch(url, {
    ...fetchOptions,
    headers,
  });

  if (!response.ok) {
    let errorData;
    try {
      errorData = await response.json();
    } catch {
      errorData = { message: response.statusText };
    }
    throw new Error(errorData.message || `API request failed: ${response.status}`);
  }

  return response.json();
}

export const junctionApi = {
  health: () => request<{ status: string; service: string; version: string }>("/api/health"),

  scenarios: {
    getAll: () => request<any>("/api/scenarios"),
    setActive: (scenarioId: string, token: string) =>
      request<any>("/api/scenarios", {
        method: "POST",
        body: JSON.stringify({ scenarioId }),
        token,
      }),
  },

  zones: {
    getAll: (token?: string) => request<any[]>("/api/zones", { token }),
    getById: (zoneId: string, token?: string) => request<any>(`/api/zones/${zoneId}`, { token }),
    getCoverageProfiles: (token?: string) => request<Record<string, any>>("/api/zones/coverage", { token }),
  },

  recommendations: {
    getAll: (token?: string) => request<any[]>("/api/recommendations", { token }),
    generate: (token?: string) => request<any[]>("/api/recommendations/generate", { method: "POST", token }),
    approve: (id: string, rationale?: string, token?: string) =>
      request<any>(`/api/recommendations/${id}/approve`, {
        method: "POST",
        body: JSON.stringify({ rationale }),
        token,
      }),
    reject: (id: string, reason?: string, token?: string) =>
      request<any>(`/api/recommendations/${id}/reject`, {
        method: "POST",
        body: JSON.stringify({ reason }),
        token,
      }),
    execute: (id: string, token?: string) =>
      request<any>(`/api/recommendations/${id}/execute`, { method: "POST", token }),
  },

  observations: {
    ingest: (observations: any[], edgeSecret?: string) =>
      request<any>("/api/observations", {
        method: "POST",
        body: JSON.stringify({ observations }),
        headers: edgeSecret ? { Authorization: `Bearer ${edgeSecret}` } : undefined,
      }),
    getRecent: (zoneId?: string, token?: string) =>
      request<any[]>(`/api/observations${zoneId ? `?zoneId=${zoneId}` : ""}`, { token }),
  },

  forecast: {
    getAll: (token?: string) => request<any[]>("/api/forecast", { token }),
  },

  routing: {
    calculate: (startLat: number, startLng: number, endLat: number, endLng: number, token?: string) =>
      request<any>(`/api/routing?startLat=${startLat}&startLng=${startLng}&endLat=${endLat}&endLng=${endLng}`, {
        token,
      }),
  },

  restaurants: {
    getAll: (zone?: string, token?: string) =>
      request<any[]>(`/api/restaurants${zone ? `?zone=${zone}` : ""}`, { token }),
    getById: (id: string, token?: string) => request<any>(`/api/restaurants/${id}`, { token }),
    create: (data: any, token?: string) =>
      request<any>("/api/restaurants", { method: "POST", body: JSON.stringify(data), token }),
    update: (id: string, data: any, token?: string) =>
      request<any>(`/api/restaurants/${id}`, { method: "PUT", body: JSON.stringify(data), token }),
    recordInventory: (id: string, data: any, token?: string) =>
      request<any>(`/api/restaurants/${id}/inventory`, {
        method: "POST",
        body: JSON.stringify(data),
        token,
      }),
  },

  audit: {
    getRecent: (token?: string) => request<any[]>("/api/audit", { token }),
  },
};

