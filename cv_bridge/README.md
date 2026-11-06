# JUNCTION — Computer Vision Bridge (`cv_bridge`)

Edge computer vision subsystem for real-time pedestrian and crowd flow analysis using **YOLOv12**, **ByteTrack**, and **OpenCV**.

## Subsystem Architecture & Responsibilities
- **Edge Inference**: Processes CCTV optical camera feeds and RTSP video streams locally.
- **Privacy by Design**: Zero facial recognition, zero biometric identification, zero identity tracking. Only spatial bounding boxes, crowd counts, tripwire direction vectors, and density values are computed.
- **Direct Spring Boot Ingestion**: Serializes normalized observation batches (`JUNCTION-NormalizedObservation-v1.0.0`) and pushes them directly to the Spring Boot backend (`http://localhost:8080/api/observations`).
- **Edge Authentication**: Transmits `Authorization: Bearer <JUNCTION_EDGE_DEVICE_SECRET>` in all ingestion requests.

## Directory Layout
```
cv_bridge/
├── models/                  # YOLOv12 weights (yolov12n.pt, yolov12m.pt)
├── tests/                   # Pytest test suite for metrics & serializers
├── config.example.json      # Sample camera and tripwire configuration
├── inference.py             # Main inference loop & ByteTrack tracker
├── metrics.py               # Spatial density and tripwire line intersection
├── observation_serializer.py# Transforms detection frames into canonical schemas
├── transport.py             # Multi-channel transport (HTTP, JSONL, stdout)
├── yolo_compat.py           # Compatibility patch for YOLOv12 architecture
├── requirements.txt         # Python dependencies
└── pytest.ini               # Test configuration
```

## Running Inference
```bash
# Set edge secret
export JUNCTION_EDGE_DEVICE_SECRET="your_shared_secret"

# Run inference on sample CCTV stream pointing to Spring Boot
python -m cv_bridge.inference \
  --video assets/test-videos/demo_cctv.mp4 \
  --zone-id ZONE_WANKHEDE \
  --camera-id DEV_CCTV_WANKHEDE_01 \
  --resource-id WANKHEDE_EXIT \
  --http-url http://localhost:8080/api/observations \
  --frame-stride 2
```

## Running Tests
```bash
pytest cv_bridge/tests -v
```
