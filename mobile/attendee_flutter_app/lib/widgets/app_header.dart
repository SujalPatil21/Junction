import 'package:flutter/material.dart';
import '../state/app_state.dart';
import '../theme/app_theme.dart';
import 'pill_badge.dart';
import 'motion_tap.dart';
import 'profile_modal.dart';

class AppHeader extends StatelessWidget {
  final AppState appState;

  const AppHeader({super.key, required this.appState});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: const BoxDecoration(
        color: AppTheme.paper,
        border: Border(
          bottom: BorderSide(color: AppTheme.neutral, width: 1.0),
        ),
      ),
      child: SafeArea(
        bottom: false,
        child: Row(
          children: [
            Text(
              "JUNCTION",
              style: AppTheme.displayFont(
                fontSize: 16,
                fontWeight: FontWeight.w800,
                letterSpacing: -0.5,
              ),
            ),
            const SizedBox(width: 6),
            const PillBadge(
              text: "● LIVE",
              variant: PillVariant.live,
              fontSize: 9,
              padding: EdgeInsets.symmetric(horizontal: 5, vertical: 2),
            ),
            const Spacer(),
            // BARCODE SCANNER BUTTON (Top Right)
            MotionTap(
              onTap: () => _showBarcodeScannerModal(context, appState),
              scaleDown: 0.90,
              child: Container(
                padding: const EdgeInsets.all(6),
                margin: const EdgeInsets.only(right: 8),
                decoration: BoxDecoration(
                  color: AppTheme.yellowLight,
                  borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                  border: Border.all(color: AppTheme.yellow, width: 1.2),
                  boxShadow: AppTheme.shadowSm,
                ),
                child: const Icon(
                  Icons.qr_code_scanner,
                  size: 16,
                  color: AppTheme.ink,
                ),
              ),
            ),
            // Attendee Profile Button
            MotionTap(
              onTap: () => ProfileModal.show(context, appState),
              scaleDown: 0.90,
              child: Container(
                width: 28,
                height: 28,
                decoration: BoxDecoration(
                  color: AppTheme.ink,
                  shape: BoxShape.circle,
                  border: Border.all(color: AppTheme.yellow, width: 1.5),
                  boxShadow: AppTheme.shadowSm,
                ),
                alignment: Alignment.center,
                child: Text(
                  (appState.currentUser?.name.isNotEmpty == true)
                      ? appState.currentUser!.name.substring(0, 1).toUpperCase()
                      : "A",
                  style: AppTheme.displayFont(
                    fontSize: 12,
                    fontWeight: FontWeight.w900,
                    color: AppTheme.yellow,
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  static void _showBarcodeScannerModal(BuildContext context, AppState appState) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) => _BarcodeScannerSheet(appState: appState),
    );
  }
}

class _BarcodeScannerSheet extends StatefulWidget {
  final AppState appState;

  const _BarcodeScannerSheet({required this.appState});

  @override
  State<_BarcodeScannerSheet> createState() => _BarcodeScannerSheetState();
}

class _BarcodeScannerSheetState extends State<_BarcodeScannerSheet> {
  late TextEditingController _codeController;
  bool _isSaved = false;

  @override
  void initState() {
    super.initState();
    _codeController = TextEditingController(
      text: widget.appState.scannedBarcode ?? "8901234567890",
    );
  }

  @override
  void dispose() {
    _codeController.dispose();
    super.dispose();
  }

  void _saveBarcode() {
    final code = _codeController.text.trim();
    if (code.isNotEmpty) {
      widget.appState.setScannedBarcode(code);
      setState(() => _isSaved = true);
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text("Barcode Scanned & Saved: $code"),
          backgroundColor: AppTheme.green,
          duration: const Duration(seconds: 2),
          behavior: SnackBarBehavior.floating,
        ),
      );
      Future.delayed(const Duration(milliseconds: 600), () {
        if (mounted) Navigator.pop(context);
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: EdgeInsets.only(
        bottom: MediaQuery.of(context).viewInsets.bottom,
      ),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: const BoxDecoration(
          color: AppTheme.white,
          borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Center(
              child: Container(
                width: 40,
                height: 4,
                decoration: BoxDecoration(
                  color: AppTheme.neutral,
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            const SizedBox(height: 16),

            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  "BARCODE SCANNER",
                  style: AppTheme.displayFont(
                    fontSize: 16,
                    fontWeight: FontWeight.w800,
                  ),
                ),
                const Icon(Icons.qr_code_scanner, color: AppTheme.ink),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              "Scan or enter numeric ticket barcode value below.",
              style: AppTheme.bodyFont(fontSize: 12, color: AppTheme.inkMuted),
            ),
            const SizedBox(height: 16),

            // Scanner Viewfinder Mock Container
            Container(
              width: double.infinity,
              height: 120,
              decoration: BoxDecoration(
                color: AppTheme.ink,
                borderRadius: BorderRadius.circular(AppTheme.radiusMd),
                border: Border.all(color: AppTheme.yellow, width: 2),
              ),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.crop_free, color: AppTheme.yellow, size: 40),
                  const SizedBox(height: 6),
                  Text(
                    "ALIGN BARCODE INSIDE FRAME",
                    style: AppTheme.metaText(
                      fontSize: 10,
                      color: AppTheme.yellow,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),

            // Barcode Numeric Input Field
            Text(
              "BARCODE NUMERIC VALUE",
              style: AppTheme.metaText(fontSize: 10, color: AppTheme.inkFaint),
            ),
            const SizedBox(height: 6),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 2),
              decoration: BoxDecoration(
                color: AppTheme.paper,
                borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                border: Border.all(color: AppTheme.neutral),
              ),
              child: TextField(
                controller: _codeController,
                keyboardType: TextInputType.number,
                style: AppTheme.displayFont(
                  fontSize: 16,
                  fontWeight: FontWeight.w800,
                  letterSpacing: 1.5,
                  color: AppTheme.ink,
                ),
                decoration: const InputDecoration(
                  icon: Icon(Icons.numbers, size: 18, color: AppTheme.inkMuted),
                  hintText: "e.g. 8901234567890",
                  border: InputBorder.none,
                ),
              ),
            ),
            const SizedBox(height: 18),

            // Save / Scan Action Button
            MotionTap(
              onTap: _saveBarcode,
              scaleDown: 0.96,
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.symmetric(vertical: 14),
                decoration: BoxDecoration(
                  color: _isSaved ? AppTheme.green : AppTheme.yellow,
                  borderRadius: BorderRadius.circular(AppTheme.radiusSm),
                  boxShadow: AppTheme.shadowSm,
                ),
                child: Center(
                  child: Text(
                    _isSaved ? "BARCODE SAVED ✓" : "SAVE BARCODE NUMERIC VALUE",
                    style: AppTheme.displayFont(
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                      color: _isSaved ? AppTheme.white : AppTheme.ink,
                      letterSpacing: 0.3,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
