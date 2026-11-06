import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:url_launcher/url_launcher.dart';
import '../models/map_destination.dart';
import '../models/types.dart';
import '../services/crowd_routing_service.dart';
import '../services/location_service.dart';
import '../services/navigation_service.dart';
import '../services/routing_service.dart';
import '../state/app_state.dart';
import '../theme/app_theme.dart';
import '../widgets/pill_badge.dart';
import '../widgets/motion_tap.dart';

class EventScreen extends StatelessWidget {
  final AppState appState;

  const EventScreen({super.key, required this.appState});

  Future<void> _makePhoneCall(String phoneNumber) async {
    final cleanNumber = phoneNumber.replaceAll(RegExp(r'[^\d+]'), '');
    final Uri launchUri = Uri(scheme: 'tel', path: cleanNumber);
    try {
      if (await canLaunchUrl(launchUri)) {
        await launchUrl(launchUri);
      } else {
        await launchUrl(launchUri, mode: LaunchMode.externalApplication);
      }
    } catch (e) {
      debugPrint('Could not launch phone call to $cleanNumber: $e');
    }
  }

  Future<void> _navigateToVenue(BuildContext context) async {
    const venueDest = MapDestination.wankhedeStadium;
    final userLoc = await LocationService.getCurrentLocation() ?? LocationService.defaultDemoLocation;

    final rawRoutes = await RoutingService.getRoutes(
      origin: userLoc,
      destination: venueDest.location,
    );

    final scoredRoutes = CrowdRoutingService.evaluateAndScoreRoutes(
      rawRoutes: rawRoutes,
      appState: appState,
    );

    if (scoredRoutes.isNotEmpty) {
      final bestRoute = scoredRoutes.firstWhere((r) => r.recommended, orElse: () => scoredRoutes.first);
      NavigationService().setAvailableRoutes(scoredRoutes, selected: bestRoute);
      NavigationService().startNavigation(bestRoute, userLoc);
      appState.setTabIndex(0); // Switch to Plan screen map
    }

    if (context.mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          behavior: SnackBarBehavior.floating,
          backgroundColor: AppTheme.ink,
          content: Text(
            "Navigating to Wankhede Stadium on JUNCTION map.",
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
          ),
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final isDelay = appState.activeScenario == ScenarioId.EVENT_DELAY;
    final event = appState.eventInfo;

    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 140),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            "MATCH & VENUE GUIDE",
            style: AppTheme.metaText(fontSize: 12, color: AppTheme.inkMuted),
          ).animate().fadeIn(duration: 250.ms),
          const SizedBox(height: 6),
          Text(
            "Event Information",
            style: AppTheme.displayFont(
              fontSize: 22,
              fontWeight: FontWeight.w700,
              letterSpacing: -0.5,
            ),
          ).animate().fadeIn(duration: 300.ms, delay: 50.ms).slideY(begin: 0.08, end: 0),
          const SizedBox(height: 16),

          // Main Event Card
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
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    const PillBadge(
                      text: "● LIVE",
                      variant: PillVariant.live,
                      fontSize: 10,
                    ),
                    if (isDelay)
                      const PillBadge(
                        text: "DELAYED +30 MIN",
                        variant: PillVariant.critical,
                        fontSize: 10,
                      ),
                  ],
                ),
                const SizedBox(height: 12),
                Text(
                  event.name,
                  style: AppTheme.displayFont(
                    fontSize: 20,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  event.venue,
                  style: AppTheme.bodyFont(
                    fontSize: 13,
                    color: AppTheme.inkMuted,
                  ),
                ),
                const SizedBox(height: 16),

                // Directions Button
                MotionTap(
                  onTap: () => _navigateToVenue(context),
                  scaleDown: 0.96,
                  child: Container(
                    width: double.infinity,
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    decoration: BoxDecoration(
                      color: AppTheme.yellow,
                      borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                      boxShadow: [
                        BoxShadow(
                          color: AppTheme.yellow.withValues(alpha: 0.3),
                          blurRadius: 6,
                          offset: const Offset(0, 2),
                        ),
                      ],
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        const Icon(Icons.navigation, size: 16, color: AppTheme.ink),
                        const SizedBox(width: 8),
                        Text(
                          "GET DIRECTIONS TO VENUE",
                          style: AppTheme.displayFont(
                            fontSize: 12,
                            fontWeight: FontWeight.w800,
                            letterSpacing: 0.3,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),

                // Timetable Grid
                Container(
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: AppTheme.paper,
                    borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                  ),
                  child: Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              "GATE OPEN",
                              style: AppTheme.metaText(
                                fontSize: 9,
                                color: AppTheme.inkFaint,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              isDelay ? "19:30" : "18:00",
                              style: AppTheme.displayFont(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                          ],
                        ),
                      ),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              "MATCH START",
                              style: AppTheme.metaText(
                                fontSize: 9,
                                color: AppTheme.inkFaint,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              isDelay ? "20:00" : "19:30",
                              style: AppTheme.displayFont(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                                color: isDelay ? AppTheme.red : AppTheme.ink,
                              ),
                            ),
                          ],
                        ),
                      ),
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              "EST END",
                              style: AppTheme.metaText(
                                fontSize: 9,
                                color: AppTheme.inkFaint,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              isDelay ? "23:00" : "22:30",
                              style: AppTheme.displayFont(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          )
              .animate()
              .fadeIn(duration: 350.ms, delay: 80.ms)
              .slideY(begin: 0.08, end: 0, curve: Curves.easeOutCubic),

          const SizedBox(height: 20),

          // Allocated Gate Card
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
                Text(
                  "YOUR ALLOCATED GATE",
                  style: AppTheme.metaText(fontSize: 11, color: AppTheme.inkMuted),
                ),
                const SizedBox(height: 10),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
                  decoration: BoxDecoration(
                    color: AppTheme.yellowLight,
                    borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                    border: Border.all(color: AppTheme.yellow),
                  ),
                  child: Row(
                    children: [
                      const Icon(Icons.meeting_room, size: 20, color: AppTheme.ink),
                      const SizedBox(width: 10),
                      Text(
                        event.allocatedGate,
                        style: AppTheme.displayFont(
                          fontSize: 14,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 10),
                Text(
                  "Arrive at least 30 minutes before match time. Expect security queue screening at peak arrival waves.",
                  style: AppTheme.bodyFont(
                    fontSize: 12,
                    color: AppTheme.inkMuted,
                    height: 1.4,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 20),

          // EMERGENCY CONTACT (Compact Section)
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
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      "EMERGENCY CONTACT",
                      style: AppTheme.displayFont(
                        fontSize: 13,
                        fontWeight: FontWeight.w800,
                        letterSpacing: 0.3,
                      ),
                    ),
                    const Icon(Icons.phone_in_talk, size: 18, color: AppTheme.red),
                  ],
                ),
                const SizedBox(height: 4),
                Text(
                  "Need immediate assistance?",
                  style: AppTheme.bodyFont(fontSize: 12, color: AppTheme.inkMuted),
                ),
                const SizedBox(height: 14),

                _buildEmergencyRow("Security / Emergency", "112", Icons.local_police),
                const Divider(height: 16, color: AppTheme.neutral),
                _buildEmergencyRow("Medical Assistance", "102", Icons.medical_services),
                const Divider(height: 16, color: AppTheme.neutral),
                _buildEmergencyRow("Event Help Desk", "+91 22 2279 5500", Icons.support_agent),
              ],
            ),
          )
              .animate()
              .fadeIn(duration: 350.ms, delay: 200.ms)
              .slideY(begin: 0.08, end: 0, curve: Curves.easeOutCubic),
        ],
      ),
    );
  }

  Widget _buildEmergencyRow(String label, String contact, IconData icon) {
    return MotionTap(
      onTap: () => _makePhoneCall(contact),
      scaleDown: 0.96,
      child: Row(
        children: [
          Icon(icon, size: 16, color: AppTheme.inkMuted),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: AppTheme.displayFont(fontSize: 12, fontWeight: FontWeight.w700),
                  overflow: TextOverflow.ellipsis,
                ),
                Text(
                  contact,
                  style: AppTheme.bodyFont(fontSize: 11, color: AppTheme.inkMuted),
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
            decoration: BoxDecoration(
              color: AppTheme.yellow,
              borderRadius: BorderRadius.circular(AppTheme.radiusSm),
              boxShadow: [
                BoxShadow(
                  color: AppTheme.yellow.withValues(alpha: 0.3),
                  blurRadius: 4,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.phone, size: 12, color: AppTheme.ink),
                const SizedBox(width: 4),
                Text(
                  label.contains("Help") ? "Contact" : "Call",
                  style: AppTheme.displayFont(fontSize: 11, fontWeight: FontWeight.w800),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

