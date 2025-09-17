#!/usr/bin/env bash
set -euo pipefail

# 固定的 node_info 节点地址
NODE_INFO_URL="http://18.167.71.41:8231"

require_cmd() { command -v "$1" >/dev/null 2>&1 || { echo "ERROR: '$1' is required" >&2; exit 1; }; }
require_cmd jq
have_unzip() { command -v unzip >/dev/null 2>&1; }
have_tar()   { command -v tar   >/dev/null 2>&1; }

# -------------------- 退出清理 --------------------
created_dirs=()
cleanup() {
  for d in "${created_dirs[@]:-}"; do
    [ -n "$d" ] && [ -d "$d" ] && rm -rf "$d" || true
  done
}
trap cleanup EXIT

# -------------------- 收集 *threads/ 目录 --------------------
threads_dirs=()
add_threads_dir() { local d="${1%/}/"; [ -d "$d" ] && threads_dirs+=("$d"); }

unpack_zip_to_dir() {
  local zip="$1" out="${zip%.zip}"
  have_unzip || { echo "ERROR: unzip not found but required to extract $zip" >&2; exit 1; }
  if [ ! -d "$out" ]; then unzip -q "$zip" -d "$out"; created_dirs+=("$out"); fi
  add_threads_dir "$out"
}
unpack_tar_to_dir() {
  local tarfile="$1" out="${tarfile%.tar.gz}"
  have_tar || { echo "ERROR: tar not found but required to extract $tarfile" >&2; exit 1; }
  mkdir -p "$out"; tar -xzf "$tarfile" -C "$out"; created_dirs+=("$out"); add_threads_dir "$out"
}

emit_one_dir() {
  local dir="$1" base tcount stats_file tp
  base="$(basename "$dir")"
  if ! tcount="$(echo "$base" | sed -E 's/.*[^0-9]([0-9]+)threads.*/\1/')" || [[ -z "${tcount// }" ]]; then
    base="$(basename "$(dirname "$dir")")"
    tcount="$(echo "$base" | sed -E 's/.*[^0-9]([0-9]+)threads.*/\1/')" || true
  fi
  [ -z "${tcount// }" ] && tcount=0

  stats_file="$(find "$dir" -type f -path '*/reports/perf/statistics.json' | head -n 1 || true)"
  if [ -n "$stats_file" ] && [ -f "$stats_file" ]; then
    tp="$(jq -r '.Total.throughput // empty' "$stats_file" 2>/dev/null || true)"
    [ -n "$tp" ] && printf "%d %.2f\n" "$tcount" "$tp"
  fi
}

# -------------------- 解析输入（或自动发现） --------------------
if [ "$#" -gt 0 ]; then
  for inpath in "$@"; do
    if [ -d "$inpath" ]; then add_threads_dir "$inpath"
    elif [[ "$inpath" == *.zip ]]; then unpack_zip_to_dir "$inpath"
    elif [[ "$inpath" == *.tar.gz ]]; then unpack_tar_to_dir "$inpath"
    else echo "WARN: skip unsupported input '$inpath'" >&2; fi
  done
else
  shopt -s nullglob
  found=0
  for z in *threads.zip; do unpack_zip_to_dir "$z"; found=1; done
  for t in *threads.tar.gz; do unpack_tar_to_dir "$t"; found=1; done
  for d in *threads/; do add_threads_dir "$d"; found=1; done
  if [ "$found" -eq 0 ] && find . -maxdepth 2 -type f -path '*/reports/perf/statistics.json' | grep -q .; then
    add_threads_dir "."; found=1
  fi
  [ "$found" -eq 0 ] && { echo "ERROR: nothing found (no *threads.zip / *threads.tar.gz / *threads/ / reports/perf/statistics.json)" >&2; exit 2; }
fi

# -------------------- 打印版本抬头（固定 curl 命令） --------------------
require_cmd curl
curl -sS "$NODE_INFO_URL" \
  -H 'Content-Type: application/json' \
  -d '{
    "id": 1,
    "jsonrpc": "2.0",
    "method": "node_info",
    "params": []
  }' | jq -r '"\(.result.version) (\(.result.commit_hash))"'

# -------------------- 输出 Markdown 表格 --------------------
tmp_file="$(mktemp)"
for dir in "${threads_dirs[@]}"; do
  emit_one_dir "$dir" >>"$tmp_file"
done

{
  echo '| Threads | TPS |'
  echo '| ------- | --- |'
  sort -n "$tmp_file" | awk '!seen[$1]++ { printf("| %d | %.2f |\n", $1, $2) }'
}

rm -f "$tmp_file"