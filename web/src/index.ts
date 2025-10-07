export function formatFps(msPerFrame: number): string {
  if (msPerFrame <= 0) return "-";
  const fps = 1000 / msPerFrame;
  return fps.toFixed(1);
}

function main() {
  const img = document.getElementById("frame") as HTMLImageElement | null;
  const stats = document.getElementById("stats") as HTMLDivElement | null;
  if (!img || !stats) return;

  // Dummy stats for now
  const resolution = "1280x720";
  const fps = formatFps(66.7);
  stats.textContent = `FPS: ${fps} | Resolution: ${resolution}`;
}

document.addEventListener("DOMContentLoaded", main);
