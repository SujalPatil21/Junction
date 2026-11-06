import type { NextConfig } from "next";

const SPRING_BOOT_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${SPRING_BOOT_URL}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
