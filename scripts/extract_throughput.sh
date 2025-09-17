#!/usr/bin/env bash
set -euo pipefail

# Usage:
#   bash scripts/extract_throughput.sh                  # 在当前目录自动查找：*threads.zip / *threads.tar.gz / *threads/
#   bash scripts/extract_throughput.sh "$REPORT_DIR"    # 直接处理某个报告目录
#   bash scripts/extract_throughput.sh a.tar.gz b.zip   # 任意多个输入

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "ERROR: '$1' is required" >&2; exit 1; }
}
require_cmd jq
# unzip/tar 只有在需要解压时才要求存在
have_unzip() { command -v unzip >/dev/null 2>&1; }
have_tar()   { command -v tar   >/dev/null 2>&1; }

# 记录由本脚本创建的临时解压目录，退出时清理
created_dirs=()
cleanup() {
  for d in "${created_dirs[@]:-}"; do
    [ -n "$d" ] && [ -d "$d" ] && rm -rf "$d" || true
  done
}
trap cleanup EXIT

# 将一个输入(目录/zip/tar.gz)变成一个或多个“*threads/”目录，加入全局数组
threads_dirs=()

add_threads_dir() {
  local d="$1"
  # 统一成以 / 结尾的目录路径
  d="${d%/}/"
  if [ -d "$d" ]; then
    threads_dirs+=("$d")
  fi
}

# 解压 zip 到同名目录
unpack_zip_to_dir() {
  local zip="$1"
  have_unzip || { echo "ERROR: unzip not found but required to extract $zip" >&2; exit 1; }
  local out="${zip%.zip}"
  if [ ! -d "$out" ]; then
    unzip -q "$zip" -d "$out"
    created_dirs+=("$out")
  fi
  add_threads_dir "$out"
}

# 解压 tar.gz 到同名目录
unpack_tar_to_dir() {
  local tarfile="$1"
  have_tar || { echo "ERROR: tar not found but required to extract $tarfile" >&2; exit 1; }
  local out="${tarfile%.tar.gz}"
  mkdir -p "$out"
  tar -xzf "$tarfile" -C "$out"
  created_dirs+=("$out")
  add_threads_dir "$out"
}

# 从一个“*threads/”目录中查找统计文件并打印一行 “<thread> <throughput>”
emit_one_dir() {
  local dir="$1"
  # 抓线程数：从目录名里提取 123threads 的 123
  local base tcount
  base="$(basename "$dir")"
  if ! tcount="$(echo "$base" | sed -E 's/.*[^0-9]([0-9]+)threads.*/\1/')" || [[ -z "${tcount// }" ]]; then
    # 如果目录名没有包含线程数，尝试从父目录名取
    base="$(basename "$(dirname "$dir")")"
    tcount="$(echo "$base" | sed -E 's/.*[^0-9]([0-9]+)threads.*/\1/')" || true
  fi
  [ -z "${tcount// }" ] && tcount=0

  # 允许两种布局：
  # 1) 直接在该目录内部存在 reports/perf/statistics.json
  # 2) 更深层（例如 jmeter/…/reports/perf/statistics.json）
  local stats_file
  stats_file="$(find "$dir" -type f -path '*/reports/perf/statistics.json' | head -n 1 || true)"

  if [ -n "$stats_file" ] && [ -f "$stats_file" ]; then
    local tp
    tp="$(jq -r '.Total.throughput // empty' "$stats_file" 2>/dev/null || true)"
    if [ -n "$tp" ]; then
      # 保留两位小数
      printf "%d %.2f\n" "$tcount" "$tp"
    fi
  fi
}

# -------- 解析输入 --------
if [ "$#" -gt 0 ]; then
  # 显式输入：逐个处理
  for inpath in "$@"; do
    if [ -d "$inpath" ]; then
      # 直接是目录：可能就是 *threads/ 或包含 reports/perf/statistics.json
      add_threads_dir "$inpath"
    elif [[ "$inpath" == *.zip ]]; then
      unpack_zip_to_dir "$inpath"
    elif [[ "$inpath" == *.tar.gz ]]; then
      unpack_tar_to_dir "$inpath"
    else
      echo "WARN: skip unsupported input '$inpath'" >&2
    fi
  done
else
  # 无参：在当前目录猜测/搜索
  shopt -s nullglob
  found=0

  # 1) 先找 *threads.zip
  for z in *threads.zip; do
    unpack_zip_to_dir "$z"; found=1
  done

  # 2) 再找 *threads.tar.gz
  for t in *threads.tar.gz; do
    unpack_tar_to_dir "$t"; found=1
  done

  # 3) 最后直接找 *threads/ 目录
  for d in *threads/; do
    add_threads_dir "$d"; found=1
  done

  # 4) 如果以上都没有，但当前目录本身就是一个报告目录（含 statistics.json），也处理一下
  if [ "$found" -eq 0 ]; then
    if find . -maxdepth 2 -type f -path '*/reports/perf/statistics.json' | grep -q .; then
      add_threads_dir "."
      found=1
    fi
  fi

  if [ "$found" -eq 0 ]; then
    echo "ERROR: nothing found (no *threads.zip / *threads.tar.gz / *threads/ / reports/perf/statistics.json)" >&2
    exit 2
  fi
fi

# -------- 统计输出 --------
tmp_file="$(mktemp)"
for dir in "${threads_dirs[@]}"; do
  emit_one_dir "$dir" >>"$tmp_file"
done

# Markdown 表格输出
{
  echo '| 线程数 | TPS |'
  echo '| ------ | ---- |'
  sort -n "$tmp_file" | awk '!seen[$1]++ { printf("| %d | %.2f |\n", $1, $2) }'
}

rm -f "$tmp_file"