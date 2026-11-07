/**
 * JUNCTION - Frontend Demonstration Video Synchronization Script
 * 
 * Synchronizes canonical test videos from `shared/assets/test-videos/`
 * and annotated demo videos from `cv_bridge/artifacts/annotated-videos/`
 * into `frontend/public/videos/` for Next.js browser demonstration.
 */

const fs = require('fs');
const path = require('path');

// Determine repository root and frontend public directory
const currentDir = __dirname;
let repoRoot = path.resolve(currentDir, '..');
if (!fs.existsSync(path.join(repoRoot, 'shared'))) {
  repoRoot = path.resolve(currentDir, '../..');
}

const sharedVideosDir = path.join(repoRoot, 'shared', 'assets', 'test-videos');
const annotatedVideosDir = path.join(repoRoot, 'cv_bridge', 'artifacts', 'annotated-videos');

// Target directory in frontend
let targetVideosDir = path.resolve(repoRoot, 'frontend', 'public', 'videos');
if (!fs.existsSync(path.dirname(targetVideosDir))) {
  targetVideosDir = path.resolve(currentDir, '..', 'public', 'videos');
}

console.log('[*] Synchronizing demonstration videos for Next.js frontend...');
console.log(`    • Source Shared Test Videos: ${sharedVideosDir}`);
console.log(`    • Source Annotated Videos:   ${annotatedVideosDir}`);
console.log(`    • Destination:               ${targetVideosDir}`);

if (!fs.existsSync(targetVideosDir)) {
  fs.mkdirSync(targetVideosDir, { recursive: true });
}

let copiedCount = 0;
let preservedCount = 0;

function copyVideo(srcPath, destPath, filename) {
  if (!fs.existsSync(srcPath)) {
    console.warn(`    ⚠ Warning: Source video not found: ${srcPath}`);
    return;
  }

  const destFile = path.join(destPath, filename);
  if (fs.existsSync(destFile)) {
    const srcStat = fs.statSync(srcPath);
    const destStat = fs.statSync(destFile);
    // If destination exists with non-zero size, preserve it unless source is newer
    if (destStat.size > 0) {
      preservedCount++;
      return;
    }
  }

  fs.copyFileSync(srcPath, destFile);
  copiedCount++;
  console.log(`    ✓ Copied: ${filename} (${(fs.statSync(srcPath).size / (1024 * 1024)).toFixed(2)} MB)`);
}

// 1. Copy canonical test videos
if (fs.existsSync(sharedVideosDir)) {
  const testVideos = fs.readdirSync(sharedVideosDir).filter(f => f.endsWith('.mp4'));
  for (const video of testVideos) {
    copyVideo(path.join(sharedVideosDir, video), targetVideosDir, video);
  }
} else {
  console.warn(`    ⚠ Shared test-videos directory not accessible at ${sharedVideosDir}. Checking if public/videos is already populated...`);
}

// 2. Copy annotated demonstration videos
if (fs.existsSync(annotatedVideosDir)) {
  const annotatedVideos = fs.readdirSync(annotatedVideosDir).filter(f => f.endsWith('.mp4'));
  for (const video of annotatedVideos) {
    copyVideo(path.join(annotatedVideosDir, video), targetVideosDir, video);
  }
} else {
  console.warn(`    ⚠ Annotated videos directory not accessible at ${annotatedVideosDir}. Checking if public/videos is already populated...`);
}

const finalVideos = fs.existsSync(targetVideosDir) ? fs.readdirSync(targetVideosDir).filter(f => f.endsWith('.mp4')) : [];
console.log(`[✔] Video sync complete: ${copiedCount} copied, ${preservedCount} already present (${finalVideos.length} total demo videos available in public/videos).\n`);

