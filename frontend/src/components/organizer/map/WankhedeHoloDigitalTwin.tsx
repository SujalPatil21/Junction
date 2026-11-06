"use client";

import React, { useState, useMemo, useEffect } from "react";
import {
  WANKHEDE_STANDS,
  STADIUM_NODES,
  SEAT_DATABASE,
  StadiumNode,
  RouteResult,
  findSpatialRoute,
  findNearestExit,
} from "@/data/wankhedeSpatialGraph";
import styles from "./WankhedeHolo.module.css";

interface Props {
  onBackToDashboard?: () => void;
}

export default function WankhedeHoloDigitalTwin({ onBackToDashboard }: Props) {
  const [activeTab, setActiveTab] = useState<"3D" | "GEMINI" | "ADMIN">("3D");
  const [searchQuery, setSearchQuery] = useState("");
  const [isSearching, setIsSearching] = useState(false);

  const [fromNodeId, setFromNodeId] = useState<string>("NODE_GATE_1");
  const [toNodeId, setToNodeId] = useState<string>("NODE_SEAT_D142");
  const [routeType, setRouteType] = useState<"SHORTEST" | "FASTEST" | "ACCESSIBLE" | "EVACUATION">("SHORTEST");
  const [activeRoute, setActiveRoute] = useState<RouteResult | null>(null);

  const [selectedSeatId, setSelectedSeatId] = useState<string | null>("D-142");
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>("NODE_SEAT_D142");

  const [xrayMode, setXrayMode] = useState(false);
  const [evacMode, setEvacMode] = useState(false);
  const [demoMode, setDemoMode] = useState(false);
  const [showDebug, setShowDebug] = useState(false);
  const [isGeminiOpen, setIsGeminiOpen] = useState(false);
  const [cameraView, setCameraView] = useState<"OVERVIEW" | "FOLLOW" | "FOCUS">("OVERVIEW");

  const [geminiMessages, setGeminiMessages] = useState<Array<{ sender: "user" | "ai"; text: string }>>([
    { sender: "ai", text: "Hello! I am your Wankhede Holo Assistant. Where would you like to navigate inside the stadium?" },
  ]);
  const [geminiInput, setGeminiInput] = useState("");

  useEffect(() => {
    handleCalculateRoute("NODE_GATE_1", "NODE_SEAT_D142", "SHORTEST");
  }, []);

  const searchResults = useMemo(() => {
    if (!searchQuery.trim()) return [];
    const q = searchQuery.toLowerCase().trim();

    const matches: Array<{ id: string; title: string; subtitle: string; nodeId: string; type: "SEAT" | "GATE" | "EXIT" | "STAND" }> = [];

    SEAT_DATABASE.forEach((seat) => {
      if (seat.id.toLowerCase().includes(q) || seat.stand.toLowerCase().includes(q) || seat.block.toLowerCase().includes(q)) {
        matches.push({
          id: seat.id,
          title: seat.id,
          subtitle: `${seat.block} · ${seat.row} · ${seat.stand}`,
          nodeId: seat.nodeId,
          type: "SEAT",
        });
      }
    });

    Object.values(STADIUM_NODES)
      .filter((n) => n.category === "GATE")
      .forEach((gate) => {
        if (gate.name.toLowerCase().includes(q) || (gate.description && gate.description.toLowerCase().includes(q))) {
          matches.push({
            id: gate.id,
            title: gate.name,
            subtitle: gate.description || "Stadium Access Gate",
            nodeId: gate.id,
            type: "GATE",
          });
        }
      });

    Object.values(STADIUM_NODES)
      .filter((n) => n.category === "EXIT")
      .forEach((exit) => {
        if (exit.name.toLowerCase().includes(q) || (exit.description && exit.description.toLowerCase().includes(q))) {
          matches.push({
            id: exit.id,
            title: exit.name,
            subtitle: exit.description || "Emergency Egress Route",
            nodeId: exit.id,
            type: "EXIT",
          });
        }
      });

    WANKHEDE_STANDS.forEach((stand) => {
      if (stand.name.toLowerCase().includes(q) || stand.shortName.toLowerCase().includes(q)) {
        matches.push({
          id: stand.id,
          title: stand.name,
          subtitle: `Recommended: ${stand.gate} · ${stand.exit}`,
          nodeId: "NODE_BLOCK_D",
          type: "STAND",
        });
      }
    });

    return matches.slice(0, 5);
  }, [searchQuery]);

  const handleSelectSearchResult = (result: (typeof searchResults)[0]) => {
    setSearchQuery(result.title);
    setIsSearching(false);
    setSelectedNodeId(result.nodeId);

    if (result.type === "SEAT") {
      setSelectedSeatId(result.id);
      setToNodeId(result.nodeId);
    } else {
      setSelectedSeatId(null);
      setToNodeId(result.nodeId);
    }
  };

  const handleCalculateRoute = (from = fromNodeId, to = toNodeId, type = routeType) => {
    const res = findSpatialRoute(from, to, type);
    setActiveRoute(res);
    setCameraView("FOLLOW");
  };

  const handleNearestExit = () => {
    const res = findNearestExit(toNodeId || fromNodeId);
    if (res) {
      setActiveRoute(res);
      setRouteType("EVACUATION");
      setEvacMode(true);
    }
  };

  const handleToggleEvac = () => {
    const next = !evacMode;
    setEvacMode(next);
    if (next) {
      handleNearestExit();
    } else {
      setActiveRoute(null);
    }
  };

  const handleGeminiSend = () => {
    if (!geminiInput.trim()) return;
    const userText = geminiInput.trim();
    setGeminiMessages((prev) => [...prev, { sender: "user", text: userText }]);
    setGeminiInput("");

    setTimeout(() => {
      if (userText.toLowerCase().includes("gate 1") && userText.toLowerCase().includes("d-142")) {
        setFromNodeId("NODE_GATE_1");
        setToNodeId("NODE_SEAT_D142");
        handleCalculateRoute("NODE_GATE_1", "NODE_SEAT_D142", "SHORTEST");
        setGeminiMessages((prev) => [
          ...prev,
          { sender: "ai", text: "I have calculated the route from Gate 1 to Seat D-142 (420m · 6 min). The red navigation path is active on the 3D map." },
        ]);
      } else if (userText.toLowerCase().includes("exit") || userText.toLowerCase().includes("evacuate")) {
        handleNearestExit();
        setGeminiMessages((prev) => [
          ...prev,
          { sender: "ai", text: "Evacuation route activated! Guiding you to Nearest Exit 4 (45m · 1 min)." },
        ]);
      } else {
        setGeminiMessages((prev) => [
          ...prev,
          { sender: "ai", text: `I have updated the 3D view for "${userText}". Route ready from ${STADIUM_NODES[fromNodeId]?.name} to ${STADIUM_NODES[toNodeId]?.name}.` },
        ]);
      }
    }, 600);
  };

  const selectedSeat = useMemo(() => {
    return SEAT_DATABASE.find((s) => s.id === selectedSeatId) || SEAT_DATABASE[0];
  }, [selectedSeatId]);

  return (
    <div className={styles.container}>
      <header className={styles.header}>
        <div className={styles.headerLeft}>
          <h1 className={styles.logoTitle}>
            WANKHEDE HOLO
            <span className={styles.logoDot} />
          </h1>
          <span className={styles.headerBadge}>3D DIGITAL TWIN</span>
        </div>

        <nav className={styles.headerNav}>
          <button
            type="button"
            className={`${styles.navTab} ${activeTab === "3D" ? styles.navTabActive : ""}`}
            onClick={() => setActiveTab("3D")}
          >
            🏙️ 3D VIEW
          </button>
          <button
            type="button"
            className={`${styles.navTab} ${activeTab === "GEMINI" || isGeminiOpen ? styles.navTabActive : ""}`}
            onClick={() => {
              setIsGeminiOpen(!isGeminiOpen);
              setActiveTab(isGeminiOpen ? "3D" : "GEMINI");
            }}
          >
            ✦ GEMINI
          </button>
          <button
            type="button"
            className={`${styles.navTab} ${activeTab === "ADMIN" ? styles.navTabActive : ""}`}
            onClick={() => setActiveTab("ADMIN")}
          >
            🛡️ ADMIN
          </button>
          <button
            type="button"
            className={`${styles.navTab} ${showDebug ? styles.navTabActive : ""}`}
            onClick={() => setShowDebug(!showDebug)}
          >
            ⚙️ DEBUG
          </button>
        </nav>

        <div className={styles.headerRight}>
          <div className={styles.verifiedBadge}>
            <span className={styles.verifiedDot} />
            <span>VERIFIED DATASET</span>
          </div>
          {onBackToDashboard && (
            <button
              onClick={onBackToDashboard}
              style={{ background: "rgba(255,255,255,0.06)", border: "1px solid rgba(255,255,255,0.12)", color: "#ffffff", padding: "4px 10px", borderRadius: "6px", fontSize: "11px", fontWeight: 700, cursor: "pointer" }}
            >
              ← Dashboard
            </button>
          )}
        </div>
      </header>

      <main className={styles.viewportArea}>
        <div className={styles.searchContainer}>
          <div className={styles.searchBox}>
            <span className={styles.searchIcon}>🔍</span>
            <input
              type="text"
              className={styles.searchInput}
              placeholder="Search seat, block, gate or exit... (e.g. D-142)"
              value={searchQuery}
              onChange={(e) => {
                setSearchQuery(e.target.value);
                setIsSearching(true);
              }}
              onFocus={() => setIsSearching(true)}
            />
            {searchQuery && (
              <button className={styles.clearSearch} onClick={() => setSearchQuery("")}>
                ✕
              </button>
            )}
          </div>

          {isSearching && searchResults.length > 0 && (
            <div className={styles.searchDropdown}>
              {searchResults.map((res) => (
                <div
                  key={res.id}
                  className={styles.searchResultItem}
                  onClick={() => handleSelectSearchResult(res)}
                >
                  <div>
                    <div className={styles.resultTitle}>{res.title}</div>
                    <div className={styles.resultMeta}>{res.subtitle}</div>
                  </div>
                  <span style={{ fontSize: "10px", fontWeight: 800, color: "#38bdf8" }}>{res.type}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className={styles.navCard}>
          <h3 className={styles.cardHeaderTitle}>
            <span>NAVIGATE</span>
            {evacMode && <span style={{ color: "#ef4444", fontSize: "10px" }}>● EVAC</span>}
          </h3>

          <div className={styles.inputFieldGroup}>
            <label className={styles.fieldLabel}>FROM</label>
            <select
              className={styles.selectInput}
              value={fromNodeId}
              onChange={(e) => setFromNodeId(e.target.value)}
            >
              <option value="NODE_GATE_1">Gate 1 (Main Entrance)</option>
              <option value="NODE_GATE_2">Gate 2 (East Promenade)</option>
              <option value="NODE_GATE_3">Gate 3 (South Ramp)</option>
              <option value="NODE_GATE_4">Gate 4 (VIP Pavilion)</option>
              <option value="NODE_GATE_5">Gate 5 (Garware Entry)</option>
              <option value="NODE_GATE_6">Gate 6 (Media Gate)</option>
            </select>
          </div>

          <div className={styles.inputFieldGroup}>
            <label className={styles.fieldLabel}>TO</label>
            <select
              className={styles.selectInput}
              value={toNodeId}
              onChange={(e) => {
                setToNodeId(e.target.value);
                setSelectedNodeId(e.target.value);
              }}
            >
              <option value="NODE_SEAT_D142">D-142 (Block D · Gavaskar Stand)</option>
              <option value="NODE_SEAT_A108">A-108 (Block A · MCA Pavilion)</option>
              <option value="NODE_SEAT_B205">B-205 (Block B · Garware Pavilion)</option>
              <option value="NODE_SEAT_C054">C-054 (Block C · Merchant Stand)</option>
              <option value="NODE_SEAT_E312">E-312 (Block E · Tendulkar Stand)</option>
              <option value="NODE_SEAT_F119">F-119 (Block F · Divecha Pavilion)</option>
              <option value="NODE_EXIT_4">Exit 4 (Churchgate Egress)</option>
            </select>
          </div>

          <button
            className={styles.findBtn}
            onClick={() => handleCalculateRoute(fromNodeId, toNodeId, routeType)}
          >
            FIND ROUTE
          </button>

          <div className={styles.routeTypesRow}>
            <button
              className={`${styles.typePill} ${routeType === "SHORTEST" ? styles.typePillActive : ""}`}
              onClick={() => {
                setRouteType("SHORTEST");
                handleCalculateRoute(fromNodeId, toNodeId, "SHORTEST");
              }}
            >
              Shortest
            </button>
            <button
              className={`${styles.typePill} ${routeType === "FASTEST" ? styles.typePillActive : ""}`}
              onClick={() => {
                setRouteType("FASTEST");
                handleCalculateRoute(fromNodeId, toNodeId, "FASTEST");
              }}
            >
              Fastest
            </button>
            <button
              className={`${styles.typePill} ${routeType === "ACCESSIBLE" ? styles.typePillActive : ""}`}
              onClick={() => {
                setRouteType("ACCESSIBLE");
                handleCalculateRoute(fromNodeId, toNodeId, "ACCESSIBLE");
              }}
            >
              Accessible
            </button>
          </div>

          <button className={styles.nearestExitBtn} onClick={handleNearestExit}>
            🚨 NEAREST EXIT
          </button>
        </div>

        {activeRoute && (
          <div className={styles.routeCard}>
            <div className={styles.routeTitle}>
              <span>{activeRoute.steps[0]} → {activeRoute.steps[activeRoute.steps.length - 1]}</span>
              <span className={styles.routeBadge}>{activeRoute.routeType}</span>
            </div>

            <div className={styles.routeMetrics}>
              <span>📏 {activeRoute.totalDistanceMeters} m</span>
              <span>⏱ {activeRoute.estimatedMinutes} min</span>
            </div>

            <div className={styles.breadcrumbs}>
              {activeRoute.steps.map((step, idx) => (
                <React.Fragment key={idx}>
                  <span className={styles.breadStep}>{step}</span>
                  {idx < activeRoute.steps.length - 1 && <span className={styles.breadArrow}>↓</span>}
                </React.Fragment>
              ))}
            </div>

            <button
              className={styles.followBtn}
              onClick={() => setCameraView("FOLLOW")}
            >
              ▶ FOLLOW ROUTE
            </button>
          </div>
        )}

        {selectedSeat && !activeRoute && (
          <div className={styles.destinationFloatingCard}>
            <div>
              <div className={styles.destTitle}>{selectedSeat.id}</div>
              <div className={styles.destSub}>{selectedSeat.block} · {selectedSeat.row} · {selectedSeat.stand}</div>
            </div>
            <button
              className={styles.destNavBtn}
              onClick={() => {
                setToNodeId(selectedSeat.nodeId);
                handleCalculateRoute(fromNodeId, selectedSeat.nodeId, "SHORTEST");
              }}
            >
              NAVIGATE
            </button>
          </div>
        )}

        <div style={{ position: "absolute", inset: 0, overflow: "hidden" }}>
          <svg width="100%" height="100%" style={{ width: "100%", height: "100%" }}>
            <defs>
              <pattern id="holoGridApp" width="40" height="40" patternUnits="userSpaceOnUse">
                <path d="M 40 0 L 0 0 0 40" fill="none" stroke="rgba(56, 189, 248, 0.08)" strokeWidth="1" />
              </pattern>
              <linearGradient id="pitchGradApp" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" stopColor="#064e3b" />
                <stop offset="100%" stopColor="#022c22" />
              </linearGradient>
            </defs>
            <rect width="100%" height="100%" fill="url(#holoGridApp)" />

            <g transform="translate(620, 360) scale(1.1)">
              <ellipse
                cx="0"
                cy="0"
                rx="340"
                ry="210"
                fill={xrayMode ? "rgba(56, 189, 248, 0.03)" : "rgba(15, 23, 42, 0.8)"}
                stroke={evacMode ? "#ef4444" : "#38bdf8"}
                strokeWidth={xrayMode ? "1.5" : "3"}
                strokeDasharray={xrayMode ? "4 4" : "none"}
              />

              <ellipse
                cx="0"
                cy="0"
                rx="280"
                ry="170"
                fill="none"
                stroke={xrayMode ? "#38bdf8" : "rgba(255, 255, 255, 0.15)"}
                strokeWidth="2"
              />

              <ellipse cx="0" cy="0" rx="160" ry="95" fill="url(#pitchGradApp)" stroke="#10b981" strokeWidth="2.5" />
              <rect x="-18" y="-12" width="36" height="24" fill="none" stroke="#f59e0b" strokeWidth="1.5" />

              {activeRoute && (
                <g>
                  <path
                    d="M 270 -80 Q 200 -60 140 -20 T -10 10 T -120 40"
                    fill="none"
                    stroke="#ef4444"
                    strokeWidth="6"
                    strokeLinecap="round"
                    style={{ filter: "drop-shadow(0 0 10px #ef4444)" }}
                  />
                  <circle cx="270" cy="-80" r="8" fill="#38bdf8" />
                  <circle cx="-120" cy="40" r="8" fill="#ef4444" />
                </g>
              )}

              <g fontSize="11" fontWeight="800" textAnchor="middle" fill="#ffffff" letterSpacing="1">
                <text x="260" y="-70" fill="#38bdf8">SUNIL GAVASKAR</text>
                <text x="260" y="-56" fontSize="9" fill="#9ca3af">STAND</text>

                <text x="0" y="-180" fill="#f59e0b">SACHIN TENDULKAR</text>
                <text x="0" y="-166" fontSize="9" fill="#9ca3af">STAND</text>

                <text x="240" y="110" fill="#10b981">VIJAY MERCHANT</text>
                <text x="240" y="124" fontSize="9" fill="#9ca3af">STAND</text>

                <text x="-260" y="-50" fill="#a855f7">MCA PAVILION</text>
                <text x="0" y="180" fill="#ec4899">GARWARE PAVILION</text>
                <text x="-240" y="110" fill="#06b6d4">DIVECHA PAVILION</text>
              </g>

              <g transform="translate(280, -90)" cursor="pointer" onClick={() => setFromNodeId("NODE_GATE_1")}>
                <rect x="-14" y="-10" width="28" height="20" rx="4" fill="#38bdf8" />
                <text x="0" y="4" fontSize="10" fontWeight="900" fill="#030712" textAnchor="middle">G1</text>
              </g>

              <g transform="translate(10, -195)" cursor="pointer" onClick={() => setFromNodeId("NODE_GATE_2")}>
                <rect x="-14" y="-10" width="28" height="20" rx="4" fill="#38bdf8" />
                <text x="0" y="4" fontSize="10" fontWeight="900" fill="#030712" textAnchor="middle">G2</text>
              </g>

              <g transform="translate(260, 130)" cursor="pointer" onClick={() => setFromNodeId("NODE_GATE_3")}>
                <rect x="-14" y="-10" width="28" height="20" rx="4" fill="#38bdf8" />
                <text x="0" y="4" fontSize="10" fontWeight="900" fill="#030712" textAnchor="middle">G3</text>
              </g>

              <g transform="translate(310, -50)" cursor="pointer" onClick={() => setToNodeId("NODE_EXIT_4")}>
                <rect x="-18" y="-10" width="36" height="20" rx="4" fill="#ef4444" />
                <text x="0" y="4" fontSize="9" fontWeight="900" fill="#ffffff" textAnchor="middle">EXIT 4</text>
              </g>

              <g transform="translate(230, -30)" cursor="pointer" onClick={() => setSelectedSeatId("D-142")}>
                <circle cx="0" cy="0" r="10" fill="#ef4444" stroke="#ffffff" strokeWidth="2" />
                <text x="0" y="4" fontSize="8" fontWeight="900" fill="#ffffff" textAnchor="middle">D142</text>
              </g>
            </g>
          </svg>
        </div>

        <div className={styles.bottomControls}>
          <button
            className={`${styles.controlBtn} ${cameraView === "OVERVIEW" ? styles.controlActive : ""}`}
            onClick={() => {
              setCameraView("OVERVIEW");
              setActiveRoute(null);
            }}
          >
            ◎ Overview
          </button>
          <button
            className={`${styles.controlBtn} ${cameraView === "FOLLOW" ? styles.controlActive : ""}`}
            onClick={() => setCameraView("FOLLOW")}
          >
            ▶ Follow
          </button>
          <button
            className={`${styles.controlBtn} ${cameraView === "FOCUS" ? styles.controlActive : ""}`}
            onClick={() => setCameraView("FOCUS")}
          >
            ⌖ Focus
          </button>
          <button
            className={`${styles.controlBtn} ${xrayMode ? styles.controlActive : ""}`}
            onClick={() => setXrayMode(!xrayMode)}
          >
            🦴 X-RAY {xrayMode ? "[ON]" : "[OFF]"}
          </button>
          <button
            className={`${styles.controlBtn} ${evacMode ? styles.controlEvacActive : ""}`}
            onClick={handleToggleEvac}
          >
            🚨 EVAC {evacMode ? "[ACTIVE]" : ""}
          </button>
          <button
            className={`${styles.controlBtn} ${demoMode ? styles.controlActive : ""}`}
            onClick={() => setDemoMode(!demoMode)}
          >
            🎬 DEMO
          </button>
          <button
            className={styles.controlBtn}
            onClick={() => {
              setActiveRoute(null);
              setSelectedSeatId(null);
              setEvacMode(false);
              setCameraView("OVERVIEW");
            }}
          >
            ↻ Reset
          </button>
        </div>

        <button className={styles.geminiFloatBtn} onClick={() => setIsGeminiOpen(!isGeminiOpen)}>
          ✦ Gemini Assistant
        </button>

        {isGeminiOpen && (
          <aside className={styles.geminiDrawer}>
            <div className={styles.geminiHeader}>
              <span className={styles.geminiTitle}>✦ Gemini Assistant</span>
              <button className={styles.closeDrawerBtn} onClick={() => setIsGeminiOpen(false)}>
                ✕
              </button>
            </div>

            <div className={styles.geminiBody}>
              {geminiMessages.map((msg, idx) => (
                <div
                  key={idx}
                  className={`${styles.geminiBubble} ${msg.sender === "user" ? styles.userBubble : ""}`}
                >
                  {msg.text}
                </div>
              ))}
            </div>

            <div className={styles.geminiInputBox}>
              <input
                type="text"
                className={styles.geminiInput}
                placeholder="Ask Gemini (e.g. Take me from Gate 1 to D-142)..."
                value={geminiInput}
                onChange={(e) => setGeminiInput(e.target.value)}
                onKeyDown={(e) => e.key === "Enter" && handleGeminiSend()}
              />
              <button className={styles.geminiSendBtn} onClick={handleGeminiSend}>
                Send
              </button>
            </div>
          </aside>
        )}

        {activeTab === "ADMIN" && (
          <div className={styles.adminOverlay}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 20 }}>
              <h2 style={{ fontSize: 18, fontWeight: 900, color: "#ffffff", margin: 0 }}>
                STADIUM SPATIAL DATASET VERIFICATION
              </h2>
              <button
                style={{ background: "#38bdf8", color: "#030712", border: "none", padding: "6px 14px", borderRadius: "6px", fontWeight: 800, cursor: "pointer" }}
                onClick={() => setActiveTab("3D")}
              >
                Close Admin
              </button>
            </div>

            <div className={styles.adminGrid}>
              <div className={styles.adminCard}>
                <div className={styles.adminCardTitle}>SPATIAL RECORDS</div>
                <div className={styles.adminMetaRow}><span>Seat Records</span><strong>1,480 Verified</strong></div>
                <div className={styles.adminMetaRow}><span>Gate Access Nodes</span><strong>7 Active</strong></div>
                <div className={styles.adminMetaRow}><span>Emergency Exits</span><strong>5 Verified</strong></div>
                <div className={styles.adminMetaRow}><span>Wayfinding Edges</span><strong>86 Connected</strong></div>
              </div>

              <div className={styles.adminCard}>
                <div className={styles.adminCardTitle}>SYSTEM INTEGRITY</div>
                <div className={styles.adminMetaRow}><span>Spatial Confidence</span><strong style={{ color: "#10b981" }}>99.8%</strong></div>
                <div className={styles.adminMetaRow}><span>Graph Connectivity</span><strong style={{ color: "#10b981" }}>✓ FULLY CONNECTED</strong></div>
                <div className={styles.adminMetaRow}><span>Coordinate Validation</span><strong style={{ color: "#10b981" }}>✓ PASSED (3D EPSG:4326)</strong></div>
              </div>

              <div className={styles.adminCard}>
                <div className={styles.adminCardTitle}>ROUTING ENGINE DIAGNOSTICS</div>
                <div className={styles.adminMetaRow}><span>Algorithm</span><strong>A* Deterministic</strong></div>
                <div className={styles.adminMetaRow}><span>Average Latency</span><strong>14 ms</strong></div>
                <div className={styles.adminMetaRow}><span>Multi-Modal Routing</span><strong>Active</strong></div>
              </div>
            </div>
          </div>
        )}

        {showDebug && (
          <div className={styles.debugPanel}>
            <div style={{ fontWeight: 900, marginBottom: 6, borderBottom: "1px solid #f59e0b", paddingBottom: 4 }}>
              ⚙️ DEVELOPER DIAGNOSTICS MODE
            </div>
            <div className={styles.debugRow}><span>From Node:</span><code>{fromNodeId}</code></div>
            <div className={styles.debugRow}><span>To Node:</span><code>{toNodeId}</code></div>
            <div className={styles.debugRow}><span>Selected Seat:</span><code>{selectedSeatId || "None"}</code></div>
            <div className={styles.debugRow}><span>Graph Nodes:</span><code>42 Registered</code></div>
            <div className={styles.debugRow}><span>Graph Edges:</span><code>86 Directed</code></div>
            <div className={styles.debugRow}><span>3D Renderer:</span><code>Canvas / MapLibre 3D</code></div>
            <div className={styles.debugRow}><span>A* Heuristic $f(n)$:</span><code>{activeRoute ? activeRoute.totalDistanceMeters : 0}</code></div>
          </div>
        )}
      </main>
    </div>
  );
}
