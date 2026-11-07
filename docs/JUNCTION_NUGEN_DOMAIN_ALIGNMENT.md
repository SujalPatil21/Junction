# JUNCTION — NUGEN DOMAIN ALIGNMENT SPECIFICATION

> **Document Type:** Domain Alignment & Customization Specification for Nugen AI  
> **Source Project:** JUNCTION — Mega-Event Hospitality + Crowd + Mobility Orchestration Platform  
> **Scope:** Ground-Truth Domain Extraction for Fine-Tuning & Customizing Domain-Aligned Intelligence Models  

---

## 1. PROJECT DOMAIN

**JUNCTION** is a real-time operational orchestration platform built to solve high-density urban mobility, crowd pressure, and hospitality bottlenecks during mega-events in South Mumbai (centered around Wankhede Stadium with 33,000+ attendees).

### Core Problem & Domain Mechanics
When 33,000 attendees exit a venue simultaneously, traditional urban networks experience severe operational friction across three interdependent domains:
1. **Venue & Perimeter Egress Pressure:** Turnstiles, perimeter concourses, and egress chutes (`WANKHEDE_EXIT_GATE_1` & `WANKHEDE_EXIT_GATE_2`) become physical bottlenecks.
2. **Pedestrian & Transit Clearance:** Dispersing crowds converge rapidly on nearby rail terminals (`CHURCHGATE`, `MARINE_LINES`, `CSMT`) and vehicle pickup zones (`TAXI_ZONE`). Signal faults or line disruptions cause rapid station platform over-crowding.
3. **Hospitality & Urban Capacity:** Surge demand saturates adjacent accommodation (Zone A & B hotels) and food/beverage hubs (Colaba & Fort dining corridors), requiring crowd redistribution toward secondary sectors (Zone C / Dadar).

JUNCTION models the city as a connected topological network of **Operational Zones**, **Transportation Arterials**, and **Hospitality Resources**, providing human-in-the-loop operators with unified state awareness, multi-horizon pressure forecasting, and verified operational interventions.

---

## 2. JUNCTION ENTITIES & DOMAIN CATALOG

The following entity definitions, capacity constraints, and operational attributes represent the ground truth extracted directly from the JUNCTION repository (`src/data/mockResources.ts`, `src/services/zoneRegistry.ts`, `src/data/mockHotels.ts`, `src/data/mockRestaurants.ts`).

### A. Operational Venues & Exit Subzones

| Entity ID | Entity Name | Type | Tier / Monitoring | Capacity / Limits | Operational Role |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `WANKHEDE` | Wankhede Stadium & Perimeter Concourse | VENUE | Tier 1 Critical (Continuous) | 33,000 attendees (37,000 zone cap) | Primary mega-event venue bowl, turnstiles, and perimeter egress concourses. |
| `WANKHEDE_EXIT_GATE_1` | Wankhede Stadium — Exit Gate 1 | VENUE_SUBZONE | Tier 1 Critical (Continuous) | 2,200 flow cap (10,000 area cap) | North perimeter turnstile bank & egress chute directing crowds toward Marine Drive. |
| `WANKHEDE_EXIT_GATE_2` | Wankhede Stadium — Exit Gate 2 | VENUE_SUBZONE | Tier 1 Critical (Continuous) | 1,800 flow cap (8,000 area cap) | South perimeter turnstiles leading directly to Churchgate Station & Taxi Staging. |
| `WANKHEDE_EXIT` | Wankhede Concourse Exit Gates | VENUE_CONCOURSE | Tier 1 Critical (Continuous) | 4,000 combined flow cap | Combined stadium perimeter egress bottleneck. |

### B. Transportation Terminals & Staging Zones

| Entity ID | Entity Name | Type | Transit Connectivity | Capacity (Ped / Transit) | Operational Role |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `CHURCHGATE` | Churchgate Station | RAIL_TERMINAL | Western Railway | 10,000 ped / 18,000 transit | Primary Western Railway terminus & main exit clearance corridor for South Mumbai. |
| `MARINE_LINES` | Marine Lines Station | RAIL_STATION | Western Railway | 8,000 ped / 12,000 transit | Intermediate Western Railway station providing northern egress relief. |
| `CSMT` | Chhatrapati Shivaji Maharaj Terminus | RAIL_TERMINAL | Central & Harbour Railways | 14,000 ped / 25,000 transit | Central Railway terminus hub capturing eastern & suburban passenger dispersal. |
| `DADAR` | Dadar Transit Interchange | RAIL_INTERCHANGE | Western & Central Railways | 18,000 ped / 35,000 transit | Dual-railway interchange & northern bus feeder terminal for crowd redistribution. |
| `TAXI_ZONE` | South Stadium Taxi & Rideshare Zone | PICKUP_STAGING | Curbside Vehicles | 200 vehicle cap (800 ped / 1,200 transit) | Curbside vehicle pickup bays, cab queues, and rideshare dispatch staging. |

### C. Hospitality & Accommodation Corridor (Hotels)

| Hotel ID | Hotel Name | Zone Sector | Total Rooms | Available Rooms | Base Occupancy Pressure | Travel Time to Venue |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `H1` | Trident Nariman Point | Zone A (South Corridor) | 540 rooms | 12 rooms | 91% (CRITICAL) | 12 min |
| `H2` | Intercontinental Marine Drive | Zone A (South Corridor) | 410 rooms | 6 rooms | 88% (HIGH) | 15 min |
| `H3` | Hotel Marine Plaza | Zone B (Central Corridor) | 68 rooms | 9 rooms | 74% (WATCH) | 18 min |
| `H4` | Ramada by Wyndham Dadar | Zone C (North Corridor) | 250 rooms | 62 rooms | 52% (NORMAL) | 22 min |
| `H5` | Hotel Kohinoor Dadar | Zone C (North Corridor) | 180 rooms | 38 rooms | 48% (NORMAL) | 25 min |

### D. Dining & Beverage Corridor (Restaurants)

| Restaurant ID | Restaurant Name | Cuisine / Category | Capacity | Current Occupancy | Available Tables | Base Wait Time | Base Pressure | Zone Sector |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| `R1` | Trishna | Coastal Seafood & Mangalorean | 80 seats | 76 seats | 2 tables | 55 min (75 min peak) | 91% (CRITICAL) | Zone A (Fort) |
| `R2` | Bade Miya | Street Grill & Kebabs | 60 seats | 54 seats | 3 tables | 42 min (60 min peak) | 84% (HIGH) | Zone A (Colaba) |
| `R3` | Britannia & Co. | Parsi & Irani Heritage Café | 120 seats | 88 seats | 8 tables | 15 min (28 min peak) | 68% (WATCH) | Zone B (Ballard Estate) |
| `R4` | Café Madras | Authentic South Indian | 90 seats | 42 seats | 18 tables | 5 min (12 min peak) | 46% (NORMAL) | Zone C (Matunga) |
| `R5` | Shalimar Restaurant | Mughlai & Kebabs | 150 seats | 58 seats | 25 tables | 8 min (15 min peak) | 42% (NORMAL) | Zone C (Bhendi Bazaar) |

### E. Transportation Arterials & Corridors

| Road ID | Road Name | Primary Function | Base Capacity |
| :--- | :--- | :--- | :--- |
| `ROAD_MARINE_DR` | Marine Drive Promenade | Coastal arterial connecting Gate 1 to Marine Lines & Nariman Point. | 4,500 veh/hr |
| `ROAD_VEER_NARIMAN` | Veer Nariman Road | Direct corridor between Stadium Gate 2, Taxi Zone, and Churchgate. | 3,000 veh/hr |
| `ROAD_MAHARSHI_KARVE` | Maharshi Karve Road | North-South spine linking Churchgate to Marine Lines. | 3,500 veh/hr |
| `ROAD_DN_ROAD` | Dadabhai Naoroji Road | East-West linkage connecting Churchgate concourse to CSMT. | 2,800 veh/hr |
| `ROAD_CENTRAL_SPINE` | Central Arterial Spine | Highway corridor connecting South Mumbai concourses to Dadar Hub. | 5,000 veh/hr |

---

## 3. OPERATIONAL RELATIONSHIPS & NETWORK TOPOLOGY

JUNCTION operates on physical, spatial, and capacity relationships across interconnected urban nodes.

### Structural Egress Topology
```
                  ┌─────────────────────────────────────────┐
                  │       WANKHEDE STADIUM (33,000)         │
                  └────────────────────┬────────────────────┘
                                       │
                ┌──────────────────────┴──────────────────────┐
                ▼                                             ▼
     EXIT GATE 1 (North / 2,200)                   EXIT GATE 2 (South / 1,800)
                │                                             │
                ├──────────────────────────┐                  ├──────────────────────────┐
                ▼                          ▼                  ▼                          ▼
       Marine Drive Corridor     Marine Lines Station    Veer Nariman Road        Churchgate Terminal
        (ROAD_MARINE_DR)            (MARINE_LINES)       (ROAD_VEER_NARIMAN)          (CHURCHGATE)
                │                                             │                          │
                ▼                                             ▼                          ▼
      South Hospitality Corridor                    Taxi & Rideshare Staging       Western Railway Dispersal
       (Trident / Intercont.)                         (TAXI_ZONE / Cabs)              (Suburban Trains)
                                                                                         │
                                                                                         ▼
                                                                               Dadar Rail Interchange
                                                                                 (ZONE_DADAR / Relief)
```

### Key Operational Dependencies
1. **Gate Chute Capacity Coupling:** Gate 2 (1,800 capacity) connects directly to Churchgate (10,000 capacity). Excess Gate 2 throughput immediately floods Churchgate's pedestrian concourse.
2. **Modal Spillover:** When Churchgate Western Railway services suffer disruption, pedestrian overflow shifts directly into `TAXI_ZONE` (200 capacity) and `ROAD_VEER_NARIMAN`, creating severe localized gridlock.
3. **Hospitality Saturation Cascade:** High event demand fills Zone A hotels (91% pressure) and restaurants (55 min wait times). Unaccommodated demand shifts north to Zone C (Dadar) where 48–52% baseline pressure provides relief.

---

## 4. EXTRACTED OPERATIONAL SCENARIOS

The following 6 scenarios are extracted directly from the JUNCTION scenario engine (`src/data/mockScenarios.ts`).

### Scenario 1: NORMAL
* **Description:** Baseline event conditions — steady inflow, manageable pressure.
* **Operational State:** Match in progress; all transit services operational; clear weather.
* **Primary Pressure:** Nominal baseline (WANKHEDE 71%, CHURCHGATE 72%, GATE 2 68%, TAXI_ZONE 45%).
* **Affected Entities:** All core zones operating within nominal bounds.
* **Operational Relationship:** Inflow matches expected clearance rates; travel time multiplier at 1.0; hotel pressure multiplier 1.0; restaurant pressure multiplier 1.0.
* **Domain Interpretation:** Baseline equilibrium. Monitoring status remains at default levels (Tier 1 Continuous, Tier 2 Adaptive).
* **Operational Context:** Standard monitoring; no immediate manual intervention required.

### Scenario 2: POST_EVENT_SURGE
* **Description:** Match ended — 33,000 attendees exiting simultaneously.
* **Operational State:** Final whistle; simultaneous stadium bowl evacuation.
* **Primary Pressure:** Exit Gate 2 (94% -> 98% p15), Churchgate Terminal (94% -> 97% p15), Taxi Zone (91% -> 97% p15), Wankhede Stadium (95% -> 98% p15).
* **Affected Entities:** `WANKHEDE_EXIT_GATE_2`, `CHURCHGATE`, `TAXI_ZONE`, `ROAD_MARINE_DR`, Zone A Dining (`R1` Trishna 91%).
* **Operational Relationship:** Simultaneous egress overloads Gate 2 chutes; queue spills into Churchgate concourse; taxi demand surges (91%); hotel pressure multiplier 1.2; restaurant pressure multiplier 1.4; travel time multiplier 1.5.
* **Domain Interpretation:** Severe physical egress surge. Gate 2 reaches critical capacity; requires active turnstile flow metering and crowd steering toward Gate 1 and Marine Lines.
* **Operational Context:** Meter Gate 2 egress turnstiles; dispatch additional shuttle buses to Churchgate; activate variable message signs (VMS) directing crowd to Marine Lines.

### Scenario 3: TRANSPORT_DISRUPTION
* **Description:** Western Railway signal fault — services delayed 20 min.
* **Operational State:** Signal breakdown at Churchgate; Western Railway platform headways extended; transportStatus: CHURCHGATE DISRUPTED, CSMT REDUCED, DADAR OPERATIONAL.
* **Primary Pressure:** Churchgate Station (89% -> 94% p15 -> 98% p60 hold), Gate 2 (81% -> 91% p15 -> 98% p60), Taxi Zone (68% -> 97% p60).
* **Affected Entities:** `CHURCHGATE`, `TAXI_ZONE`, `WANKHEDE_EXIT_GATE_2`, `ROAD_VEER_NARIMAN`.
* **Operational Relationship:** Rail stoppage halts Churchgate platform clearance; pedestrian accumulation backs up through Gate 2 to stadium concourse; cab demand surges; travel time multiplier 1.8.
* **Domain Interpretation:** Severe transit bottleneck leading to physical backup at venue gates. Requires rapid intermodal shift to Central Railway (`CSMT`) and Northern Hub (`DADAR`).
* **Operational Context:** Open Mahapalika Marg evacuation corridor to CSMT; activate suburban shuttle fleet to Dadar Interchange; issue real-time passenger broadcast on venue screens.

### Scenario 4: HEAVY_RAIN
* **Description:** Heavy monsoon rain — reduced walking velocity, higher taxi demand.
* **Operational State:** Monsoon rainfall active; weatherCondition: HEAVY_RAIN; transportStatus: CHURCHGATE REDUCED.
* **Primary Pressure:** Taxi Zone (87% -> 94% p15 -> 97% p30), Exit Gate 2 (84% -> 91% p15 -> 96% p60), Churchgate Station (82% -> 88% p15 -> 96% p60).
* **Affected Entities:** `TAXI_ZONE`, `WANKHEDE_EXIT_GATE_2`, `CHURCHGATE`, `ROAD_MARINE_DR`, Zone A Restaurants (`R1` wait times elevated).
* **Operational Relationship:** Walking speed decreases (coefficient 0.007 per mm/h); crowd seeks shelter inside station concourses; taxi hailing surges (coefficient 0.016 per mm/h); hotel pressure multiplier 1.3; restaurant pressure multiplier 1.5; travel time multiplier 2.0.
* **Domain Interpretation:** Environmental friction compounding physical bottlenecks. Requires indoor platform queue management and curbside vehicle dispatch throttle.
* **Operational Context:** Open covered concourse holding areas; deploy rain-shuttle fleets; adjust signal timing on Veer Nariman Road to clear cab queueing.

### Scenario 5: ACCOMMODATION_SATURATION
* **Description:** Hotel zones A & B fully booked — redirect to Zone C.
* **Operational State:** Post-match night stay demand peak; hotelPressureMultiplier: 1.8.
* **Primary Pressure:** Trident Nariman Point (`H1` 91%), Intercontinental (`H2` 88%), Churchgate Station (78% -> 91% p30).
* **Affected Entities:** `H1`, `H2`, `R1`, `R2`, Zone A Hospitality Corridor.
* **Operational Relationship:** Localized accommodation saturation in South Mumbai; Zone A hotels operate near 90%+ capacity; Zone C hotels (`H4` 52%, `H5` 48%) remain in normal pressure range.
* **Domain Interpretation:** Geographic hospitality imbalance. Requires automated redirection of reservation traffic to Zone C (Dadar) where Ramada (52%) and Kohinoor (48%) maintain available capacity.
* **Operational Context:** Trigger hospitality partner inventory re-route; issue promotional incentives for Zone C restaurants (`R4` Café Madras); offer Dadar express shuttle passes.

### Scenario 6: EVENT_DELAY
* **Description:** Match delayed 30 minutes — arrival demand shifts later.
* **Operational State:** Match timing shifted; hotelPressureMultiplier 0.9; restaurantPressureMultiplier 0.8; travelTimeMultiplier 0.9.
* **Primary Pressure:** Temporal demand shift across all exit zones (WANKHEDE 45% -> 82% p60, CHURCHGATE 55% -> 91% p60, GATE 2 48% -> 90% p60).
* **Affected Entities:** All transit terminals and venue gates.
* **Operational Relationship:** Egress peak aligns with late-night transit service scaling; train frequency drops after 23:00, heightening late-stage pressure.
* **Domain Interpretation:** Temporal mismatch between crowd egress peak and fixed transit timetables.
* **Operational Context:** Request Western Railway to extend peak train frequency; delay taxi queue staging teardown.

---

## 5. ALIGNMENT EXAMPLES FOR NUGEN TRAINING

The following 8 ground-truth examples illustrate the input-to-reasoning mapping for Nugen domain alignment.

> **Note on Expected Domain Intelligence:** The "EXPECTED DOMAIN INTELLIGENCE" blocks describe what the Nugen-aligned model should learn to reason about. They represent qualitative domain reasoning targets rather than hardcoded deterministic code outputs.

```
================================================================================
EXAMPLE 1: Exit Gate 1 Overcrowding & Egress Steering
================================================================================
ACTUAL JUNCTION STATE:
- Scenario: POST_EVENT_SURGE
- Zone State: WANKHEDE_EXIT_GATE_1 pressure at 82% (Predicted 15m: 88%, 30m: 81%)
- Flow Capacity: Gate 1 flow limit at 2,200 ped/min (Area capacity: 10,000)
- Connected Corridors: Marine Drive (ROAD_MARINE_DR) pressure at 82% (Predicted 15m: 87%)

DOMAIN INTERPRETATION:
North perimeter exit chute (Gate 1) is operating under heavy surge pressure. 
Pedestrian density on Marine Drive promenade creates back-pressure toward North 
concourse turnstiles.

AFFECTED ENTITIES:
- Primary: WANKHEDE_EXIT_GATE_1, ROAD_MARINE_DR
- Downstream: MARINE_LINES, ZONE_HOTELS_SOUTH

OPERATIONAL RELATIONSHIP:
Gate 1 Surge (82%) -> Pedestrian Chute Load -> Marine Drive Congestion -> Marine Lines Platform Inflow

EXPECTED DOMAIN INTELLIGENCE (Target Reasoning Output):
Identify Gate 1 as a key North perimeter flow bottleneck. Recommend metering Gate 1 
turnstile throughput and activating directional signage to guide North-bound 
pedestrians toward Marine Lines Station to balance corridor load.
================================================================================
```

```
================================================================================
EXAMPLE 2: Exit Gate 2 Redistribution to South Taxi Zone
================================================================================
ACTUAL JUNCTION STATE:
- Scenario: POST_EVENT_SURGE
- Zone State: WANKHEDE_EXIT_GATE_2 pressure at 94% (Predicted 15m: 98%, 30m: 89%)
- Subzone State: TAXI_ZONE pressure at 91% (Vehicle capacity: 200 vehicles)
- Transit Link: Churchgate Station operating at 94% pressure (Predicted 15m: 97%)

DOMAIN INTERPRETATION:
South exit chute (Gate 2) is severely overloaded due to simultaneous convergence 
of rail commuters (heading to Churchgate) and rideshare seekers (heading to Taxi Zone).

AFFECTED ENTITIES:
- Primary: WANKHEDE_EXIT_GATE_2, TAXI_ZONE, CHURCHGATE
- Connected: ROAD_VEER_NARIMAN

OPERATIONAL RELATIONSHIP:
Gate 2 Surge (94%) -> Dual Convergence (Rail + Taxi) -> Cab Queue Accumulation -> Turnstile Stagnation

EXPECTED DOMAIN INTELLIGENCE (Target Reasoning Output):
Detect dual-modal convergence at Gate 2. Formulate load-shredding strategy: 
recommend dispatching rideshare vehicles to auxiliary pickup locations and establishing 
concourse queue management at Churchgate to accelerate platform clearance.
================================================================================
```

```
================================================================================
EXAMPLE 3: Churchgate Western Railway Signal Disruption
================================================================================
ACTUAL JUNCTION STATE:
- Scenario: TRANSPORT_DISRUPTION
- Transport Status: CHURCHGATE DISRUPTED (Western Railway signal fault, 20 min delay)
- Zone State: CHURCHGATE pressure at 89% (Predicted 15m: 94%, 30m: 97%, 60m: 98%)
- Subzone State: WANKHEDE_EXIT_GATE_2 pressure rising to 81% (Predicted 15m: 91%, 30m: 97%)
- Pickup State: TAXI_ZONE pressure at 68% (Predicted 30m: 94%, 60m: 97%)

DOMAIN INTERPRETATION:
Western Railway departure disruption prevents platform evacuation at Churchgate. 
Pedestrian inflow from stadium cannot clear, causing crowd accumulation that 
backs up through Gate 2 and increases curbside taxi demand.

AFFECTED ENTITIES:
- Primary: CHURCHGATE, WANKHEDE_EXIT_GATE_2
- Secondary: CSMT, DADAR, ROAD_VEER_NARIMAN

OPERATIONAL RELATIONSHIP:
Rail Disruption -> Terminal Platform Stagnation -> Concourse Backup -> Gate 2 Rejection -> Taxi Demand Surge

EXPECTED DOMAIN INTELLIGENCE (Target Reasoning Output):
Recognize Western Railway clearance failure at Churchgate. Identify an appropriate 
intermodal diversion: recommend opening the Mahapalika Marg transfer corridor toward 
CSMT (Central Railway) and deploying feeder bus services to Dadar Interchange.
================================================================================
```

```
================================================================================
EXAMPLE 4: Uneven Stadium Evacuation Flow Imbalance
================================================================================
ACTUAL JUNCTION STATE:
- Scenario: POST_EVENT_SURGE
- Gate 1 Pressure: 82% (Predicted 15m: 88%)
- Gate 2 Pressure: 94% (Predicted 15m: 98%) — CRITICAL CONVERGENCE
- Flow Capacity Differential: Gate 1 flow cap 2,200 vs Gate 2 flow cap 1,800

DOMAIN INTERPRETATION:
Spatial egress imbalance. Attendees are preferentially exiting via Gate 2 
due to direct proximity to Churchgate, leaving Gate 1 underutilized relative to its 
2,200 flow capacity.

AFFECTED ENTITIES:
- Primary: WANKHEDE_EXIT_GATE_2, WANKHEDE_EXIT_GATE_1
- Connected: ROAD_VEER_NARIMAN, ROAD_MARINE_DR

OPERATIONAL RELATIONSHIP:
Asymmetric Exit Selection -> Gate 2 Overload (94%) + Gate 1 Available Capacity 
-> Extended Overall Evacuation Time

EXPECTED DOMAIN INTELLIGENCE (Target Reasoning Output):
Identify spatial flow imbalance between exit gates. Recommend activating concourse 
informational broadcasts and perimeter stewarding to guide a portion of Southbound 
attendees toward Gate 1 via Marine Drive walkway to balance gate throughput.
================================================================================
```

```
================================================================================
EXAMPLE 5: South Mumbai Hospitality Zone A Saturation
================================================================================
ACTUAL JUNCTION STATE:
- Scenario: ACCOMMODATION_SATURATION
- Hotel H1 (Trident Nariman Point): Pressure 91% (12 available / 540 total rooms)
- Hotel H2 (Intercontinental): Pressure 88% (6 available / 410 total rooms)
- Hotel H4 (Ramada Dadar - Zone C): Pressure 52% (62 available / 250 total rooms)
- Hotel H5 (Kohinoor Dadar - Zone C): Pressure 48% (38 available / 180 total rooms)

DOMAIN INTERPRETATION:
Premium South Mumbai hospitality corridor (Zone A) is operating near complete 
occupancy saturation (H1 91%, H2 88%), while Zone C hotels in Dadar maintain substantial 
available room inventory (H4 62 rooms, H5 38 rooms) at normal pressure (48–52%).

AFFECTED ENTITIES:
- Primary: H1, H2, ZONE_HOTELS_SOUTH
- Relief Entities: H4, H5, ZONE_DADAR

OPERATIONAL RELATIONSHIP:
Zone A Saturation (91%) -> Local Availability Reduction -> Regional Redistribution Requirement -> Zone C Capacity Utilization

EXPECTED DOMAIN INTELLIGENCE (Target Reasoning Output):
Detect localized hospitality exhaustion in Zone A. Recommend redistributing unaccommodated 
guest inquiries toward Zone C (Dadar) hotels where 100 rooms remain available across H4 and H5.
================================================================================
```

```
================================================================================
EXAMPLE 6: Dining Wait-Time & Hospitality Imbalance
================================================================================
ACTUAL JUNCTION STATE:
- Scenario: POST_EVENT_SURGE (Restaurant Pressure Multiplier 1.4)
- Restaurant R1 (Trishna - Zone A): Occupancy 76/80, Wait Time 55 min, Pressure 91% (CRITICAL)
- Restaurant R2 (Bade Miya - Zone A): Occupancy 54/60, Wait Time 42 min, Pressure 84% (HIGH)
- Restaurant R4 (Café Madras - Zone C): Occupancy 42/90, Wait Time 5 min, Pressure 46% (NORMAL)

DOMAIN INTERPRETATION:
Post-match dining demand is concentrated in Zone A (R1 91% pressure, 55 min wait), 
while high-capacity dining in Zone C (R4 Café Madras, 18 available tables, 5 min wait) 
operates under normal baseline conditions.

AFFECTED ENTITIES:
- Primary: R1, R2, R4, R5
- Connected Sectors: Zone A (Fort/Colaba), Zone C (Matunga/Dadar)

OPERATIONAL RELATIONSHIP:
Zone A Culinary Concentrated Demand -> Extended Wait Times -> Unutilized Zone C Dining Capacity

EXPECTED DOMAIN INTELLIGENCE (Target Reasoning Output):
Identify 50-minute wait-time contrast between Zone A (R1 55 min) and Zone C (R4 5 min). 
Recommend promoting available Zone C dining options to relieve Zone A restaurant overcrowding.
================================================================================
```

```
================================================================================
EXAMPLE 7: Event Schedule Shift (+30 Min Rain Delay)
================================================================================
ACTUAL JUNCTION STATE:
- Scenario: EVENT_DELAY
- Match Timing: Concluding 30 minutes later than scheduled
- Pressure Trajectory: WANKHEDE 45% -> 82% p60; CHURCHGATE 55% -> 91% p60; GATE 2 48% -> 90% p60
- Transit Schedule Interaction: Off-peak transit frequency reductions occur late-night

DOMAIN INTERPRETATION:
Match delay shifts the crowd egress peak later into the evening, increasing the 
risk of a temporal mismatch between peak crowd dispersal and late-night rail service schedules.

AFFECTED ENTITIES:
- Primary: CHURCHGATE, MARINE_LINES, CSMT, TAXI_ZONE
- Secondary: WANKHEDE_EXIT_GATE_2

OPERATIONAL RELATIONSHIP:
Event Completion Delay -> Shifted Egress Peak -> Late-Night Transit Frequency Reduction -> Station Platform Buildup

EXPECTED DOMAIN INTELLIGENCE (Target Reasoning Output):
Recognize temporal collision between delayed egress peak and late-night transit service scaling. 
Recommend extending peak suburban rail service frequency and delaying taxi staging teardown 
to align transit availability with the shifted egress peak.
================================================================================
```

```
================================================================================
EXAMPLE 8: Heavy Monsoon Rain & Compound Transit Friction
================================================================================
ACTUAL JUNCTION STATE:
- Scenario: HEAVY_RAIN (weatherCondition: HEAVY_RAIN)
- Physical Coefficients (`weatherImpactModel.ts`):
  - WALKING_SPEED_REDUCTION_COEFF: 0.007 per mm/h
  - TAXI_DEMAND_SURGE_COEFF: 0.016 per mm/h
  - ROAD_CONGESTION_COEFF: 0.009 per mm/h
  - GATE_2_RAIN_FACTOR: 1.28
- Zone State: TAXI_ZONE pressure 87% (Predicted 30m: 97%), GATE 2 pressure 84% (Predicted 60m: 96%)

DOMAIN INTERPRETATION:
Heavy monsoon rain causes compound operational friction: reduced walking speed 
increases concourse dwell time, while cab hailing surges and Gate 2 experiences elevated 
rain convergence (1.28 multiplier).

AFFECTED ENTITIES:
- Primary: TAXI_ZONE, WANKHEDE_EXIT_GATE_2, CHURCHGATE, ROAD_MARINE_DR
- Environmental: Weather Twin Layer (`weatherImpactModel.ts`)

OPERATIONAL RELATIONSHIP:
Monsoon Rain -> Pedestrian Speed Reduction + Taxi Demand Surge + Concourse Dwell -> Gate 2 / Taxi Zone Saturation

EXPECTED DOMAIN INTELLIGENCE (Target Reasoning Output):
Evaluate compound environmental friction. Recommend opening covered concourse holding 
areas, adjusting traffic signal timing on Veer Nariman Road, and deploying rain-shuttle 
fleets to clear station entryways.
================================================================================
```

---

## 6. CASCADING & CONNECTED IMPACT DYNAMICS

JUNCTION operational logic enforces multi-tier impact propagation across urban networks.

### Primary Physical Cascade (Evacuation & Transit)
```
[Event Conclusion / Surge]
          │
          ▼ (Physical Egress)
[Exit Gate 1 & 2 Turnstiles] ──(Overflow)──► [Perimeter Sidewalk Congestion]
          │                                                │
          ▼ (Pedestrian Flow)                              ▼
[Churchgate & Marine Lines] ──(Disruption)──► [Cab Hailing Surge @ Taxi Zone]
          │                                                │
          ▼ (Platform Saturation)                          ▼
[Evacuation Re-route to CSMT] ◄──(Intermodal)──► [Arterial Gridlock @ Veer Nariman]
          │
          ▼
[Regional Relief @ Dadar Hub]
```

### Secondary Economic Cascade (Hospitality & Capacity)
```
[33,000 Spectator Dispersal]
          │
          ▼
[Zone A Hotel & Restaurant Peak] ──(91%+ Pressure)──► [Reservation Rejections]
          │                                                     │
          ▼                                                     ▼
[Local Area Over-crowding] ◄────(Re-routing)───────── [Zone C Hospitality Relief]
                                                        (Dadar / Matunga Hubs)
```

### Distinction Between Implemented & Inferred Relationships

#### Directly Represented in JUNCTION Code
* Exact node capacities (`totalCapacity` in `mockResources.ts` and `nominalPedestrianCapacity` in `zoneRegistry.ts`).
* Multi-horizon pressure forecasting equations ($p_{15}, p_{30}, p_{60}$ in `mockScenarios.ts`).
* Physical weather impact coefficients (walking speed reduction, taxi demand surge, gate multipliers in `weatherImpactModel.ts`).
* Hotel and restaurant room counts, seat capacities, base wait times, and pressure metrics (`mockHotels.ts`, `mockRestaurants.ts`).

#### Domain-Level Reasoning Target for Nugen Alignment
* Downstream multi-modal crowd equilibrium reasoning across transit operators.
* Qualitative operator intervention synthesis based on multi-zone pressure metrics.
* Dynamic crowd behavior interpretation under compound environmental and transit stress.

---

## 7. NUGEN DOMAIN ALIGNMENT OBJECTIVE

The explicit target objective for Nugen Domain Alignment is defined as follows:

> **Alignment Objective:**  
> Given a live JUNCTION operational state (comprising venue gate throughputs, rail terminal pressures, road congestion levels, hospitality occupancies, and environmental parameters), the Nugen-aligned model must understand the domain-specific spatial and capacity relationships across South Mumbai's urban network; accurately identify primary pressure points and downstream cascading impacts; and generate actionable, human-in-the-loop operational intelligence that directly supports the JUNCTION operator decision workflow.

---

## 8. ARCHITECTURAL SEPARATION FROM EXISTING AI

To guarantee system stability and preserve project integrity, the following architectural boundary is strictly enforced:

> [!IMPORTANT]
> **Architectural Constraint:**  
> The existing JUNCTION AI model (Google Gemini Flash via `@google/genai`) and prediction engines remain completely unchanged. The Nugen-aligned model operates as an **independent, additive domain-intelligence layer**. It does NOT replace, modify, retrain, wrap, or become a build dependency of the existing JUNCTION AI or simulation pipeline.

---

## 9. DATA QUALITY & CLEANLINESS ASSURANCE

This specification adheres to strict data sanitization standards:
* **No Code Clutter:** Implementation noise, TypeScript syntax, UI props, and component boilerplate have been excluded.
* **Zero Credentials:** No API keys, OAuth publishable keys, database URIs, or passwords are contained in this document.
* **Domain Authenticity:** All names, geographic locations (Wankhede, Churchgate, Marine Drive, Dadar), room counts, and pressure metrics match the exact ground truth of the JUNCTION repository.

---

## 10. SYSTEM CAPABILITY BOUNDARIES & NON-REPRESENTED FEATURES

In accordance with strict factual accuracy guidelines, the following capabilities are explicitly documented as **not represented in the current JUNCTION implementation**:

* `Air Quality / Pollution Indexing`: Not represented in the current JUNCTION implementation.
* `Subsurface Metro Line 3 Live Telemetry`: Not represented in the current JUNCTION implementation (Suburban Western & Central Railways represent the ground truth).
* `Automated Ticket Refund Interventions`: Not represented in the current JUNCTION implementation.
* `Automated Traffic Signal Hardware Actuation`: Not represented in the current JUNCTION implementation (Operator recommendation & manual override workflow represented).
* `Drone Aerial Video Stream Processing`: Not represented in the current JUNCTION implementation (Optical CCTV and LiDAR counter modalities represented).

---

## 11. SUMMARY & AUDIT METRICS

1. **Domain Sections:** 11 comprehensive structural sections.
2. **Scenarios Extracted:** 6 real scenarios (`NORMAL`, `POST_EVENT_SURGE`, `TRANSPORT_DISRUPTION`, `HEAVY_RAIN`, `ACCOMMODATION_SATURATION`, `EVENT_DELAY`).
3. **Alignment Examples Created:** 8 detailed, domain-specific input-to-reasoning mapping pairs.
4. **JUNCTION Data Sources Verified:** `src/data/mockScenarios.ts`, `src/services/zoneRegistry.ts`, `src/data/mockResources.ts`, `src/data/mockHotels.ts`, `src/data/mockRestaurants.ts`, `src/services/weatherImpactModel.ts`.
5. **Factual Gaps Identified:** Metro Line 3 live telemetry and automated traffic signal hardware actuation are unrepresented in the core codebase and explicitly documented as non-represented boundaries.
