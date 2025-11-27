package darwincatalog.util;

import darwincatalog.exception.InvalidSqlException;
import java.util.HashSet;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.springframework.stereotype.Component;

@Component
public class SqlParser {

  public Set<String> getTableNames(String sql) {
    // Handle empty or null SQL
    if (sql == null || sql.trim().isEmpty()) {
      return new HashSet<>();
    }

    Statement statement = null;
    try {
      statement = CCJSqlParserUtil.parse(sql);
    } catch (JSQLParserException e) {
      throw new InvalidSqlException(sql);
    }

    Set<String> tableNames = new HashSet<>();

    if (statement instanceof Select) {
      Select select = (Select) statement;

      // Extract table names
      TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
      tableNames = new HashSet<>(tablesNamesFinder.getTableList((Statement) select));

      // todo Extract columns
      //            PlainSelect plainSelect = select.getPlainSelect();
      //            System.out.println("\nColumns selected:");
      //            for (SelectItem item : plainSelect.getSelectItems()) {
      //                System.out.println(item.toString());
      //            }
    }
    return tableNames;
  }
}
