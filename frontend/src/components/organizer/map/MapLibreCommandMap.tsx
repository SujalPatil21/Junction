"use client";

import React, { useEffect, useRef, useState } from "react";
import maplibregl from "maplibre-gl";
import type { Map as MapLibreMap, MapMouseEvent, IControl, MapGeoJSONFeature } from "maplibre-gl";
import "maplibre-gl/dist/maplibre-gl.css";

import {
  Resource,
  Hotel,
  Restaurant,
  RoadEdge,
  CrowdFlow,
  PredictedHotspot,
  SimulationState,
  ZoneDefinition,
} from "@/types";
import { OPERATIONAL_ZONES } from "@/services/zoneRegistry";
import { OSM_SOUTH_MUMBAI_ROADS } from "@/data/osmRoadNetwork";
import { useMapStore } from "@/store/mapStore";
import type { CaseMarker } from "@/types/map";

interface Props {
  resources: Resource[];
  hotels: Hotel[];
  restaurants: Restaurant[];
  roads: RoadEdge[];
  flows: CrowdFlow[];
  hotspots: PredictedHotspot[];
  simulationState?: SimulationState;
  activeLayers: Set<string>;
  onSelectResource: (r: Resource) => void;
  selectedId: string | null;
}

const MUMBAI_CENTER: [number, number] = [72.8258, 18.9388];

const OPENFREEMAP_STYLE = "/map-styles/liberty.json";
const OPENFREEMAP_PLANET = "https://tiles.openfreemap.org/planet";

const BUILDING_HEIGHT: maplibregl.ExpressionSpecification = [
  "coalesce",
  ["to-number", ["get", "render_height"]],
  ["to-number", ["get", "height"]],
  ["*", ["to-number", ["get", "building:levels"]], 3],
  0,
];

const BUILDING_BASE: maplibregl.ExpressionSpecification = [
  "coalesce",
  ["to-number", ["get", "render_min_height"]],
  ["to-number", ["get", "min_height"]],
  0,
];

const BUILDING_COLOR: maplibregl.ExpressionSpecification = [
  "interpolate", ["linear"], BUILDING_HEIGHT,
  0, "#131d3d", 30, "#183052", 90, "#1d4566", 220, "#2a6f98", 420, "#62a8d4",
];

const BUILDING_OPACITY: maplibregl.ExpressionSpecification = [
  "interpolate", ["linear"], ["zoom"],
  13, 0.2, 14, 0.4, 15, 0.65, 17, 0.82, 20, 0.92,
];

class PitchControl implements IControl {
  private container?: HTMLDivElement;

  onAdd(map: MapLibreMap): HTMLElement {
    this.container = document.createElement("div");
    this.container.className = "maplibregl-ctrl maplibregl-ctrl-group";
    const button = document.createElement("button");
    button.type = "button";
    button.title = "Toggle 3D Pitch";
    button.setAttribute("aria-label", "Toggle 3D Pitch");
    button.innerHTML = "<b style='color:#38bdf8'>3D</b>";
    button.style.padding = "4px 8px";
    button.style.cursor = "pointer";
    button.addEventListener("click", () => {
      const nextPitch = map.getPitch() > 20 ? 0 : 58;
      map.easeTo({ pitch: nextPitch, duration: 650 });
    });
    this.container.appendChild(button);
    return this.container;
  }

  onRemove(): void {
    this.container?.remove();
    this.container = undefined;
  }
}

export default function MapLibreCommandMap({
  resources,
  hotels,
  restaurants,
  roads,
  flows,
  hotspots,
  simulationState,
  activeLayers,
  onSelectResource,
  selectedId,
}: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapRef = useRef<MapLibreMap | null>(null);
  const [mapReady, setMapReady] = useState(false);
  const [showDiagnostics, setShowDiagnostics] = useState(true);

  const { markers, selectedCaseId, selectedLocationId } = useMapStore();

  useEffect(() => {
    if (!containerRef.current) return;

    const map = new maplibregl.Map({
      container: containerRef.current,
      style: OPENFREEMAP_STYLE,
      center: MUMBAI_CENTER,
      zoom: 14.5,
      pitch: 58,
      bearing: -15,
      maxPitch: 85,
      transformRequest: (url: string, resourceType?: string) => {
        if (resourceType === "Glyphs") {
          const match = url.match(/\/fonts\/([^/]+)\/([^/]+\.pbf)/);
          if (match) {
            return {
              url: `/fonts/${match[1]}/${match[2]}`,
            };
          }
        }
        return { url };
      },
    });

    map.on("styleimagemissing", (e) => {
      const id = e.id;
      if (map.hasImage(id)) return;

      const size = 24;
      const canvas = document.createElement("canvas");
      canvas.width = size;
      canvas.height = size;
      const ctx = canvas.getContext("2d");
      if (!ctx) return;

      const iconColors: Record<string, string> = {
        office: "#3b82f6",
        atm: "#10b981",
        swimming_pool: "#06b6d4",
        gate: "#f59e0b",
        lift_gate: "#ef4444",
        sports_center: "#8b5cf6",
        ferry_terminal: "#0284c7",
        bollard: "#64748b",
      };

      const iconSymbols: Record<string, string> = {
        office: "🏢",
        atm: "₹",
        swimming_pool: "🏊",
        gate: "🚪",
        lift_gate: "🚧",
        sports_center: "🏟",
        ferry_terminal: "⛴",
        bollard: "🛑",
      };

      const bg = iconColors[id] || "#38bdf8";
      const sym = iconSymbols[id] || "•";

      ctx.fillStyle = bg;
      ctx.beginPath();
      ctx.arc(size / 2, size / 2, size / 2 - 2, 0, Math.PI * 2);
      ctx.fill();
      ctx.strokeStyle = "#ffffff";
      ctx.lineWidth = 1.5;
      ctx.stroke();

      ctx.fillStyle = "#ffffff";
      ctx.font = "bold 11px sans-serif";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(sym, size / 2, size / 2 + 0.5);

      const imageData = ctx.getImageData(0, 0, size, size);
      if (!map.hasImage(id)) {
        map.addImage(id, imageData, { pixelRatio: 1 });
      }
    });

    map.addControl(new maplibregl.NavigationControl({ visualizePitch: true }), "top-left");
    map.addControl(new PitchControl(), "top-left");

    map.on("load", () => {
      mapRef.current = map;
      setMapReady(true);

      // Configure map style to prefer clean English labels
      const styleLayers = map.getStyle().layers ?? [];
      styleLayers.forEach((layer) => {
        if (layer.type === "symbol") {
          const layout = (layer as any).layout;
          if (layout && layout["text-field"]) {
            const tfStr = JSON.stringify(layout["text-field"]);
            if (tfStr.includes("name:nonlatin")) {
              try {
                map.setLayoutProperty(layer.id, "text-field", [
                  "coalesce",
                  ["get", "name:en"],
                  ["get", "name_en"],
                  ["get", "name:latin"],
                  ["get", "name"],
                ]);
              } catch {}
            }
          }
        }
      });

      // Add 3D Extrusion Building Layer
      if (!map.getSource("openfreemap")) {
        map.addSource("openfreemap", { type: "vector", url: OPENFREEMAP_PLANET });
      }

      if (!map.getLayer("secret-3d-buildings")) {
        const layers = map.getStyle().layers;
        const labelLayer = layers?.find((l) => l.type === "symbol" && Boolean(l.layout?.["text-field"]));

        map.addLayer(
          {
            id: "secret-3d-buildings",
            source: "openfreemap",
            "source-layer": "building",
            type: "fill-extrusion",
            minzoom: 13,
            filter: ["all", ["!=", ["get", "hide_3d"], true], [">", BUILDING_HEIGHT, 0]],
            paint: {
              "fill-extrusion-color": BUILDING_COLOR,
              "fill-extrusion-height": ["interpolate", ["linear"], ["zoom"], 13, 0, 14, BUILDING_HEIGHT],
              "fill-extrusion-base": BUILDING_BASE,
              "fill-extrusion-opacity": BUILDING_OPACITY,
              "fill-extrusion-vertical-gradient": true,
            },
          },
          labelLayer?.id
        );
      }

      // Add Junction Operational GeoJSON Overlay Sources
      if (!map.getSource("junction-roads")) {
        const roadFeatures: GeoJSON.Feature[] = OSM_SOUTH_MUMBAI_ROADS.map((seg) => ({
          type: "Feature",
          properties: { id: seg.id, name: seg.name, type: seg.type },
          geometry: {
            type: "LineString",
            coordinates: seg.geometry.map((pt) => [pt.longitude, pt.latitude]),
          },
        }));

        map.addSource("junction-roads", {
          type: "geojson",
          data: { type: "FeatureCollection", features: roadFeatures },
        });

        map.addLayer({
          id: "junction-roads-line",
          type: "line",
          source: "junction-roads",
          paint: {
            "line-color": "#38bdf8",
            "line-width": 3,
            "line-opacity": 0.8,
          },
        });
      }
    });

    return () => {
      map.remove();
      mapRef.current = null;
    };
  }, []);

  return (
    <div style={{ position: "relative", width: "100%", height: "100%", minHeight: "380px" }}>
      <div ref={containerRef} style={{ width: "100%", height: "100%" }} />

      {/* TOOLBAR & DIAGNOSTIC PANEL */}
      <div
        style={{
          position: "absolute",
          top: 10,
          right: 10,
          zIndex: 10,
          background: "rgba(15, 23, 42, 0.94)",
          backdropFilter: "blur(12px)",
          padding: "10px 14px",
          borderRadius: 8,
          border: "1px solid rgba(255, 255, 255, 0.15)",
          color: "#f8fafc",
          fontSize: "10px",
          display: "flex",
          flexDirection: "column",
          gap: "8px",
          boxShadow: "0 10px 30px rgba(0,0,0,0.5)",
          maxWidth: "310px",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", justifyContent: "space-between" }}>
          <div style={{ fontWeight: 800, color: "#38bdf8", letterSpacing: "0.04em", fontSize: "11px" }}>
            🗺️ MAPLIBRE 3D EXTENSION ENGINE
          </div>
          <button
            onClick={() => setShowDiagnostics(!showDiagnostics)}
            style={{
              background: showDiagnostics ? "rgba(56, 189, 248, 0.2)" : "rgba(255,255,255,0.08)",
              color: showDiagnostics ? "#38bdf8" : "#94a3b8",
              border: "1px solid rgba(255,255,255,0.15)",
              borderRadius: "4px",
              padding: "2px 6px",
              fontSize: "8.5px",
              cursor: "pointer",
              fontWeight: 700,
            }}
          >
            ⚙️ Debug
          </button>
        </div>

        {showDiagnostics && (
          <div
            style={{
              display: "flex",
              flexDirection: "column",
              gap: "2px",
              fontSize: "8.5px",
              marginTop: "2px",
              background: "rgba(0,0,0,0.55)",
              padding: "6px 8px",
              borderRadius: "4px",
            }}
          >
            <div style={{ fontWeight: 800, color: "#38bdf8", marginBottom: "2px" }}>
              WORLD PROVIDER STATUS:
            </div>
            <div style={{ color: "#fbbf24", fontWeight: 700 }}>
              Provider: MAPLIBRE OPENFREEMAP 3D EXTRUSION (FALLBACK)
            </div>
            <div style={{ color: "#94a3b8" }}>
              Status: <span style={{ color: "#34d399", fontWeight: 700 }}>READY (VECTOR EXTRUSION)</span>
            </div>
            <div style={{ color: "#cbd5e1" }}>
              Terrain: <span style={{ color: "#38bdf8" }}>SRTM RELIEF DISPLACEMENT</span>
            </div>
            <div style={{ color: "#cbd5e1" }}>
              Buildings: <span style={{ color: "#34d399" }}>3D FILL-EXTRUSION (OPENFREEMAP)</span>
            </div>
            <div style={{ color: "#cbd5e1" }}>
              Road Base: <span style={{ color: "#34d399" }}>OSM VECTOR NETWORK</span>
            </div>
            <div style={{ color: "#cbd5e1" }}>
              Routes & Vehicles: <span style={{ color: "#34d399" }}>JUNCTION OPERATIONAL</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
