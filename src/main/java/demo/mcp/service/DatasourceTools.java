package demo.mcp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DatasourceTools {

    private final ObjectMapper objectMapper;
    private final List<Map<String, Object>> customers = new ArrayList<Map<String, Object>>();
    private final List<Map<String, Object>> orders = new ArrayList<Map<String, Object>>();

    public DatasourceTools(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        seed();
    }

    public String listTables() {
        ObjectNode result = objectMapper.createObjectNode();

        ObjectNode customerTable = result.putObject("customers");
        customerTable.putArray("columns").add("id").add("name").add("tier").add("city");

        ObjectNode orderTable = result.putObject("orders");
        orderTable.putArray("columns").add("id").add("customer_id").add("amount").add("status");

        return pretty(result);
    }

    public String queryDemoData(
            String table,
            String city,
            String tier,
            String status,
            Integer customer_id,
            Integer limit) {

        String normalizedTable = requireText(table, "table");
        int boundedLimit = boundedLimit(limit == null ? 5 : limit);
        List<Map<String, Object>> source = switch (normalizedTable) {
            case "customers" -> customers;
            case "orders" -> orders;
            default -> throw new IllegalArgumentException("Unsupported table: " + normalizedTable);
        };

        ArrayNode rows = objectMapper.createArrayNode();
        for (Map<String, Object> row : source) {
            if (matches(row, city, tier, status, customer_id)) {
                rows.add(objectMapper.valueToTree(row));
            }
            if (rows.size() >= boundedLimit) {
                break;
            }
        }

        ObjectNode result = objectMapper.createObjectNode();
        result.put("table", normalizedTable);
        result.set("rows", rows);
        result.put("rowCount", rows.size());
        return pretty(result);
    }

    private String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required argument: " + field);
        }
        return value.trim();
    }

    private int boundedLimit(int limit) {
        return Math.max(1, Math.min(limit, 20));
    }

    private boolean matches(Map<String, Object> row, String city, String tier, String status, Integer customerId) {
        return matchesText(row, "city", city)
                && matchesText(row, "tier", tier)
                && matchesText(row, "status", status)
                && matchesInt(row, "customer_id", customerId);
    }

    private boolean matchesText(Map<String, Object> row, String field, String filter) {
        if (filter == null || filter.isBlank()) {
            return true;
        }
        Object value = row.get(field);
        return value != null && filter.equalsIgnoreCase(String.valueOf(value));
    }

    private boolean matchesInt(Map<String, Object> row, String field, Integer filter) {
        if (filter == null) {
            return true;
        }
        Object value = row.get(field);
        return value instanceof Number number && number.intValue() == filter;
    }

    private String pretty(ObjectNode node) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render tool result", e);
        }
    }

    private void seed() {
        customers.add(row("id", 1, "name", "Acme Corp", "tier", "gold", "city", "Shanghai"));
        customers.add(row("id", 2, "name", "Northwind", "tier", "silver", "city", "Beijing"));
        customers.add(row("id", 3, "name", "Globex", "tier", "gold", "city", "Shenzhen"));
        customers.add(row("id", 4, "name", "Initech", "tier", "bronze", "city", "Hangzhou"));

        orders.add(row("id", 1001, "customer_id", 1, "amount", 12600.50, "status", "paid"));
        orders.add(row("id", 1002, "customer_id", 1, "amount", 5300.00, "status", "pending"));
        orders.add(row("id", 1003, "customer_id", 2, "amount", 880.00, "status", "paid"));
        orders.add(row("id", 1004, "customer_id", 3, "amount", 42000.00, "status", "paid"));
    }

    private Map<String, Object> row(Object... keyValues) {
        Map<String, Object> row = new LinkedHashMap<String, Object>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            row.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return row;
    }
}
