// ============================================================
// JUNCTION - API Route for Nugen Domain Intelligence (Task 2)
// ============================================================

import { NextRequest, NextResponse } from "next/server";
import { fetchNugenDomainIntelligence, NugenStateInput } from "@/services/nugenService";

export const dynamic = "force-dynamic";

export async function POST(req: NextRequest) {
  try {
    let body: NugenStateInput = {};
    try {
      body = await req.json();
    } catch {
      // Allow empty payload to evaluate baseline nominal state
      body = {};
    }

    if (!process.env.NUGEN_API_KEY) {
      process.env.NUGEN_API_KEY = "nugen-acb84a05a4567887";
    }

    const intelligence = await fetchNugenDomainIntelligence(body);

    return NextResponse.json({
      success: true,
      intelligence,
    });
  } catch (error: any) {
    console.error("[API /api/nugen/intelligence] Handler error:", error.message);
    return NextResponse.json(
      {
        success: false,
        error: error.message || "Failed to process Nugen intelligence request.",
      },
      { status: 500 }
    );
  }
}
