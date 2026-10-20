#!/usr/bin/env bash
# Wall-clock benchmarks for the multi-species expression calls endpoint.
#
# Measures:
#   D1  cold vs warm (controller count + result caches; loader not built on hit)
#   D2  combined get_results+get_result_count vs count-only then results-only
#       (in-loader memoization vs two separate loader constructions)
#   D3  scaling: 3-gene vs 9-gene lists; small vs large limit on results-only
#
# Requires: a running bgee-webapp instance connected to a Bgee database, curl, jq.
#
# Usage:
#   BASE_URL=http://localhost:8080/bgee-webapp ./multispec_expr_calls_bench.sh
#
# For a true cold D1/D2, restart Tomcat before running. Cache keys are the
# SimilarityExpressionCallFilter (genes, quality, condition filters) plus
# offset/limit for result pages. Dummy URL parameters do not bust the cache.
# If a "cold" request finishes in under ${COLD_WARN_S}s, the script warns that
# the key was likely already warm.
#
# Optional environment:
#   TIMEOUT       curl timeout in seconds (default 600)
#   WARM_REPEATS  extra warm requests after the first hit (default 3)
#   SKIP_D3       set to 1 to skip the slower 9-gene / large-limit scenarios
#   COLD_WARN_S   warn if a labelled-cold request is faster than this (default 3)

set -u

BASE_URL="${BASE_URL:-http://localhost:8080/bgee-webapp}"
ENDPOINT="${BASE_URL}/?page=data&action=multispec_expr_calls&display_type=json"
TIMEOUT="${TIMEOUT:-600}"
WARM_REPEATS="${WARM_REPEATS:-3}"
SKIP_D3="${SKIP_D3:-0}"
COLD_WARN_S="${COLD_WARN_S:-3}"
WORKDIR="$(mktemp -d)"
trap 'rm -rf "${WORKDIR}"' EXIT

# Human HBB + APOC1, mouse Hbb-bs + Apoe.
GENES_4="ENSG00000244734%0D%0AENSG00000130208%0D%0AENSMUSG00000052187%0D%0AENSMUSG00000040564"
# Human GATA3 + HOXD12, mouse Gata3 + Hoxd12 (same size, different cache key).
GENES_4B="ENSG00000139767%0D%0AENSG00000170178%0D%0AENSMUSG00000063919%0D%0AENSMUSG00000001823"
# Zebrafish hoxd12a, human HOXD12, mouse Hoxd12 (endpoint docs example).
GENES_3="ENSDARG00000059263%0D%0AENSG00000170178%0D%0AENSMUSG00000001823"
# Nine-species list used by the service-level integration test.
GENES_9="ENSG00000139767%0D%0AENSMUSG00000063919%0D%0AENSPPAG00000028134%0D%0AENSRNOG00000001141%0D%0AENSSSCG00000009845%0D%0AENSECAG00000021729%0D%0AENSCAFG00000023113%0D%0AENSOCUG00000004503%0D%0AENSACAG00000004139"

SUMMARY="anat_entity_id=SUMMARY&cell_type_id=SUMMARY"

# scenario | label | seconds | http | count | n
RESULTS="${WORKDIR}/results.tsv"
: > "${RESULTS}"

request() { # request <output-file> <query-suffix> -> "HTTPCODE TIME"
    curl -s --max-time "${TIMEOUT}" -o "$1" \
        -w "%{http_code} %{time_total}" \
        "${ENDPOINT}&$2"
}

record() { # record <scenario> <label> <http> <time> <json-file>
    local count n
    count=$(jq -r '.data.expressionCallCount // "—"' "$5" 2>/dev/null || echo "—")
    n=$(jq -r '.data.expressionData.expressionCalls | length // 0' "$5" 2>/dev/null || echo 0)
    printf '%s\t%s\t%s\t%s\t%s\t%s\n' "$1" "$2" "$4" "$3" "${count}" "${n}" >> "${RESULTS}"
    printf '  %-8s  HTTP %s  %8ss  count=%s  n=%s\n' "$2" "$3" "$4" "${count}" "${n}"
    if [ "$2" = "cold" ]; then
        awk -v t="$4" -v w="${COLD_WARN_S}" 'BEGIN {
            if (t + 0 < w) {
                printf "  warning: labelled-cold request finished in %.3fs (< %ss); cache key was likely already warm\n", t, w
            }
        }'
    fi
}

run_once() { # run_once <scenario> <label> <query-suffix>
    local out="${WORKDIR}/$1_$2.json"
    local http time
    read -r http time <<< "$(request "${out}" "$3")"
    if [ "${http}" != "200" ]; then
        echo "  ERROR: HTTP ${http} for $1/$2 (see ${out})"
        jq -r '.message // empty' "${out}" 2>/dev/null | sed 's/^/    /'
    fi
    record "$1" "$2" "${http}" "${time}" "${out}"
}

run_warm_repeats() { # run_warm_repeats <scenario> <query-suffix>
    local i
    for i in $(seq 1 "${WARM_REPEATS}"); do
        run_once "$1" "warm_${i}" "$2"
    done
}

echo "Target: ${ENDPOINT}"
echo "Timeout: ${TIMEOUT}s  warm repeats: ${WARM_REPEATS}"
echo

echo "=== D1: cold vs warm (3-gene HOXD12, SILVER, SUMMARY, limit=20) ==="
echo "    Controller caches: miss on first request, hit on repeats (restart Tomcat for a true miss)."
D1_Q="get_results=1&get_result_count=1&limit=20&data_qual=SILVER&${SUMMARY}&gene_list=${GENES_3}"
run_once D1 cold "${D1_Q}"
run_warm_repeats D1 "${D1_Q}"
echo

echo "=== D2a: combined count+results in one request (4-gene HBB, BRONZE, SUMMARY) ==="
echo "    One loader on miss: loadDataCount() materializes the list, loadData() reuses it."
D2A_Q="get_results=1&get_result_count=1&limit=20&data_qual=BRONZE&${SUMMARY}&gene_list=${GENES_4}"
run_once D2a cold "${D2A_Q}"
run_warm_repeats D2a "${D2A_Q}"
echo

echo "=== D2b: count-only then results-only (4-gene alt, BRONZE, SUMMARY) ==="
echo "    Two HTTP requests, two loaders on miss: count caches the total; results rebuild the loader."
D2B_COUNT="get_result_count=1&data_qual=BRONZE&${SUMMARY}&gene_list=${GENES_4B}"
D2B_RESULTS="get_results=1&limit=20&data_qual=BRONZE&${SUMMARY}&gene_list=${GENES_4B}"
run_once D2b_count cold "${D2B_COUNT}"
run_once D2b_results cold "${D2B_RESULTS}"
run_once D2b_count warm_1 "${D2B_COUNT}"
run_once D2b_results warm_1 "${D2B_RESULTS}"
echo

if [ "${SKIP_D3}" != "1" ]; then
    echo "=== D3a: 9-gene list, SILVER, SUMMARY, count+results, limit=20 ==="
    D3A_Q="get_results=1&get_result_count=1&limit=20&data_qual=SILVER&${SUMMARY}&gene_list=${GENES_9}"
    run_once D3a cold "${D3A_Q}"
    run_once D3a warm_1 "${D3A_Q}"
    echo

    echo "=== D3b: results-only, small vs large limit (3-gene HOXD12, GOLD, SUMMARY) ==="
    echo "    No get_result_count, so a small limit can stop after filling the page (lazy path)."
    D3B_SMALL="get_results=1&limit=20&data_qual=GOLD&${SUMMARY}&gene_list=${GENES_3}"
    D3B_LARGE="get_results=1&limit=10000&data_qual=GOLD&${SUMMARY}&gene_list=${GENES_3}"
    run_once D3b_limit20 cold "${D3B_SMALL}"
    run_once D3b_limit20 warm_1 "${D3B_SMALL}"
    run_once D3b_limit10000 cold "${D3B_LARGE}"
    run_once D3b_limit10000 warm_1 "${D3B_LARGE}"
    echo
else
    echo "=== D3 skipped (SKIP_D3=1) ==="
    echo
fi

echo "=== Timing table ==="
printf '%-16s %-12s %10s %6s %8s %6s\n' "scenario" "label" "seconds" "http" "count" "n"
printf '%-16s %-12s %10s %6s %8s %6s\n' "----------------" "------------" "----------" "------" "--------" "------"
awk -F '\t' '{ printf "%-16s %-12s %10s %6s %8s %6s\n", $1, $2, $3, $4, $5, $6 }' "${RESULTS}"
echo
echo "Reading the table:"
echo "  The first warm after a miss is often still slow (JVM/DB); use warm_2+ as the hit baseline."
echo "  D1 cold >> D1 warm_2+         loader skipped on cache hit (~1s gene/LCA/taxon tree)"
echo "  D2a cold vs D2b_count+results cold   combined (one loader) should beat two separate misses"
echo "  D3a cold                      larger gene list; dominated by count (full pass)"
echo "  D3b_limit20 vs limit10000     lazy page fill vs scanning the rest (results-only;"
echo "                                difference is small when the full set already fits in limit=20)"
