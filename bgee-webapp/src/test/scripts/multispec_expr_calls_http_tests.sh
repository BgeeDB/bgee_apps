#!/usr/bin/env bash
# HTTP-level smoke tests for the multi-species expression calls endpoint
# (?page=data&action=multispec_expr_calls).
#
# Requires: a running bgee-webapp instance (e.g. mvn cargo:run) connected to a Bgee
# database, plus curl and jq.
#
# Usage:
#   BASE_URL=http://localhost:8080/bgee-webapp ./multispec_expr_calls_http_tests.sh
#
# Notes:
# - The first run against a fresh instance is slow (cold caches); repeated runs are
#   fast, which also exercises the controller-level count/result caches.
# - Page requests use distinct cache keys, so each page is computed on first run.

set -u

BASE_URL="${BASE_URL:-http://localhost:8080/bgee-webapp}"
ENDPOINT="${BASE_URL}/?page=data&action=multispec_expr_calls&display_type=json"
TIMEOUT="${TIMEOUT:-600}"
WORKDIR="$(mktemp -d)"
trap 'rm -rf "${WORKDIR}"' EXIT

# Human HBB + APOC1, mouse Hbb-bs + Apoe (known orthologous pairs).
GENES_TWO_SPECIES="ENSG00000244734%0D%0AENSG00000130208%0D%0AENSMUSG00000052187%0D%0AENSMUSG00000040564"
GENES_ONE_SPECIES="ENSG00000244734%0D%0AENSG00000130208"
GENE_SINGLE="ENSG00000244734"
SUMMARY_FILTER="anat_entity_id=SUMMARY&cell_type_id=SUMMARY&data_qual=SILVER"

PASS=0
FAIL=0

pass() { PASS=$((PASS + 1)); echo "  PASS: $1"; }
fail() { FAIL=$((FAIL + 1)); echo "  FAIL: $1"; }

check() { # check <description> <condition-exit-code>
    if [ "$2" -eq 0 ]; then pass "$1"; else fail "$1"; fi
}

# request <output-file> <query-string-suffix> -> prints "HTTPCODE TIME"
request() {
    curl -s --max-time "${TIMEOUT}" -o "$1" \
        -w "%{http_code} %{time_total}" \
        "${ENDPOINT}&$2"
}

# Extracts a unique identity key per expression call item.
CALL_KEY_JQ='.gene.geneId + "|" + (.gene.species.id|tostring)
    + "|" + (.multiSpeciesCondition.anatEntities | map(.id) | join(","))
    + "|" + (.multiSpeciesCondition.cellTypes | map(.id) | join(","))'

echo "=== E2E-1: basic request (structure, count/results consistency) ==="
OUT="${WORKDIR}/c1.json"
read -r HTTP_CODE TIME <<< "$(request "${OUT}" \
    "get_results=1&get_result_count=1&limit=5&${SUMMARY_FILTER}&gene_list=${GENES_TWO_SPECIES}")"
echo "  (HTTP ${HTTP_CODE} in ${TIME}s)"
check "HTTP status is 200" "$([ "${HTTP_CODE}" = "200" ]; echo $?)"
check "JSON envelope code is 200, status SUCCESS" \
    "$(jq -e '.code == 200 and .status == "SUCCESS"' "${OUT}" > /dev/null; echo $?)"
check "speciesByTaxon tree present with root taxon" \
    "$(jq -e '.data.speciesByTaxon.taxon.id != null' "${OUT}" > /dev/null; echo $?)"
check "expressionCallCount is a positive number" \
    "$(jq -e '.data.expressionCallCount > 0' "${OUT}" > /dev/null; echo $?)"
check "result page size == min(limit, count)" \
    "$(jq -e '(.data.expressionData.expressionCalls | length)
        == ([5, .data.expressionCallCount] | min)' "${OUT}" > /dev/null; echo $?)"
check "items contain gene, condition, state, score, quality, data types" \
    "$(jq -e '.data.expressionData.expressionCalls | all(
        (.gene.geneId != null) and (.multiSpeciesCondition != null)
        and (.expressionState != null) and (.expressionScore != null)
        and (.expressionQuality != null) and (.dataTypesWithData != null))' \
        "${OUT}" > /dev/null; echo $?)"
check "both species present in speciesByTaxon leaves" \
    "$(jq -e '[.data.speciesByTaxon | recurse(.children[]?) | .species[]?.id]
        | contains([9606, 10090])' "${OUT}" > /dev/null; echo $?)"
COUNT=$(jq -r '.data.expressionCallCount' "${OUT}")

echo "=== E2E-2: paging consistency (count=${COUNT}, pageSize=13) ==="
PAGE_SIZE=13
FULL="${WORKDIR}/full.json"
read -r HTTP_CODE TIME <<< "$(request "${FULL}" \
    "get_results=1&get_result_count=1&limit=$(( COUNT > 10000 ? 10000 : COUNT ))&${SUMMARY_FILTER}&gene_list=${GENES_TWO_SPECIES}")"
echo "  (full fetch: HTTP ${HTTP_CODE} in ${TIME}s)"
jq -r ".data.expressionData.expressionCalls[] | ${CALL_KEY_JQ}" "${FULL}" \
    > "${WORKDIR}/full_keys.txt"
: > "${WORKDIR}/stitched_keys.txt"
STITCH_OK=0
OFFSET=0
while [ "${OFFSET}" -lt "${COUNT}" ]; do
    PAGE="${WORKDIR}/page_${OFFSET}.json"
    read -r HTTP_CODE TIME <<< "$(request "${PAGE}" \
        "get_results=1&get_result_count=1&limit=${PAGE_SIZE}&offset=${OFFSET}&${SUMMARY_FILTER}&gene_list=${GENES_TWO_SPECIES}")"
    echo "  (page offset=${OFFSET}: HTTP ${HTTP_CODE} in ${TIME}s)"
    if [ "${HTTP_CODE}" != "200" ]; then STITCH_OK=1; break; fi
    PAGE_COUNT=$(jq -r '.data.expressionCallCount' "${PAGE}")
    if [ "${PAGE_COUNT}" != "${COUNT}" ]; then
        echo "  (count drifted on page offset=${OFFSET}: ${PAGE_COUNT} != ${COUNT})"
        STITCH_OK=1
    fi
    jq -r ".data.expressionData.expressionCalls[] | ${CALL_KEY_JQ}" "${PAGE}" \
        >> "${WORKDIR}/stitched_keys.txt"
    OFFSET=$((OFFSET + PAGE_SIZE))
done
check "all pages returned HTTP 200 with stable count" "${STITCH_OK}"
check "concatenated pages reproduce the full result list in order" \
    "$(cmp -s "${WORKDIR}/full_keys.txt" "${WORKDIR}/stitched_keys.txt"; echo $?)"
check "no duplicate items across pages" \
    "$([ "$(sort "${WORKDIR}/stitched_keys.txt" | uniq -d | wc -l)" -eq 0 ]; echo $?)"

OUT="${WORKDIR}/beyond.json"
read -r HTTP_CODE TIME <<< "$(request "${OUT}" \
    "get_results=1&limit=10&offset=$((COUNT + 1000))&${SUMMARY_FILTER}&gene_list=${GENES_TWO_SPECIES}")"
check "page past the end is empty (HTTP ${HTTP_CODE} in ${TIME}s)" \
    "$(jq -e '(.data.expressionData.expressionCalls | length) == 0' "${OUT}" > /dev/null; echo $?)"

echo "=== E2E-3: error handling (expect HTTP 400 + InvalidRequestException) ==="
expect_error() { # expect_error <description> <query-suffix> <message-grep> [<exception-type>]
    local out="${WORKDIR}/err.json"
    local exception_type="${4:-InvalidRequestException}"
    local http_code time
    read -r http_code time <<< "$(request "${out}" "$2")"
    local ok=1
    if [ "${http_code}" = "400" ] \
        && jq -e --arg t "${exception_type}" '.data.exceptionType == $t' "${out}" > /dev/null \
        && jq -r '.message' "${out}" | grep -qi "$3"; then
        ok=0
    else
        echo "  (got HTTP ${http_code}, message: $(jq -r '.message // empty' "${out}" | head -c 120))"
    fi
    check "$1" "${ok}"
}
expect_error "single gene rejected" \
    "get_results=1&gene_list=${GENE_SINGLE}" "at least two gene"
expect_error "single-species gene list rejected" \
    "get_results=1&gene_list=${GENES_ONE_SPECIES}" "at least two species"
# Invalid quality values are rejected by the data_qual parameter format regex
# at the RequestParameters level, before reaching the controller.
expect_error "invalid data_qual rejected" \
    "get_results=1&data_qual=FOO&gene_list=${GENES_TWO_SPECIES}" "incorrect format" \
    "InvalidFormatException"
expect_error "limit above 10000 rejected" \
    "get_results=1&limit=10001&gene_list=${GENES_TWO_SPECIES}" "more than 10000"
expect_error "negative offset rejected" \
    "get_results=1&offset=-1&gene_list=${GENES_TWO_SPECIES}" "offset"
expect_error "get_filters rejected (post-filters unsupported)" \
    "get_filters=1&gene_list=${GENES_TWO_SPECIES}" "not supported"

echo "=== E2E-4: column definitions ==="
OUT="${WORKDIR}/c4.json"
read -r HTTP_CODE TIME <<< "$(request "${OUT}" \
    "get_column_definition=1&gene_list=${GENES_TWO_SPECIES}")"
echo "  (HTTP ${HTTP_CODE} in ${TIME}s)"
check "HTTP status is 200" "$([ "${HTTP_CODE}" = "200" ]; echo $?)"
check "columnDescriptions present and non-empty" \
    "$(jq -e '(.data.columnDescriptions | length) > 0' "${OUT}" > /dev/null; echo $?)"
#Link-type columns (e.g. "See supporting raw data") legitimately have no attributes.
check "column entries have titles, data columns have attributes" \
    "$(jq -e '(.data.columnDescriptions | all(.title != null))
        and (.data.columnDescriptions | map(select(.attributes != null)) | length > 0)' \
        "${OUT}" > /dev/null; echo $?)"

echo
echo "=== Summary: ${PASS} passed, ${FAIL} failed ==="
[ "${FAIL}" -eq 0 ]
