// ============================================================
// JUNCTION - Task 2 Nugen Intelligence Automated Test Suite
// ============================================================

import fs from "fs";
import path from "path";
import { fetchNugenDomainIntelligence, NugenStateInput } from "@/services/nugenService";

async function runNugenTask2Tests() {
  console.log("=======================================================");
  console.log(" 🧪 HACKCELESTIAL 3.0 — TASK 2 NUGEN TEST SUITE");
  console.log("=======================================================");

  let passed = 0;
  let failed = 0;

  function assert(condition: boolean, description: string) {
    if (condition) {
      console.log(`  ✓ PASS: ${description}`);
      passed++;
    } else {
      console.log(`  ✗ FAIL: ${description}`);
      failed++;
    }
  }

  // --- 1. Dataset Verification ---
  console.log("\n--- 1. Nugen Domain Alignment Dataset Integrity ---");
  const datasetJsonPath = path.join(process.cwd(), "docs", "NUGEN_ALIGNMENT_DATASET.json");
  const datasetJsonlPath = path.join(process.cwd(), "docs", "NUGEN_ALIGNMENT_DATASET.jsonl");

  assert(fs.existsSync(datasetJsonPath), "NUGEN_ALIGNMENT_DATASET.json exists in docs/");
  assert(fs.existsSync(datasetJsonlPath), "NUGEN_ALIGNMENT_DATASET.jsonl exists in docs/");

  const rawJson = fs.readFileSync(datasetJsonPath, "utf-8");
  const dataset = JSON.parse(rawJson);

  assert(Array.isArray(dataset) && dataset.length === 6, "Dataset contains 6 mandatory operational scenarios");

  const scenarioTypes = new Set(dataset.map((d: any) => d.scenario_type));
  assert(scenarioTypes.has("GATE_PRESSURE"), "Dataset includes Scenario 1: GATE_PRESSURE");
  assert(scenarioTypes.has("UNEVEN_DISTRIBUTION"), "Dataset includes Scenario 2: UNEVEN_DISTRIBUTION");
  assert(scenarioTypes.has("TRANSPORT_PRESSURE"), "Dataset includes Scenario 3: TRANSPORT_PRESSURE");
  assert(scenarioTypes.has("HOSPITALITY_CAPACITY"), "Dataset includes Scenario 4: HOSPITALITY_CAPACITY");
  assert(scenarioTypes.has("WEATHER_IMPACT"), "Dataset includes Scenario 5: WEATHER_IMPACT");
  assert(scenarioTypes.has("SCHEDULE_SHIFT"), "Dataset includes Scenario 6: SCHEDULE_SHIFT");

  // --- 2. Isolation & Safe Fallback Execution ---
  console.log("\n--- 2. Nugen Service Isolation & Failure Safety ---");

  const sampleState: NugenStateInput = {
    activeScenario: "HEAVY_RAIN",
    simulationMinutes: 25,
    zones: [
      { id: "ZONE_WANKHEDE", name: "Wankhede Concourse", pressure: 94, status: "CRITICAL" },
      { id: "ZONE_CHURCHGATE", name: "Churchgate Transit Hub", pressure: 88, status: "HIGH" },
    ],
    resources: [
      { id: "GATE_1", name: "Wankhede Gate 1", pressure: 94 },
      { id: "GATE_2", name: "Wankhede Gate 2", pressure: 38 },
    ],
    hotspots: [
      { zoneId: "ZONE_WANKHEDE", name: "Wankhede Gate 1 Plaza", intensity: 0.94 },
    ],
    weather: {
      condition: "Heavy Monsoon Rain",
      tempC: 26,
      rainRateMmHr: 45,
      walkSpeedMps: 0.65,
    },
  };

  const result = await fetchNugenDomainIntelligence(sampleState);

  assert(typeof result === "object" && result !== null, "Nugen service returns valid intelligence result object");
  assert(typeof result.isAvailable === "boolean", "Result exposes clear boolean isAvailable status");
  assert(result.baseModelUsed === "meta-llama/Llama-3.1-8B-Instruct", "Identifies base model meta-llama/Llama-3.1-8B-Instruct");
  assert(typeof result.modelId === "string" && result.modelId.length > 0, "Identifies target domain-aligned model ID");
  assert(typeof result.statusMessage === "string", "Provides clear status diagnostic message");

  // --- 3. Non-Interference with Existing AI ---
  console.log("\n--- 3. Existing AI Path Non-Interference Verification ---");
  const existingAiRoutePath = path.join(process.cwd(), "frontend", "src", "app", "api", "recommendations", "plan", "route.ts");
  const routeContent = fs.readFileSync(existingAiRoutePath, "utf-8");

  assert(!routeContent.includes("nugenService"), "Existing AI route /api/recommendations/plan does NOT import nugenService");
  assert(!routeContent.includes("NUGEN_API_KEY"), "Existing AI route /api/recommendations/plan does NOT depend on NUGEN_API_KEY");
  assert(routeContent.includes("GoogleGenAI"), "Existing AI route preserves original GoogleGenAI implementation intact");

  console.log("\n=======================================================");
  console.log(` SUMMARY: ${passed} PASSED, ${failed} FAILED`);
  console.log("=======================================================");

  if (failed > 0) {
    process.exit(1);
  }
}

runNugenTask2Tests().catch((err) => {
  console.error("Test execution error:", err);
  process.exit(1);
});
