"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useApp } from "@/state/AppContext";
import styles from "./organizer.module.css";
import { SCENARIOS } from "@/data/mockScenarios";
import { ScenarioId } from "@/types";
import { useState, useEffect } from "react";
import { useUser, useAuth, UserButton } from "@clerk/nextjs";
import {
  LayoutDashboard,
  Map as MapIcon,
  Video,
  Building2,
  TrendingUp,
  Zap,
  Sliders,
  Calendar,
  Box,
  ArrowLeftRight,
} from "lucide-react";

const NAV = [
  {
    group: "COMMAND",
    items: [
      { href: "/organizer", label: "Operations Room", icon: <LayoutDashboard size={16} strokeWidth={2} /> },
      { href: "/organizer/select", label: "Scope Selector", icon: <ArrowLeftRight size={16} strokeWidth={2} /> },
    ],
  },
  {
    group: "DESTINATION",
    items: [
      { href: "/organizer/map", label: "Live Command Map", icon: <MapIcon size={16} strokeWidth={2} /> },
      { href: "/organizer/stadium", label: "Stadium Digital Twin", icon: <Box size={16} strokeWidth={2} /> },
      { href: "/organizer/cctv-demo", label: "CCTV Telemetry Lab", icon: <Video size={16} strokeWidth={2} /> },
      { href: "/organizer/capacity", label: "Capacity & Hotels", icon: <Building2 size={16} strokeWidth={2} /> },
      { href: "/organizer/predictions", label: "Predictions & Cascade", icon: <TrendingUp size={16} strokeWidth={2} /> },
    ],
  },
  {
    group: "DECISIONS",
    items: [
      { href: "/organizer/recommendations", label: "Action Recommendations", icon: <Zap size={16} strokeWidth={2} /> },
      { href: "/organizer/simulation", label: "What-If Simulator", icon: <Sliders size={16} strokeWidth={2} /> },
    ],
  },
  {
    group: "EVENT",
    items: [
      { href: "/organizer/event", label: "Event Gate Info", icon: <Calendar size={16} strokeWidth={2} /> },
    ],
  },
];

export default function OrganizerLayout({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const pathname = usePathname();
  const { activeScenario, setScenario, actionStatus, clearActionStatus } = useApp();
  const { user, isLoaded, isSignedIn } = useUser();
  const { getToken } = useAuth();
  const [dbProfile, setDbProfile] = useState<any>(null);
  const [isCollapsed, setIsCollapsed] = useState(false);

  const isDevPreview = pathname === "/organizer/stadium-preview";
  const isStandaloneRoute =
    pathname === "/organizer/select" ||
    pathname === "/organizer/stadium" ||
    pathname === "/organizer/stadium-preview";

  // Auth protection guard
  useEffect(() => {
    if (!isDevPreview && isLoaded && !isSignedIn) {
      router.replace(`/login?redirect=${encodeURIComponent(pathname)}`);
    }
  }, [isLoaded, isSignedIn, router, pathname, isDevPreview]);

  // Fetch authoritative authenticated profile from Spring Boot on port 8080
  useEffect(() => {
    let isMounted = true;
    async function loadPostgresProfile() {
      try {
        const token = await getToken();
        if (token) {
          const res = await fetch("http://localhost:8080/api/auth/me", {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          });
          if (res.ok) {
            const data = await res.json();
            if (isMounted && data.profile) {
              setDbProfile(data.profile);
            }
          }
        }
      } catch (err) {
        console.error("Failed to load authenticated profile from Spring Boot:", err);
      }
    }

    if (isLoaded && isSignedIn) {
      loadPostgresProfile();
    }
    return () => { isMounted = false; };
  }, [isLoaded, isSignedIn, getToken]);

  if (!isDevPreview && (!isLoaded || !isSignedIn)) {
    return (
      <div style={{ minHeight: "100vh", display: "flex", alignItems: "center", justifyContent: "center", background: "#111111" }}>
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 12 }}>
          <span className="pill pill-yellow">VERIFYING COMMAND SESSION</span>
          <p style={{ fontSize: 13, color: "#999999", fontFamily: "var(--font-display)" }}>
            Connecting to City Operations Command...
          </p>
        </div>
      </div>
    );
  }

  // Standalone full-viewport mode for /organizer/select, /organizer/stadium, and /organizer/stadium-preview
  if (isStandaloneRoute) {
    return <>{children}</>;
  }

  const userDisplayName = dbProfile?.name || user?.fullName || user?.primaryEmailAddress?.emailAddress || "City Operations Command";
  const userRole = dbProfile?.role || "ORGANIZER";

  return (
    <div className={styles.shell}>
      {/* SIDEBAR */}
      <aside className={`${styles.sidebar} ${isCollapsed ? styles.sidebarCollapsed : ""}`}>
        <div className={styles.sidebarHeader}>
          <Link href="/" className={styles.sidebarLogo}>
            {isCollapsed ? "J" : "JUNCTION"}
          </Link>
          <button
            type="button"
            className={styles.collapseBtn}
            onClick={() => setIsCollapsed(!isCollapsed)}
            title={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
            aria-label="Toggle sidebar"
          >
            {isCollapsed ? "»" : "«"}
          </button>
        </div>

        {/* Switch Operations Action */}
        <div style={{ padding: isCollapsed ? "8px 6px" : "8px 12px", borderBottom: "1px solid var(--border-subtle, rgba(255,255,255,0.1))" }}>
          <Link
            href="/organizer/select"
            title="Return to Scope Selection"
            style={{
              display: "flex",
              alignItems: "center",
              justifyContent: isCollapsed ? "center" : "flex-start",
              gap: "8px",
              padding: "7px 10px",
              background: "rgba(255, 214, 0, 0.08)",
              border: "1px solid rgba(255, 214, 0, 0.25)",
              borderRadius: "6px",
              color: "#ffd600",
              fontSize: "11px",
              fontWeight: 700,
              textDecoration: "none",
              letterSpacing: "0.04em",
              transition: "all 0.15s ease",
            }}
          >
            <ArrowLeftRight size={13} strokeWidth={2.5} />
            {!isCollapsed && <span>SWITCH OPERATIONS</span>}
          </Link>
        </div>

        <nav className={styles.sidebarNav}>
          {NAV.map(group => (
            <div key={group.group} className={styles.navGroup}>
              {!isCollapsed && <span className={styles.navGroupLabel}>{group.group}</span>}
              {group.items.map(item => {
                const isActive = item.href === "/organizer"
                  ? pathname === "/organizer"
                  : pathname.startsWith(item.href);
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    className={`${styles.navItem} ${isActive ? styles.navItemActive : ""} ${isCollapsed ? styles.navItemCollapsed : ""}`}
                    title={isCollapsed ? item.label : undefined}
                  >
                    <span className={styles.navIcon}>{item.icon}</span>
                    {!isCollapsed && <span className={styles.navLabel}>{item.label}</span>}
                  </Link>
                );
              })}
            </div>
          ))}
        </nav>

        <div className={`${styles.sidebarFooter} ${isCollapsed ? styles.footerCollapsedWrap : ""}`}>
          {!isCollapsed ? (
            <>
              <div className={styles.scenarioSelector}>
                <span className={styles.scenarioLabel}>ACTIVE SCENARIO</span>
                <select
                  className={styles.scenarioSelect}
                  value={activeScenario}
                  onChange={e => setScenario(e.target.value as ScenarioId)}
                >
                  {Object.values(SCENARIOS).map(s => (
                    <option key={s.id} value={s.id}>{s.label}</option>
                  ))}
                </select>
              </div>
              <div className={styles.simulatedEnv}>
                <span className="simulated-dot" />
                <span>SIMULATED ENVIRONMENT</span>
              </div>
            </>
          ) : (
            <div className={styles.footerCollapsed} title={`Scenario: ${SCENARIOS[activeScenario]?.label || activeScenario} (Simulated)`}>
              <span className="simulated-dot" />
              <span className={styles.collapsedScenarioTag}>{activeScenario.slice(0, 3)}</span>
            </div>
          )}
        </div>
      </aside>

      {/* MAIN COMMAND AREA */}
      <div className={styles.main}>
        {/* HEADER */}
        <header className={styles.header}>
          <div className={styles.headerLeft}>
            <div className={styles.headerEvent}>
              <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                <span className={styles.commandTag}>CITY OPERATIONS COMMAND</span>
                <span className={styles.headerEventName}>Mumbai Indians vs Delhi Capitals</span>
              </div>
              <span className={styles.headerEventMeta}>Wankhede Stadium · 33,000 expected attendees</span>
            </div>
          </div>

          <div className={styles.headerRight}>
            <div className={styles.headerBadges}>
              <span className="pill pill-simulated" style={{ display: "inline-flex", alignItems: "center", gap: 6, fontWeight: 700, padding: "4px 8px" }}>
                <span className="simulated-dot" /> SIMULATED TELEMETRY
              </span>
              <span className={styles.timeTag}>19:30–22:30</span>
            </div>

            <div className={styles.userProfile}>
              <span className={styles.userName}>
                {userDisplayName}
              </span>
              <span className={styles.userRoleTag}>
                {userRole}
              </span>
            </div>

            <UserButton
              appearance={{
                elements: {
                  userButtonAvatarBox: {
                    width: 32,
                    height: 32,
                    border: "2px solid #F5C400",
                    borderRadius: "4px",
                  },
                },
              }}
            />
          </div>
        </header>

        <div className={styles.content}>
          {actionStatus && (
            <div
              style={{
                position: "fixed",
                top: "72px",
                right: "24px",
                zIndex: 9999,
                display: "flex",
                alignItems: "center",
                gap: "12px",
                padding: "12px 18px",
                borderRadius: "6px",
                backgroundColor: actionStatus.type === "error" ? "#7f1d1d" : "#064e3b",
                color: actionStatus.type === "error" ? "#fca5a5" : "#6ee7b7",
                border: `1px solid ${actionStatus.type === "error" ? "#ef4444" : "#10b981"}`,
                boxShadow: "0 10px 25px -5px rgba(0, 0, 0, 0.5), 0 8px 10px -6px rgba(0, 0, 0, 0.3)",
                fontFamily: "var(--font-sans)",
                fontSize: "13px",
                fontWeight: 600,
                maxWidth: "520px",
                animation: "fadeInDown 0.2s cubic-bezier(0.16, 1, 0.3, 1)",
              }}
            >
              <span style={{ fontSize: "16px", lineHeight: 1 }}>
                {actionStatus.type === "error" ? "⚠️" : "✓"}
              </span>
              <span style={{ flex: 1, color: "#ffffff", lineHeight: 1.35 }}>
                {actionStatus.message}
              </span>
              <button
                onClick={clearActionStatus}
                style={{
                  background: "none",
                  border: "none",
                  color: "rgba(255, 255, 255, 0.7)",
                  cursor: "pointer",
                  fontSize: "16px",
                  fontWeight: 700,
                  padding: "2px 6px",
                  lineHeight: 1,
                }}
                title="Dismiss status notification"
              >
                ✕
              </button>
            </div>
          )}
          {children}
        </div>
      </div>
    </div>
  );
}
