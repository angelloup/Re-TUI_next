#!/usr/bin/env bash
set -euo pipefail

PKG="${1:-com.dvil.tui_renewed}"
DURATION_SECONDS="${2:-300}"
INTERVAL_SECONDS="${3:-10}"
OUT_DIR="${4:-perf-results}"
ADB_SERIAL="${ANDROID_SERIAL:-}"
ADB_BIN="${ADB:-adb}"
if ! command -v "$ADB_BIN" >/dev/null 2>&1 && [ -x "$HOME/Library/Android/sdk/platform-tools/adb" ]; then
  ADB_BIN="$HOME/Library/Android/sdk/platform-tools/adb"
fi
ADB=("$ADB_BIN")
if [ -n "$ADB_SERIAL" ]; then
  ADB=("$ADB_BIN" -s "$ADB_SERIAL")
fi

mkdir -p "$OUT_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"
OUT="$OUT_DIR/idle-${STAMP}.tsv"
META="$OUT_DIR/idle-${STAMP}.txt"

"${ADB[@]}" shell dumpsys batterystats --reset >/dev/null 2>&1 || true
"${ADB[@]}" shell am force-stop "$PKG" >/dev/null 2>&1 || true
"${ADB[@]}" shell monkey -p "$PKG" 1 >/dev/null
sleep 8

PID="$("${ADB[@]}" shell pidof "$PKG" | tr -d '\r' || true)"
if [ -z "$PID" ]; then
  echo "Package is not running: $PKG" >&2
  exit 1
fi

{
  echo "package=$PKG"
  echo "pid=$PID"
  echo "serial=${ADB_SERIAL:-default}"
  echo "duration_seconds=$DURATION_SECONDS"
  echo "interval_seconds=$INTERVAL_SECONDS"
  echo "started_at=$(date '+%Y-%m-%dT%H:%M:%S%z')"
  "${ADB[@]}" shell dumpsys deviceidle get deep 2>/dev/null | sed 's/^/deviceidle_deep=/'
} > "$META"

echo -e "elapsed_s\tpid\tcpu_pct\trss_kb\tpss_kb\tjava_heap_kb\tnative_heap_kb\tthreads\tfd_count" > "$OUT"

to_kb() {
  case "$1" in
    *G) awk "BEGIN {printf \"%.0f\", ${1%G} * 1024 * 1024}" ;;
    *M) awk "BEGIN {printf \"%.0f\", ${1%M} * 1024}" ;;
    *K) awk "BEGIN {printf \"%.0f\", ${1%K}}" ;;
    *) printf '%s' "${1:-0}" ;;
  esac
}

END=$((SECONDS + DURATION_SECONDS))
while [ "$SECONDS" -lt "$END" ]; do
  ELAPSED=$((DURATION_SECONDS - (END - SECONDS)))
  TOP_LINE="$("${ADB[@]}" shell top -b -n 1 -p "$PID" 2>/dev/null | tr -d '\r' | awk -v pid="$PID" '$1 == pid {print; exit}')"
  CPU="$(printf '%s\n' "$TOP_LINE" | awk '{print $9; exit}')"
  RSS_RAW="$(printf '%s\n' "$TOP_LINE" | awk '{print $6; exit}')"
  RSS="$(to_kb "$RSS_RAW")"

  MEM="$("${ADB[@]}" shell dumpsys meminfo "$PKG" 2>/dev/null | tr -d '\r')"
  PSS="$(printf '%s\n' "$MEM" | awk '/TOTAL PSS:/ {print $3; exit}')"
  JAVA_HEAP="$(printf '%s\n' "$MEM" | awk '/Java Heap:/ {print $3; exit}')"
  NATIVE_HEAP="$(printf '%s\n' "$MEM" | awk '/Native Heap:/ {print $3; exit}')"

  THREADS="$("${ADB[@]}" shell ls "/proc/$PID/task" 2>/dev/null | wc -l | tr -d ' ' || true)"
  FDS="$("${ADB[@]}" shell ls "/proc/$PID/fd" 2>/dev/null | wc -l | tr -d ' ' || true)"

  echo -e "${ELAPSED}\t${PID}\t${CPU:-0}\t${RSS:-0}\t${PSS:-0}\t${JAVA_HEAP:-0}\t${NATIVE_HEAP:-0}\t${THREADS:-0}\t${FDS:-0}" >> "$OUT"
  sleep "$INTERVAL_SECONDS"
done

{
  echo
  echo "ended_at=$(date '+%Y-%m-%dT%H:%M:%S%z')"
  echo
  "${ADB[@]}" shell dumpsys batterystats "$PKG" 2>/dev/null || true
} >> "$META"

echo "Wrote:"
echo "$OUT"
echo "$META"
