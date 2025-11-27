#!/usr/bin/env bash

# Quick test queries (non-interactive version)
# Default base URL - change this to your actual service endpoint
BASE_URL=${BASE_URL:-http://localhost/darwin-catalog}

echo "🔍 Darwin Catalog - Quick Test Data Queries"
echo "=============================================="
echo "Base URL: ${BASE_URL}"
echo ""

# ============================================================================
echo "1️⃣  Listing all assets (first 10)..."
curl -s -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{}' | jq '.data[] | {fqdn, type, source_platform}'

echo ""
echo "=============================================="

# ============================================================================
echo "2️⃣  Searching for 'example' keyword..."
curl -s -X POST "${BASE_URL}/v1/search?depth=5&offset=0&page_size=5" \
  -H "Content-Type: application/json" \
  -d '{"search_string": "example"}' | jq '.data[] | {asset_name, asset_prefix, depth}'

echo ""
echo "=============================================="

# ============================================================================
echo "3️⃣  Getting specific asset: 'example:table:redshift:example:test_x_league'"
curl -s -X GET "${BASE_URL}/v1/assets/example:table:redshift:example:test_x_league" \
  -H "Content-Type: application/json" | jq '{fqdn, type, schema: .schema.fields[0:3]}'

echo ""
echo "=============================================="

# ============================================================================
echo "4️⃣  Getting lineage for asset: '6_M'"
curl -s -X GET "${BASE_URL}/v1/assets/6_M/lineage" \
  -H "Content-Type: application/json" | jq '{upstream: .upstream[].fqdn, downstream: .downstream[].fqdn}'

echo ""
echo "=============================================="

# ============================================================================
echo "5️⃣  Listing only TABLE type assets..."
curl -s -X POST "${BASE_URL}/v1/assets?offset=0&page_size=5" \
  -H "Content-Type: application/json" \
  -d '{"type": ["TABLE"]}' | jq '.data[] | {fqdn, type}'

echo ""
echo "=============================================="

# ============================================================================
echo "6️⃣  Listing only KAFKA type assets..."
curl -s -X POST "${BASE_URL}/v1/assets?offset=0&page_size=5" \
  -H "Content-Type: application/json" \
  -d '{"type": ["KAFKA"]}' | jq '.data[] | {fqdn, type}'

echo ""
echo "=============================================="

# ============================================================================
echo "7️⃣  Getting asset with schema: 'example:table:redshift:segment:example:existing_transactions'"
curl -s -X GET "${BASE_URL}/v1/assets/example:table:redshift:segment:example:existing_transactions?fields=schema,tags" \
  -H "Content-Type: application/json" | jq '{fqdn, tags, schema_version: .schema.version_id}'

echo ""
echo "=============================================="

# ============================================================================
echo "8️⃣  Getting rules/monitors for: 'example:table:test:webhook:test_asset_1'"
curl -s -X GET "${BASE_URL}/v1/assets/example:table:test:webhook:test_asset_1/rules" \
  -H "Content-Type: application/json" | jq '.[] | {type, comparator, health_status, monitor_id}'

echo ""
echo "=============================================="

# ============================================================================
echo "9️⃣  Listing assets by source platform: 'databeam'"
curl -s -X POST "${BASE_URL}/v1/assets?offset=0&page_size=5" \
  -H "Content-Type: application/json" \
  -d '{"source_platform": ["databeam"]}' | jq '.data[] | {fqdn, source_platform, business_roster}'

echo ""
echo "=============================================="

# ============================================================================
echo "🔟  Getting SERVICE and DASHBOARD type assets..."
curl -s -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"type": ["SERVICE", "DASHBOARD"]}' | jq '.data[] | {fqdn, type}'

echo ""
echo "=============================================="
echo "✅ All quick queries completed!"
echo ""
echo "💡 Run the full interactive version: ./sample-test-data-queries.sh"

