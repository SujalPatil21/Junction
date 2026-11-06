import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:latlong2/latlong.dart';
import '../models/map_destination.dart';
import '../models/route_option.dart';
import '../services/routing_service.dart';
import '../services/transit_routing_service.dart';
import '../services/crowd_routing_service.dart';
import '../services/navigation_service.dart';
import '../services/location_service.dart';
import '../state/app_state.dart';
import '../theme/app_theme.dart';
import '../models/types.dart';
import '../widgets/pill_badge.dart';
import '../widgets/motion_tap.dart';
import '../widgets/route_transit_map.dart';
import '../widgets/travel_mode_selector.dart';

class PlanScreen extends StatefulWidget {
  final AppState appState;

  const PlanScreen({super.key, required this.appState});

  @override
  State<PlanScreen> createState() => _PlanScreenState();
}

class _PlanScreenState extends State<PlanScreen> {
  final NavigationService _navService = NavigationService();
  final ScrollController _mainScrollController = ScrollController();

  List<RouteOption> _calculatedRoutes = [];
  RouteOption? _selectedRouteOption;
  bool _isLoadingRoutes = false;
  bool _hasPlanned = false;
  bool _showAdvancedOptions = false;
  LatLng _userOrigin = LocationService.defaultDemoLocation;

  MapDestination get activeDestination =>
      _navService.activeDestination ?? MapDestination.wankhedeStadium;

  MapDestination get activeOrigin =>
      _navService.activeOrigin ??
      MapDestination(
        id: 'user_origin',
        name: 'Current Location',
        location: _userOrigin,
        type: DestinationType.OTHER,
      );

  TravelMode get _activeTravelMode => _navService.travelMode;

  @override
  void initState() {
    super.initState();
    _loadRoutes();
    widget.appState.addListener(_onAppStateScenarioChanged);
    _navService.addListener(_onNavServiceUpdated);
  }

  @override
  void dispose() {
    widget.appState.removeListener(_onAppStateScenarioChanged);
    _navService.removeListener(_onNavServiceUpdated);
    _mainScrollController.dispose();
    super.dispose();
  }

  void _onAppStateScenarioChanged() {
    if (mounted) {
      _reEvaluateRoutes();
    }
  }

  void _onNavServiceUpdated() {
    if (mounted) {
      setState(() {
        if (_navService.allRouteOptions.isNotEmpty) {
          _calculatedRoutes = _navService.allRouteOptions;
        }
        if (_navService.activeRoute != null) {
          _selectedRouteOption = _navService.activeRoute;
        }
      });
    }
  }

  Future<List<RouteOption>> _fetchRawRoutesForMode(
    LatLng originLoc,
    MapDestination targetDest,
    TravelMode mode,
  ) async {
    final List<RouteOption> rawList = [];

    if (mode == TravelMode.multimodal || mode == TravelMode.driving) {
      final roadRoutes = await RoutingService.getRoutes(
        origin: originLoc,
        destination: targetDest.location,
        mode: TravelMode.driving,
      );
      rawList.addAll(roadRoutes);
    }

    if (mode == TravelMode.multimodal || mode == TravelMode.walking) {
      final walkRoutes = await RoutingService.getRoutes(
        origin: originLoc,
        destination: targetDest.location,
        mode: TravelMode.walking,
      );
      rawList.addAll(walkRoutes);
    }

    if (mode == TravelMode.multimodal ||
        mode == TravelMode.train ||
        mode == TravelMode.metro) {
      final transitRoutes = await TransitRoutingService.findTransitRoutes(
        origin: originLoc,
        destination: targetDest.location,
        destinationName: targetDest.name,
        scenario: widget.appState.activeScenario,
        preferredMode: mode,
      );
      for (var tr in transitRoutes) {
        rawList.add(tr.toRouteOption());
      }
    }

    return rawList;
  }

  Future<void> _loadRoutes({TravelMode? overrideMode}) async {
    final mode = overrideMode ?? _activeTravelMode;
    setState(() {
      _isLoadingRoutes = true;
    });

    final loc = await LocationService.getCurrentLocation();
    if (loc != null) {
      _userOrigin = loc;
    }

    final targetDest = activeDestination;
    final originLoc = _navService.activeOrigin?.location ?? _userOrigin;

    final rawRoutes = await _fetchRawRoutesForMode(originLoc, targetDest, mode);

    final scoredRoutes = CrowdRoutingService.evaluateAndScoreRoutes(
      rawRoutes: rawRoutes,
      appState: widget.appState,
    );

    if (mounted) {
      setState(() {
        _calculatedRoutes = scoredRoutes;
        _selectedRouteOption = scoredRoutes.firstWhere(
          (r) => r.recommended,
          orElse: () => scoredRoutes.isNotEmpty ? scoredRoutes.first : rawRoutes.first,
        );
        _isLoadingRoutes = false;
        _hasPlanned = true;
      });

      _navService.setAvailableRoutes(
        scoredRoutes,
        selected: _selectedRouteOption,
        destination: targetDest,
        origin: activeOrigin,
        mode: mode,
      );
    }
  }

  void _onTravelModeChanged(TravelMode newMode) {
    _navService.setTravelMode(newMode);
    _loadRoutes(overrideMode: newMode);
  }

  void _reEvaluateRoutes() {
    if (_calculatedRoutes.isEmpty) return;

    final scoredRoutes = CrowdRoutingService.evaluateAndScoreRoutes(
      rawRoutes: _calculatedRoutes,
      appState: widget.appState,
    );

    setState(() {
      _calculatedRoutes = scoredRoutes;
      if (_selectedRouteOption != null) {
        final match = scoredRoutes.firstWhere(
          (r) => r.id == _selectedRouteOption!.id,
          orElse: () => scoredRoutes.first,
        );
        _selectedRouteOption = match;
      }
    });

    _navService.setAvailableRoutes(scoredRoutes, selected: _selectedRouteOption);
  }

  void _startInAppNavigation() {
    if (_selectedRouteOption == null) return;

    final dest = activeDestination;
    final orig = activeOrigin;

    _navService.startNavigation(
      _selectedRouteOption!,
      orig.location,
      destination: dest,
      origin: orig,
    );
    widget.appState.selectAttendeeRoute(_selectedRouteOption!.id);

    ScaffoldMessenger.of(context).clearSnackBars();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        behavior: SnackBarBehavior.floating,
        margin: const EdgeInsets.only(left: 20, right: 20, bottom: 84),
        backgroundColor: AppTheme.ink,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        content: Row(
          children: [
            const Icon(Icons.navigation_rounded, color: AppTheme.yellow, size: 18),
            const SizedBox(width: 10),
            Expanded(
              child: Text(
                "Navigating to ${dest.name}",
                style: AppTheme.displayFont(
                  fontSize: 12,
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  void _resetToVenueNavigation() {
    _navService.clearNavigation();
    _loadRoutes();
  }

  @override
  Widget build(BuildContext context) {
    final activeRoute = _selectedRouteOption ??
        (_calculatedRoutes.isNotEmpty ? _calculatedRoutes.first : null);

    return SingleChildScrollView(
      controller: _mainScrollController,
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 140),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Text(
            "PLAN YOUR JOURNEY",
            style: AppTheme.displayFont(
              fontSize: 22,
              fontWeight: FontWeight.w700,
              letterSpacing: -0.5,
            ),
          ).animate().fadeIn(duration: 250.ms),
          const SizedBox(height: 16),

          // Clean Planning Card (FROM -> TO -> DATE/TIME -> TRAVEL OPTIONS -> PLAN JOURNEY)
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: AppTheme.white,
              borderRadius: BorderRadius.circular(AppTheme.radiusMd),
              border: Border.all(color: AppTheme.neutral, width: 1.0),
              boxShadow: AppTheme.shadowSm,
            ),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                // FROM
                Text(
                  "FROM",
                  style: AppTheme.metaText(fontSize: 10, color: AppTheme.inkFaint),
                ),
                const SizedBox(height: 4),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  decoration: BoxDecoration(
                    color: AppTheme.paper,
                    borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                    border: Border.all(color: AppTheme.neutral),
                  ),
                  child: Text(
                    activeOrigin.name,
                    style: AppTheme.displayFont(
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                const SizedBox(height: 14),

                // TO
                Text(
                  "TO",
                  style: AppTheme.metaText(fontSize: 10, color: AppTheme.inkFaint),
                ),
                const SizedBox(height: 4),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  decoration: BoxDecoration(
                    color: AppTheme.paper,
                    borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                    border: Border.all(color: AppTheme.neutral),
                  ),
                  child: Text(
                    activeDestination.name,
                    style: AppTheme.displayFont(
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
                const SizedBox(height: 14),

                // DATE / TIME
                Text(
                  "DATE / TIME",
                  style: AppTheme.metaText(fontSize: 10, color: AppTheme.inkFaint),
                ),
                const SizedBox(height: 4),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  decoration: BoxDecoration(
                    color: AppTheme.paper,
                    borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                    border: Border.all(color: AppTheme.neutral),
                  ),
                  child: Text(
                    "Today · Match Time (18:00)",
                    style: AppTheme.displayFont(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                const SizedBox(height: 14),

                // TRAVEL OPTIONS
                Text(
                  "TRAVEL OPTIONS",
                  style: AppTheme.metaText(fontSize: 10, color: AppTheme.inkFaint),
                ),
                const SizedBox(height: 6),
                TravelModeSelector(
                  activeMode: _activeTravelMode,
                  onModeChanged: _onTravelModeChanged,
                ),
                const SizedBox(height: 18),

                // PLAN JOURNEY BUTTON
                MotionTap(
                  onTap: _loadRoutes,
                  scaleDown: 0.96,
                  child: Container(
                    width: double.infinity,
                    padding: const EdgeInsets.symmetric(vertical: 14),
                    decoration: BoxDecoration(
                      color: AppTheme.yellow,
                      borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                      boxShadow: [
                        BoxShadow(
                          color: AppTheme.yellow.withValues(alpha: 0.3),
                          blurRadius: 8,
                          offset: const Offset(0, 3),
                        ),
                      ],
                    ),
                    alignment: Alignment.center,
                    child: _isLoadingRoutes
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              color: AppTheme.ink,
                              strokeWidth: 2,
                            ),
                          )
                        : Text(
                            "PLAN JOURNEY",
                            style: AppTheme.displayFont(
                              fontSize: 13,
                              fontWeight: FontWeight.w800,
                              letterSpacing: 0.5,
                            ),
                          ),
                  ),
                ),
              ],
            ),
          ).animate().fadeIn(duration: 350.ms, delay: 50.ms),

          const SizedBox(height: 16),

          // Collapsible Advanced Options
          GestureDetector(
            onTap: () => setState(() => _showAdvancedOptions = !_showAdvancedOptions),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  _showAdvancedOptions ? "Hide Advanced Options" : "Advanced Options",
                  style: AppTheme.metaText(
                    fontSize: 11,
                    color: AppTheme.inkMuted,
                  ),
                ),
                Icon(
                  _showAdvancedOptions ? Icons.keyboard_arrow_up : Icons.keyboard_arrow_down,
                  size: 18,
                  color: AppTheme.inkMuted,
                ),
              ],
            ),
          ),
          if (_showAdvancedOptions) ...[
            const SizedBox(height: 10),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: AppTheme.paper,
                borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                border: Border.all(color: AppTheme.neutral),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    "Destination Reset",
                    style: AppTheme.bodyFont(fontSize: 12, color: AppTheme.ink),
                  ),
                  MotionTap(
                    onTap: _resetToVenueNavigation,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                      decoration: BoxDecoration(
                        color: AppTheme.ink,
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: const Text(
                        "RESET TO VENUE",
                        style: TextStyle(
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                          color: Colors.white,
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ],

          const SizedBox(height: 20),

          // JOURNEY RESULT (If planned & active route exists)
          if (_hasPlanned && activeRoute != null) ...[
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: AppTheme.white,
                borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                border: Border.all(color: AppTheme.yellow, width: 1.5),
                boxShadow: AppTheme.shadowSm,
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        "JOURNEY RESULT",
                        style: AppTheme.metaText(
                          fontSize: 11,
                          color: AppTheme.inkMuted,
                        ),
                      ),
                      const PillBadge(
                        text: "RECOMMENDED",
                        variant: PillVariant.yellow,
                        fontSize: 9,
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),

                  Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              "FROM",
                              style: AppTheme.metaText(fontSize: 9, color: AppTheme.inkFaint),
                            ),
                            Text(
                              activeOrigin.name,
                              style: AppTheme.displayFont(
                                fontSize: 13,
                                fontWeight: FontWeight.w700,
                              ),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ),
                      const Icon(Icons.arrow_forward, size: 14, color: AppTheme.inkMuted),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.end,
                          children: [
                            Text(
                              "TO",
                              style: AppTheme.metaText(fontSize: 9, color: AppTheme.inkFaint),
                            ),
                            Text(
                              activeDestination.name,
                              style: AppTheme.displayFont(
                                fontSize: 13,
                                fontWeight: FontWeight.w700,
                              ),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),

                  // Metrics Summary Grid
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: AppTheme.paper,
                      borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              "EST TIME",
                              style: AppTheme.metaText(fontSize: 9, color: AppTheme.inkFaint),
                            ),
                            Text(
                              activeRoute.formattedDuration,
                              style: AppTheme.displayFont(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                          ],
                        ),
                        Container(width: 1, height: 28, color: AppTheme.neutralDark),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              "DISTANCE",
                              style: AppTheme.metaText(fontSize: 9, color: AppTheme.inkFaint),
                            ),
                            Text(
                              activeRoute.formattedDistance,
                              style: AppTheme.displayFont(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                          ],
                        ),
                        Container(width: 1, height: 28, color: AppTheme.neutralDark),
                        Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              "TRANSPORT",
                              style: AppTheme.metaText(fontSize: 9, color: AppTheme.inkFaint),
                            ),
                            Text(
                              activeRoute.name,
                              style: AppTheme.displayFont(
                                fontSize: 13,
                                fontWeight: FontWeight.w700,
                              ),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 16),

                  // VIEW ROUTE BUTTON
                  MotionTap(
                    onTap: _startInAppNavigation,
                    scaleDown: 0.96,
                    child: Container(
                      width: double.infinity,
                      padding: const EdgeInsets.symmetric(vertical: 14),
                      decoration: BoxDecoration(
                        color: AppTheme.yellow,
                        borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                      ),
                      alignment: Alignment.center,
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          const Icon(Icons.navigation, size: 16, color: AppTheme.ink),
                          const SizedBox(width: 8),
                          Text(
                            "VIEW ROUTE",
                            style: AppTheme.displayFont(
                              fontSize: 13,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ).animate().fadeIn(duration: 350.ms),

            const SizedBox(height: 16),

            // Route Transit Map Preview
            RouteTransitMap(
              routeOptions: _calculatedRoutes,
              selectedRoute: _selectedRouteOption,
              height: 240,
              isInteractive: true,
              destination: activeDestination,
              onRouteSelected: (route) {
                setState(() {
                  _selectedRouteOption = route;
                });
                _navService.selectRoute(route);
              },
              onStartNavigation: _startInAppNavigation,
            ),
          ],
        ],
      ),
    );
  }
}
