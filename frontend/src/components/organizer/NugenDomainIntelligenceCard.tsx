"use client";

import React from "react";
import { NugenOperationalIntelligence } from "@/services/nugenService";
import { BrainCircuit, Layers, ShieldCheck, Zap, RefreshCw } from "lucide-react";

interface NugenDomainIntelligenceCardProps {
  intelligence: NugenOperationalIntelligence | null;
  isLoading?: boolean;
  onRefresh?: () => void;
}

export default function NugenDomainIntelligenceCard({
  intelligence,
  isLoading = false,
  onRefresh,
}: NugenDomainIntelligenceCardProps) {
  if (isLoading) {
    return (
      <div
        style={{
          background: "linear-gradient(135deg, rgba(15, 23, 42, 0.96) 0%, rgba(30, 41, 59, 0.98) 100%)",
          border: "1px solid rgba(56, 189, 248, 0.25)",
          borderRadius: "var(--radius-md)",
          padding: "20px 24px",
          color: "#f8fafc",
          display: "flex",
          alignItems: "center",
          gap: "12px",
          fontSize: "13px",
        }}
      >
        <Zap className="spin" size={18} style={{ color: "#38bdf8" }} />
        <span>Evaluating live operational state with Nugen Domain-Aligned Model...</span>
      </div>
    );
  }

  const isLive = intelligence?.isAvailable ?? false;
  const modelId = intelligence?.modelId || "nugen-junction-crowd-v1";
  const baseModel = intelligence?.baseModelUsed || "meta-llama/Llama-3.1-8B-Instruct";

  return (
    <div
      style={{
        background: "linear-gradient(135deg, rgba(15, 23, 42, 0.96) 0%, rgba(30, 41, 59, 0.98) 100%)",
        border: "1px solid rgba(56, 189, 248, 0.25)",
        borderRadius: "var(--radius-md)",
        padding: "20px 24px",
        color: "#f8fafc",
        boxShadow: "0 10px 25px -5px rgba(0, 0, 0, 0.3)",
        position: "relative",
        overflow: "hidden",
      }}
    >
      {/* Background Accent Glow */}
      <div
        style={{
          position: "absolute",
          top: "-50px",
          right: "-50px",
          width: "200px",
          height: "200px",
          borderRadius: "50%",
          background: "radial-gradient(circle, rgba(56, 189, 248, 0.12) 0%, rgba(0, 0, 0, 0) 70%)",
          pointerEvents: "none",
        }}
      />

      {/* Header Bar */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          marginBottom: "16px",
          flexWrap: "wrap",
          gap: "10px",
        }}
      >
        <div style={{ display: "flex", alignItems: "center", gap: "10px" }}>
          <div
            style={{
              width: "32px",
              height: "32px",
              borderRadius: "8px",
              background: "rgba(56, 189, 248, 0.15)",
              border: "1px solid rgba(56, 189, 248, 0.3)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              color: "#38bdf8",
            }}
          >
            <BrainCircuit size={18} />
          </div>
          <div>
            <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
              <span
                style={{
                  fontFamily: "var(--font-display)",
                  fontSize: "14px",
                  fontWeight: 700,
                  letterSpacing: "0.08em",
                  textTransform: "uppercase",
                  color: "#f8fafc",
                }}
              >
                Nugen Domain Intelligence
              </span>
              <span
                style={{
                  fontSize: "10px",
                  fontFamily: "var(--font-mono)",
                  padding: "2px 8px",
                  borderRadius: "12px",
                  background: isLive ? "rgba(34, 197, 94, 0.2)" : "rgba(245, 158, 11, 0.2)",
                  color: isLive ? "#4ade80" : "#fbbf24",
                  border: isLive ? "1px solid rgba(34, 197, 94, 0.4)" : "1px solid rgba(245, 158, 11, 0.4)",
                  fontWeight: 600,
                }}
              >
                {isLive ? "● ALIGNED MODEL LIVE" : "⚙ NUGEN ALIGNMENT PIPELINE"}
              </span>
            </div>
            <span style={{ fontSize: "11px", color: "#94a3b8" }}>
              Task 2 Additive Domain Model · Mega-Event Operational Interpretation
            </span>
          </div>
        </div>

        {onRefresh && (
          <button
            type="button"
            onClick={onRefresh}
            disabled={isLoading}
            style={{
              background: isLoading ? "rgba(56, 189, 248, 0.2)" : "rgba(255, 255, 255, 0.08)",
              border: "1px solid rgba(255, 255, 255, 0.15)",
              color: "#e2e8f0",
              fontSize: "11px",
              fontWeight: 600,
              padding: "6px 12px",
              borderRadius: "6px",
              cursor: isLoading ? "not-allowed" : "pointer",
              transition: "all 0.2s ease",
              display: "flex",
              alignItems: "center",
              gap: "6px",
            }}
          >
            <RefreshCw size={12} className={isLoading ? "spin" : ""} />
            {isLoading ? "EVALUATING NUGEN..." : "RE-EVALUATE NUGEN"}
          </button>
        )}
      </div>

      {/* Model Alignment Pipeline Provenance Bar */}
      <div
        style={{
          background: "rgba(15, 23, 42, 0.6)",
          border: "1px solid rgba(255, 255, 255, 0.08)",
          borderRadius: "6px",
          padding: "8px 12px",
          fontSize: "11px",
          color: "#cbd5e1",
          display: "flex",
          alignItems: "center",
          gap: "8px",
          marginBottom: "16px",
          flexWrap: "wrap",
        }}
      >
        <span style={{ color: "#94a3b8", fontWeight: 600 }}>ALIGNMENT PIPELINE:</span>
        <span style={{ color: "#38bdf8", fontFamily: "var(--font-mono)" }}>{baseModel}</span>
        <span style={{ color: "#64748b" }}>→</span>
        <span style={{ color: "#f472b6", fontWeight: 600 }}>Nugen Alignment</span>
        <span style={{ color: "#64748b" }}>→</span>
        <span style={{ color: "#4ade80", fontFamily: "var(--font-mono)", fontWeight: 700 }}>
          {modelId}
        </span>
      </div>

      {/* Content Section: Live Output vs Status Message */}
      {isLive && intelligence ? (
        <div style={{ display: "flex", flexDirection: "column", gap: "14px" }}>
          {/* Primary Pressure */}
          <div
            style={{
              padding: "12px 16px",
              background: "rgba(239, 68, 68, 0.1)",
              borderLeft: "4px solid #ef4444",
              borderRadius: "4px",
            }}
          >
            <span
              style={{
                fontSize: "10px",
                fontWeight: 700,
                letterSpacing: "0.1em",
                textTransform: "uppercase",
                color: "#fca5a5",
                display: "block",
                marginBottom: "4px",
              }}
            >
              Primary Operational Pressure
            </span>
            <p style={{ fontSize: "14px", fontWeight: 700, color: "#ffffff", margin: 0 }}>
              {intelligence.primaryPressure}
            </p>
          </div>

          {/* Connected Impact Chain */}
          {intelligence.affectedEntities && intelligence.affectedEntities.length > 0 && (
            <div>
              <span
                style={{
                  fontSize: "10px",
                  fontWeight: 700,
                  letterSpacing: "0.1em",
                  textTransform: "uppercase",
                  color: "#94a3b8",
                  display: "block",
                  marginBottom: "6px",
                }}
              >
                Connected Affected Entities
              </span>
              <div style={{ display: "flex", alignItems: "center", gap: "8px", flexWrap: "wrap" }}>
                {intelligence.affectedEntities.map((entity, i) => (
                  <React.Fragment key={entity}>
                    <span
                      style={{
                        padding: "4px 10px",
                        background: "rgba(56, 189, 248, 0.12)",
                        border: "1px solid rgba(56, 189, 248, 0.25)",
                        borderRadius: "4px",
                        fontSize: "12px",
                        fontWeight: 600,
                        color: "#7dd3fc",
                      }}
                    >
                      {entity}
                    </span>
                    {i < (intelligence.affectedEntities?.length || 0) - 1 && (
                      <span style={{ fontSize: "12px", color: "#64748b" }}>→</span>
                    )}
                  </React.Fragment>
                ))}
              </div>
            </div>
          )}

          {/* Operational Relationship */}
          {intelligence.operationalRelationship && (
            <div style={{ fontSize: "12.5px", color: "#e2e8f0", lineHeight: "1.5" }}>
              <strong style={{ color: "#38bdf8" }}>Downstream Operational Relationship: </strong>
              {intelligence.operationalRelationship}
            </div>
          )}

          {/* Domain Interpretation */}
          {intelligence.domainInterpretation && (
            <div
              style={{
                padding: "12px 14px",
                background: "rgba(255, 255, 255, 0.04)",
                border: "1px solid rgba(255, 255, 255, 0.08)",
                borderRadius: "6px",
                fontSize: "12.5px",
                color: "#cbd5e1",
                lineHeight: "1.5",
              }}
            >
              <strong style={{ color: "#f472b6" }}>Nugen Domain Interpretation: </strong>
              {intelligence.domainInterpretation}
            </div>
          )}

          {/* Recommended Operational Context */}
          {intelligence.recommendedOperationalContext && (
            <div
              style={{
                padding: "12px 14px",
                background: "rgba(34, 197, 94, 0.08)",
                border: "1px solid rgba(34, 197, 94, 0.2)",
                borderRadius: "6px",
                fontSize: "12.5px",
                color: "#86efac",
                lineHeight: "1.5",
              }}
            >
              <strong>Domain Response Guidance: </strong>
              {intelligence.recommendedOperationalContext}
            </div>
          )}
        </div>
      ) : null}

      {/* Footer Isolation Disclaimer */}
      <div
        style={{
          marginTop: "16px",
          paddingTop: "10px",
          borderTop: "1px solid rgba(255, 255, 255, 0.08)",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          fontSize: "10px",
          color: "#64748b",
        }}
      >
        <span style={{ display: "flex", alignItems: "center", gap: "4px" }}>
          <ShieldCheck size={12} style={{ color: "#38bdf8" }} />
          Isolated Additive Architecture: Existing JUNCTION AI path frozen & unaffected.
        </span>
        <span style={{ fontFamily: "var(--font-mono)" }}>
          TASK 2 · HACKCELESTIAL 3.0
        </span>
      </div>
    </div>
  );
}
