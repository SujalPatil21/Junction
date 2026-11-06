import 'package:flutter/material.dart';
import 'package:flutter_animate/flutter_animate.dart';
import 'package:latlong2/latlong.dart';
import '../models/map_destination.dart';
import '../models/types.dart';
import 'destination_route_screen.dart';
import '../services/maps_launcher.dart';
import '../state/app_state.dart';
import '../theme/app_theme.dart';
import '../widgets/pressure_bar.dart';
import '../widgets/motion_tap.dart';

class StayScreen extends StatefulWidget {
  final AppState appState;

  const StayScreen({super.key, required this.appState});

  @override
  State<StayScreen> createState() => _StayScreenState();
}

class _StayScreenState extends State<StayScreen> {
  String _searchQuery = "";
  String _sortBy = "RATING"; // RATING, PRICE, DISTANCE

  void _showHotelDetailModal(BuildContext context, Hotel hotel) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => _HotelDetailSheet(hotel: hotel, appState: widget.appState),
    );
  }

  @override
  Widget build(BuildContext context) {
    final hotels = widget.appState.hotels;

    List<Hotel> displayHotels = hotels.where((h) {
      if (_searchQuery.isEmpty) return true;
      final query = _searchQuery.toLowerCase();
      return h.name.toLowerCase().contains(query) ||
          h.address.toLowerCase().contains(query) ||
          h.zone.toLowerCase().contains(query);
    }).toList();

    if (_sortBy == "RATING") {
      displayHotels.sort((a, b) => b.rating.compareTo(a.rating));
    } else if (_sortBy == "PRICE") {
      displayHotels.sort((a, b) => a.priceRange.compareTo(b.priceRange));
    } else if (_sortBy == "DISTANCE") {
      displayHotels.sort((a, b) => a.travelTimeToVenue.compareTo(b.travelTimeToVenue));
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(20, 16, 20, 140),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            "ACCOMMODATION",
            style: AppTheme.metaText(fontSize: 12, color: AppTheme.inkMuted),
          ).animate().fadeIn(duration: 250.ms),
          const SizedBox(height: 6),
          Text(
            "Hotels",
            style: AppTheme.displayFont(
              fontSize: 22,
              fontWeight: FontWeight.w700,
              letterSpacing: -0.5,
            ),
          ).animate().fadeIn(duration: 300.ms, delay: 50.ms).slideY(begin: 0.08, end: 0),
          const SizedBox(height: 16),

          // Search Box
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 4),
            decoration: BoxDecoration(
              color: AppTheme.white,
              borderRadius: BorderRadius.circular(AppTheme.radiusSm),
              border: Border.all(color: AppTheme.neutral, width: 1.0),
              boxShadow: AppTheme.shadowSm,
            ),
            child: TextField(
              onChanged: (val) => setState(() => _searchQuery = val),
              style: AppTheme.bodyFont(fontSize: 14, color: AppTheme.ink),
              decoration: const InputDecoration(
                icon: Icon(Icons.search, size: 20, color: AppTheme.inkMuted),
                hintText: "Search hotels by name or location...",
                border: InputBorder.none,
              ),
            ),
          ),
          const SizedBox(height: 12),

          // Filter Pills Row (Sort by Price, Rating, Distance)
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            child: Row(
              children: [
                _buildFilterChip("RATING", "Top Rated ★", _sortBy == "RATING"),
                const SizedBox(width: 8),
                _buildFilterChip("DISTANCE", "Closest to Venue", _sortBy == "DISTANCE"),
                const SizedBox(width: 8),
                _buildFilterChip("PRICE", "Price / Category", _sortBy == "PRICE"),
              ],
            ),
          ),
          const SizedBox(height: 16),

          // Hotel Count Header
          Text(
            "ALL HOTELS (${displayHotels.length})",
            style: AppTheme.metaText(fontSize: 11, color: AppTheme.inkMuted),
          ),
          const SizedBox(height: 12),

          // Unified Hotel List
          ...List.generate(displayHotels.length, (idx) {
            final h = displayHotels[idx];

            return MotionTap(
              onTap: () => _showHotelDetailModal(context, h),
              scaleDown: 0.98,
              child: Container(
                margin: const EdgeInsets.only(bottom: 14),
                decoration: BoxDecoration(
                  color: AppTheme.white,
                  borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                  border: Border.all(color: AppTheme.neutral, width: 1.0),
                  boxShadow: AppTheme.shadowSm,
                ),
                child: ClipRRect(
                  borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      // HOTEL IMAGE (Restored)
                      if (h.imageUrl != null)
                        SizedBox(
                          height: 140,
                          width: double.infinity,
                          child: Image.network(
                            h.imageUrl!,
                            fit: BoxFit.cover,
                            errorBuilder: (ctx, err, stack) => Container(
                              height: 140,
                              color: AppTheme.neutral,
                              child: const Center(
                                child: Icon(Icons.hotel, color: AppTheme.inkMuted, size: 36),
                              ),
                            ),
                          ),
                        ),

                      Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Expanded(
                                  child: Column(
                                    crossAxisAlignment: CrossAxisAlignment.start,
                                    children: [
                                      Text(
                                        h.name,
                                        style: AppTheme.displayFont(
                                          fontSize: 16,
                                          fontWeight: FontWeight.w700,
                                        ),
                                      ),
                                      const SizedBox(height: 2),
                                      Text(
                                        h.address,
                                        style: AppTheme.bodyFont(
                                          fontSize: 12,
                                          color: AppTheme.inkMuted,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                                  decoration: BoxDecoration(
                                    color: AppTheme.yellowLight,
                                    borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                                    border: Border.all(color: AppTheme.yellow),
                                  ),
                                  child: Row(
                                    children: [
                                      const Icon(Icons.star, color: AppTheme.ink, size: 14),
                                      const SizedBox(width: 4),
                                      Text(
                                        "${h.rating}",
                                        style: AppTheme.displayFont(
                                          fontSize: 12,
                                          fontWeight: FontWeight.w800,
                                        ),
                                      ),
                                    ],
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),

                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Row(
                                  children: [
                                    const Icon(Icons.place_outlined, size: 14, color: AppTheme.inkMuted),
                                    const SizedBox(width: 4),
                                    Text(
                                      "${h.travelTimeToVenue} min to venue",
                                      style: AppTheme.bodyFont(
                                        fontSize: 12,
                                        color: AppTheme.inkLight,
                                      ),
                                    ),
                                  ],
                                ),
                                Text(
                                  h.priceRange,
                                  style: AppTheme.displayFont(
                                    fontSize: 13,
                                    fontWeight: FontWeight.w700,
                                    color: AppTheme.ink,
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 12),

                            Row(
                              mainAxisAlignment: MainAxisAlignment.spaceBetween,
                              children: [
                                Text(
                                  "${h.usableRooms} rooms available",
                                  style: AppTheme.metaText(
                                    fontSize: 10,
                                    color: AppTheme.inkMuted,
                                  ),
                                ),
                                Container(
                                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                                  decoration: BoxDecoration(
                                    color: AppTheme.yellow,
                                    borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                                  ),
                                  child: Text(
                                    "View Details →",
                                    style: AppTheme.displayFont(
                                      fontSize: 11,
                                      fontWeight: FontWeight.w700,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            )
                .animate()
                .fadeIn(duration: 300.ms, delay: Duration(milliseconds: idx * 40))
                .slideY(begin: 0.05, end: 0, curve: Curves.easeOutCubic);
          }),

          const SizedBox(height: 16),
        ],
      ),
    );
  }

  Widget _buildFilterChip(String key, String label, bool isSelected) {
    return MotionTap(
      onTap: () => setState(() => _sortBy = key),
      scaleDown: 0.94,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        decoration: BoxDecoration(
          color: isSelected ? AppTheme.ink : AppTheme.white,
          borderRadius: BorderRadius.circular(AppTheme.radiusSm),
          border: Border.all(color: isSelected ? AppTheme.ink : AppTheme.neutral),
        ),
        child: Text(
          label,
          style: AppTheme.displayFont(
            fontSize: 11,
            fontWeight: FontWeight.w700,
            color: isSelected ? AppTheme.white : AppTheme.ink,
          ),
        ),
      ),
    );
  }
}

class _HotelDetailSheet extends StatefulWidget {
  final Hotel hotel;
  final AppState appState;

  const _HotelDetailSheet({required this.hotel, required this.appState});

  @override
  State<_HotelDetailSheet> createState() => _HotelDetailSheetState();
}

class _HotelDetailSheetState extends State<_HotelDetailSheet> {
  int _selectedRoomIndex = 0;

  IconData _getAmenityIcon(String name) {
    final lower = name.toLowerCase();
    if (lower.contains("sea") || lower.contains("view")) return Icons.wb_sunny_outlined;
    if (lower.contains("pool")) return Icons.pool_outlined;
    if (lower.contains("dining") || lower.contains("resto") || lower.contains("buffet")) return Icons.restaurant_outlined;
    if (lower.contains("valet") || lower.contains("parking") || lower.contains("car")) return Icons.local_parking_outlined;
    if (lower.contains("lounge") || lower.contains("bar")) return Icons.local_bar_outlined;
    if (lower.contains("spa") || lower.contains("massage")) return Icons.spa_outlined;
    if (lower.contains("wi-fi") || lower.contains("wifi")) return Icons.wifi;
    if (lower.contains("shuttle") || lower.contains("pickup") || lower.contains("taxi")) return Icons.airport_shuttle_outlined;
    if (lower.contains("station") || lower.contains("train")) return Icons.train_outlined;
    if (lower.contains("fitness") || lower.contains("gym")) return Icons.fitness_center_outlined;
    if (lower.contains("pass") || lower.contains("off") || lower.contains("discount")) return Icons.local_offer_outlined;
    if (lower.contains("room") || lower.contains("service") || lower.contains("concierge")) return Icons.room_service_outlined;
    return Icons.check_circle_outline;
  }

  @override
  Widget build(BuildContext context) {
    final h = widget.hotel;
    final isZoneC = h.zone == "ZONE_C";

    return DraggableScrollableSheet(
      initialChildSize: 0.88,
      minChildSize: 0.5,
      maxChildSize: 0.95,
      builder: (context, scrollController) {
        return Container(
          decoration: const BoxDecoration(
            color: AppTheme.white,
            borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
          ),
          child: Column(
            children: [
              // Drag handle
              Center(
                child: Container(
                  margin: const EdgeInsets.only(top: 12, bottom: 8),
                  width: 44,
                  height: 5,
                  decoration: BoxDecoration(
                    color: AppTheme.neutral,
                    borderRadius: BorderRadius.circular(2.5),
                  ),
                ),
              ),

              Expanded(
                child: ListView(
                  controller: scrollController,
                  padding: const EdgeInsets.fromLTRB(20, 8, 20, 24),
                  children: [
                    // Hero Image
                    if (h.imageUrl != null)
                      ClipRRect(
                        borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                        child: SizedBox(
                          height: 210,
                          width: double.infinity,
                          child: Image.network(
                            h.imageUrl!,
                            fit: BoxFit.cover,
                            errorBuilder: (ctx, err, stack) => Container(
                              color: AppTheme.neutral,
                              child: const Icon(Icons.hotel, size: 50, color: AppTheme.inkMuted),
                            ),
                          ),
                        ),
                      ),
                    const SizedBox(height: 16),

                    // Title & Rating
                    Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text(
                                h.name,
                                style: AppTheme.displayFont(
                                  fontSize: 22,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                              const SizedBox(height: 4),
                              Text(
                                "${h.zone.replaceAll('_', ' ')} · ${h.address}",
                                style: AppTheme.bodyFont(
                                  fontSize: 13,
                                  color: AppTheme.inkMuted,
                                ),
                              ),
                            ],
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                          decoration: BoxDecoration(
                            color: AppTheme.yellowLight,
                            border: Border.all(color: AppTheme.yellow),
                            borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                          ),
                          child: Row(
                            children: [
                              const Icon(Icons.star, color: AppTheme.ink, size: 16),
                              const SizedBox(width: 4),
                              Text(
                                "${h.rating}",
                                style: AppTheme.displayFont(
                                  fontSize: 14,
                                  fontWeight: FontWeight.w800,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),

                    // Real-Time Capacity & Pressure Bar
                    Container(
                      padding: const EdgeInsets.all(16),
                      decoration: BoxDecoration(
                        color: AppTheme.paper,
                        borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                        border: Border.all(color: AppTheme.neutral),
                      ),
                      child: Column(
                        children: [
                          Row(
                            mainAxisAlignment: MainAxisAlignment.spaceAround,
                            children: [
                              _buildMetricItem("Usable Rooms", "${h.usableRooms}", "of ${h.availableRooms} avail", AppTheme.ink),
                              Container(width: 1, height: 36, color: AppTheme.neutral),
                              _buildMetricItem("Venue Transit", "${h.travelTimeToVenue} min", "Direct shuttle", AppTheme.green),
                              Container(width: 1, height: 36, color: AppTheme.neutral),
                              _buildMetricItem("Occupancy", "${h.pressure}%", h.pressureLevel.name, isZoneC ? AppTheme.green : AppTheme.red),
                            ],
                          ),
                          const SizedBox(height: 12),
                          PressureBar(percentage: h.pressure, height: 6),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),

                    // Match Day Transit & Shuttle Badge
                    Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: isZoneC ? AppTheme.yellowLight : AppTheme.white,
                        borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                        border: Border.all(
                          color: isZoneC ? AppTheme.yellow : AppTheme.neutral,
                          width: 1.2,
                        ),
                      ),
                      child: Row(
                        children: [
                          const Icon(Icons.airport_shuttle, size: 24, color: AppTheme.ink),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  "MATCH DAY CONNECTIVITY",
                                  style: AppTheme.metaText(fontSize: 10, fontWeight: FontWeight.w800),
                                ),
                                const SizedBox(height: 2),
                                Text(
                                  h.shuttleInfo,
                                  style: AppTheme.displayFont(fontSize: 13, fontWeight: FontWeight.w700),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 20),

                    // Hotel Amenities
                    Text(
                      "PROPERTY HIGHLIGHTS & AMENITIES",
                      style: AppTheme.metaText(fontSize: 11, color: AppTheme.inkMuted),
                    ),
                    const SizedBox(height: 10),

                    Wrap(
                      spacing: 8,
                      runSpacing: 8,
                      children: h.amenities.map((a) {
                        return Container(
                          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                          decoration: BoxDecoration(
                            color: AppTheme.white,
                            borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                            border: Border.all(color: AppTheme.neutral),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              Container(
                                padding: const EdgeInsets.all(4),
                                decoration: const BoxDecoration(
                                  color: AppTheme.paperDark,
                                  shape: BoxShape.circle,
                                ),
                                child: Icon(_getAmenityIcon(a.name), size: 14, color: AppTheme.ink),
                              ),
                              const SizedBox(width: 8),
                              Text(
                                a.name,
                                style: AppTheme.bodyFont(fontSize: 12, fontWeight: FontWeight.w600),
                              ),
                            ],
                          ),
                        );
                      }).toList(),
                    ),
                    const SizedBox(height: 20),

                    // Room Selection Options
                    Text(
                      "AVAILABLE ROOM TYPES FOR MATCH NIGHT",
                      style: AppTheme.metaText(fontSize: 11, color: AppTheme.inkMuted),
                    ),
                    const SizedBox(height: 10),

                    ...List.generate(h.roomTypes.length, (idx) {
                      final room = h.roomTypes[idx];
                      final isSelected = _selectedRoomIndex == idx;

                      return GestureDetector(
                        onTap: () => setState(() => _selectedRoomIndex = idx),
                        child: Container(
                          margin: const EdgeInsets.only(bottom: 10),
                          padding: const EdgeInsets.all(14),
                          decoration: BoxDecoration(
                            color: isSelected ? AppTheme.paper : AppTheme.white,
                            borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                            border: Border.all(
                              color: isSelected ? AppTheme.ink : AppTheme.neutral,
                              width: isSelected ? 1.5 : 1.0,
                            ),
                          ),
                          child: Row(
                            children: [
                              Icon(
                                isSelected ? Icons.radio_button_checked : Icons.radio_button_off,
                                color: isSelected ? AppTheme.ink : AppTheme.inkMuted,
                                size: 20,
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Row(
                                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                      children: [
                                        Text(
                                          room.name,
                                          style: AppTheme.displayFont(
                                            fontSize: 14,
                                            fontWeight: FontWeight.w700,
                                          ),
                                        ),
                                        Text(
                                          room.price,
                                          style: AppTheme.displayFont(
                                            fontSize: 14,
                                            fontWeight: FontWeight.w800,
                                            color: AppTheme.ink,
                                          ),
                                        ),
                                      ],
                                    ),
                                    const SizedBox(height: 3),
                                    Text(
                                      "${room.bedType} · ${room.perks}",
                                      style: AppTheme.bodyFont(fontSize: 12, color: AppTheme.inkMuted),
                                    ),
                                  ],
                                ),
                              ),
                            ],
                          ),
                        ),
                      );
                    }),
                  ],
                ),
              ),

              // Pinned Bottom Actions Bar
              Container(
                padding: EdgeInsets.fromLTRB(
                  20,
                  12,
                  20,
                  MediaQuery.of(context).padding.bottom + 16,
                ),
                decoration: BoxDecoration(
                  color: AppTheme.white,
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.06),
                      blurRadius: 10,
                      offset: const Offset(0, -4),
                    ),
                  ],
                  border: const Border(
                    top: BorderSide(color: AppTheme.neutral),
                  ),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () {
                          final double lat = h.latitude ?? 18.9272;
                          final double lng = h.longitude ?? 72.8205;
                          final mapDest = MapDestination(
                            id: h.id,
                            name: h.name,
                            address: h.address,
                            location: LatLng(lat, lng),
                            type: DestinationType.HOTEL,
                          );

                          Navigator.pop(context); // Close bottom modal
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (_) => DestinationRouteScreen(
                                destination: mapDest,
                                origin: MapDestination.wankhedeStadium,
                                appState: widget.appState,
                              ),
                            ),
                          );
                        },
                        icon: const Icon(Icons.directions, size: 18),
                        label: const Text("Transit Route"),
                        style: OutlinedButton.styleFrom(
                          foregroundColor: AppTheme.ink,
                          side: const BorderSide(color: AppTheme.ink, width: 1.5),
                          padding: const EdgeInsets.symmetric(vertical: 14),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                          ),
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: ElevatedButton.icon(
                        onPressed: () async {
                          final url = h.bookingUrl;
                          if (url == null || url.isEmpty) {
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text("Official booking link unavailable for this simulated property."),
                              ),
                            );
                            return;
                          }
                          final opened = await MapsLauncherService.openExternalUrl(url);
                          if (context.mounted) {
                            if (opened) {
                              Navigator.pop(context);
                            } else {
                              ScaffoldMessenger.of(context).showSnackBar(
                                const SnackBar(
                                  content: Text("Unable to open hotel booking website."),
                                ),
                              );
                            }
                          }
                        },
                        icon: const Icon(Icons.open_in_new_rounded, size: 18),
                        label: const Text("Book Room"),
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppTheme.yellow,
                          foregroundColor: AppTheme.ink,
                          elevation: 0,
                          padding: const EdgeInsets.symmetric(vertical: 14),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildMetricItem(String label, String value, String subtext, Color valColor) {
    return Column(
      children: [
        Text(
          value,
          style: AppTheme.displayFont(
            fontSize: 16,
            fontWeight: FontWeight.w800,
            color: valColor,
          ),
        ),
        Text(
          label,
          style: AppTheme.metaText(fontSize: 10, color: AppTheme.ink),
        ),
        Text(
          subtext,
          style: AppTheme.bodyFont(fontSize: 9, color: AppTheme.inkMuted),
        ),
      ],
    );
  }
}

