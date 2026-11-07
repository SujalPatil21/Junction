# JUNCTION — Shared Assets & Cross-Platform Resources

This directory contains canonical media, video datasets, and shared fixtures utilized across multiple subsystems of the **JUNCTION Intelligent Destination Orchestration Platform**.

## Directory Structure

```text
shared/
└── assets/
    └── test-videos/
        ├── 15546948_1080_1920_50fps.mp4      # High-density gate ingress feed
        ├── 30744-382777820.mp4               # Curbside taxi/rideshare transit bay feed
        ├── 6387-191695740_medium.mp4         # Concourse pedestrian clearance feed
        ├── demo_cctv.mp4                     # Synthetic benchmark calibration feed
        └── venue_entrance_validation.mp4     # Venue turnstile ingress validation feed
```

## Consumption Model

1. **Python Computer Vision Bridge (`cv_bridge`)**:
   Reads video files directly from `shared/assets/test-videos/` during edge inference (e.g., `--video shared/assets/test-videos/demo_cctv.mp4`). The CV bridge is completely independent of the frontend directory.

2. **Presentation Frontend (`frontend`)**:
   At build time (`npm run build` or `npm run prebuild`), the synchronization script (`frontend/scripts/copy-demo-videos.js`) copies the required demonstration video assets into `frontend/public/videos/` for web playback.

