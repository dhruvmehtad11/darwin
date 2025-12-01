#!/usr/bin/env bash

# Sample curl commands to query the test data loaded by pre-deploy.sh
# Default base URL - change this to your actual service endpoint
BASE_URL=${BASE_URL:-http://localhost/darwin-catalog}

echo "🔍 Darwin Catalog - Sample Test Data Queries"
echo "=============================================="
echo "Base URL: ${BASE_URL}"
echo ""

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ============================================================================
# 1. LIST ALL ASSETS (with pagination)
# ============================================================================
echo -e "${BLUE}1. List all assets (first 10)${NC}"
curl -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 2. SEARCH FOR ASSETS BY KEYWORD
# ============================================================================
echo -e "${BLUE}2. Search for assets containing 'example'${NC}"
curl -X POST "${BASE_URL}/v1/search?depth=5&offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"search_string": "example"}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 3. SEARCH FOR KAFKA ASSETS
# ============================================================================
echo -e "${BLUE}3. Search for Kafka assets${NC}"
curl -X POST "${BASE_URL}/v1/search?depth=5&offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"search_string": "kafka"}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 4. GET SPECIFIC ASSET BY FQDN - Table Example
# ============================================================================
echo -e "${BLUE}4. Get asset details for 'example:table:redshift:example:test_x_league'${NC}"
curl -X GET "${BASE_URL}/v1/assets/example:table:redshift:example:test_x_league" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 5. GET SPECIFIC ASSET BY FQDN - Kafka Example
# ============================================================================
echo -e "${BLUE}5. Get asset details for 'example:kafka:datahighway_logger:test_consumer_offsets'${NC}"
curl -X GET "${BASE_URL}/v1/assets/example:kafka:datahighway_logger:test_consumer_offsets" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 6. GET SPECIFIC ASSET WITH SCHEMA
# ============================================================================
echo -e "${BLUE}6. Get asset with schema - 'example:table:redshift:segment:example:existing_transactions'${NC}"
curl -X GET "${BASE_URL}/v1/assets/example:table:redshift:segment:example:existing_transactions?fields=schema" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 7. GET LINEAGE FOR AN ASSET (from lineage.sql test data)
# ============================================================================
echo -e "${BLUE}7. Get lineage for asset '6_M' (middle node in lineage graph)${NC}"
curl -X GET "${BASE_URL}/v1/assets/6_M/lineage" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 8. GET LINEAGE FOR UPSTREAM ASSET
# ============================================================================
echo -e "${BLUE}8. Get lineage for asset '1_L21' (left upstream)${NC}"
curl -X GET "${BASE_URL}/v1/assets/1_L21/lineage" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 9. GET LINEAGE FOR DOWNSTREAM ASSET
# ============================================================================
echo -e "${BLUE}9. Get lineage for asset '7_R11' (right downstream)${NC}"
curl -X GET "${BASE_URL}/v1/assets/7_R11/lineage" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 10. LIST ASSETS BY TYPE - TABLES ONLY
# ============================================================================
echo -e "${BLUE}10. List only TABLE type assets${NC}"
curl -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"type": ["TABLE"]}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 11. LIST ASSETS BY TYPE - KAFKA ONLY
# ============================================================================
echo -e "${BLUE}11. List only KAFKA type assets${NC}"
curl -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"type": ["KAFKA"]}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 12. GET ASSET WITH SPECIFIC FIELDS
# ============================================================================
echo -e "${BLUE}12. Get asset with only schema and tags fields${NC}"
curl -X GET "${BASE_URL}/v1/assets/example:table:redshift:segment:example:existing_transactions?fields=schema,tags" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 13. GET RULES/MONITORS FOR AN ASSET (from monitor.sql test data)
# ============================================================================
echo -e "${BLUE}13. Get rules/monitors for asset 'example:table:test:webhook:test_asset_1'${NC}"
curl -X GET "${BASE_URL}/v1/assets/example:table:test:webhook:test_asset_1/rules" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 14. LIST ASSETS BY SOURCE PLATFORM
# ============================================================================
echo -e "${BLUE}14. List assets from 'databeam' platform${NC}"
curl -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"source_platform": ["databeam"]}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 15. LIST ASSETS WITH MULTIPLE FILTERS
# ============================================================================
echo -e "${BLUE}15. List TABLE assets from 'databeam' platform${NC}"
curl -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{
    "type": ["TABLE"],
    "source_platform": ["databeam"]
  }' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 16. SEARCH WITH DEPTH PARAMETER
# ============================================================================
echo -e "${BLUE}16. Search 'redshift' with depth 3${NC}"
curl -X POST "${BASE_URL}/v1/search?depth=3&offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"search_string": "redshift"}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 17. GET ASSET FROM LAKEHOUSE (from metric.sql)
# ============================================================================
echo -e "${BLUE}17. Get lakehouse asset 'example:table:lakehouse:example:test_x_matchpoints'${NC}"
curl -X GET "${BASE_URL}/v1/assets/example:table:lakehouse:example:test_x_matchpoints" \
  -H "Content-Type: application/json" | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 18. SEARCH FOR SERVICE TYPE ASSETS
# ============================================================================
echo -e "${BLUE}18. List only SERVICE type assets${NC}"
curl -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"type": ["SERVICE"]}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 19. SEARCH FOR DASHBOARD TYPE ASSETS
# ============================================================================
echo -e "${BLUE}19. List only DASHBOARD type assets${NC}"
curl -X POST "${BASE_URL}/v1/assets?offset=0&page_size=10" \
  -H "Content-Type: application/json" \
  -d '{"type": ["DASHBOARD"]}' | jq .

echo ""
echo "Press Enter to continue..."
read

# ============================================================================
# 20. GET SCHEMA DETAILS FOR AN ASSET
# ============================================================================
echo -e "${BLUE}20. Get schema for 'example:table:redshift:segment:example:test_asset_with_schema'${NC}"
curl -X GET "${BASE_URL}/v1/assets/example:table:redshift:segment:example:test_asset_with_schema/schema" \
  -H "Content-Type: application/json" | jq .

echo ""
echo -e "${GREEN}✅ All sample queries completed!${NC}"
echo ""
echo "💡 Tips:"
echo "   - Remove the 'Press Enter' prompts to run all queries in sequence"
echo "   - Use jq to filter specific fields: | jq '.data[] | {fqdn, type}'"
echo "   - Change BASE_URL environment variable: export BASE_URL=https://your-service.com"
echo "   - Add '-v' flag to curl for verbose output"

