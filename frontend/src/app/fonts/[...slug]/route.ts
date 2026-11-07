import { NextRequest, NextResponse } from "next/server";
import fs from "fs";
import path from "path";

/**
 * Robust Glyph Fallback Route Handler
 *
 * Serves font glyph PBF files from local public assets, or fetches valid protobuf
 * glyphs on-demand from the OpenFreeMap font server and caches them locally.
 * Prevents HTML 404 responses or protobuf wire-type decoding failures.
 */
export async function GET(
  request: NextRequest,
  context: { params: Promise<{ slug: string[] }> }
) {
  const { slug } = await context.params;
  if (!slug || slug.length < 2) {
    return new NextResponse("Invalid font request", { status: 400 });
  }

  // slug example: ['Noto Sans Regular', '0-255.pbf']
  const fontstack = decodeURIComponent(slug.slice(0, -1).join("/"));
  const range = slug[slug.length - 1];

  // 1. Check local public disk
  const localDir = path.join(process.cwd(), "public", "fonts", fontstack);
  const localPath = path.join(localDir, range);

  if (fs.existsSync(localPath)) {
    const buffer = fs.readFileSync(localPath);
    return new NextResponse(buffer, {
      status: 200,
      headers: {
        "Content-Type": "application/x-protobuf",
        "Cache-Control": "public, max-age=31536000, immutable",
      },
    });
  }

  // 2. Fetch on-demand from OpenFreeMap font server (protobuf source)
  try {
    const upstreamUrl = `https://tiles.openfreemap.org/fonts/${encodeURIComponent(fontstack)}/${range}`;
    const upstreamRes = await fetch(upstreamUrl, {
      headers: { Accept: "application/x-protobuf" },
    });

    if (upstreamRes.ok) {
      const arrayBuffer = await upstreamRes.arrayBuffer();
      const buffer = Buffer.from(arrayBuffer);

      // Cache locally to avoid future network round-trips
      try {
        fs.mkdirSync(localDir, { recursive: true });
        fs.writeFileSync(localPath, buffer);
      } catch (err) {
        console.warn("[Glyphs] Could not write cache to disk:", err);
      }

      return new NextResponse(buffer, {
        status: 200,
        headers: {
          "Content-Type": "application/x-protobuf",
          "Cache-Control": "public, max-age=31536000, immutable",
        },
      });
    }

    return new NextResponse("Glyph range not found", { status: 404 });
  } catch (err) {
    console.error("[Glyphs] Upstream fetch error:", err);
    return new NextResponse("Internal font error", { status: 502 });
  }
}

