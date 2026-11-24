package com.hazelcast.samples.sql;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.sql.SqlResult;
import com.hazelcast.sql.SqlRow;
import com.hazelcast.sql.SqlRowMetadata;
import com.hazelcast.sql.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Example works only if all classes are present on the classpath - therefore only when using no WAN replication
 * (that requires separate docker instances; adding classes to their classpath would be an unnecessary complication).
 */
@RestController
@ConditionalOnExpression("${use.wan.replication:false} == false")
public class SqlAnalysisController {

    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public SqlAnalysisController(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    /**
     * NOTE: this endpoint is very unsafe and used only to quickly print results.
     */
    @RequestMapping(value = "/basket/query", produces = MediaType.TEXT_PLAIN_VALUE)
    public String sql(@RequestParam("sql") String sql) {
        SqlService sqlService = hazelcastInstance.getSql();
        // normally don't do it, don't pass sql
        try (SqlResult sqlResult = sqlService.execute(sql)) {
            return formatSqlResult(sqlResult);
        }
    }

    private static String formatSqlResult(SqlResult result) {
        SqlRowMetadata metadata = result.getRowMetadata();
        int columnCount = metadata.getColumnCount();
        int[] widths = new int[columnCount];
        String[] headers = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            headers[i] = tableCell(metadata.getColumn(i).getName());
            widths[i] = Math.max(3, headers[i].length());
        }

        List<String[]> rows = new ArrayList<>();
        for (SqlRow row : result) {
            String[] cells = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                cells[i] = tableCell(row.getObject(i));
                widths[i] = Math.max(widths[i], cells[i].length());
            }
            rows.add(cells);
        }

        StringBuilder table = new StringBuilder();
        appendTableRow(table, headers, widths);
        table.append('|');
        for (int width : widths) {
            table.append(' ').append("-".repeat(width)).append(" |");
        }
        table.append('\n');
        for (String[] row : rows) {
            appendTableRow(table, row, widths);
        }
        return table.toString();
    }

    private static void appendTableRow(StringBuilder table, String[] cells, int[] widths) {
        table.append('|');
        for (int i = 0; i < cells.length; i++) {
            table.append(' ').append(cells[i])
                 .append(" ".repeat(widths[i] - cells[i].length())).append(" |");
        }
        table.append('\n');
    }

    private static String tableCell(Object value) {
        return (value == null ? "NULL" : value.toString())
                .replace("\\", "\\\\")
                .replace("|", "\\|")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
