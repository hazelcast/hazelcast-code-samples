package com.hazelcast.samples.json.jsongrid.revenge;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>The {@code JdbcTemplate} injected by Spring gives us
 * a connection to the database, based on the
 * {@code application.yml} file.
 * </p>
 * <p>Run two queries against the "{@code potus_t}" table
 * holding the <b>P</b>residents <b>O</b>f <b>T</b>he 
 * <b>U</b>nited <b>S</b>tates.  
 * </p>
 * <p>So long as some rows are displayed, everything is
 * working fine. There should be at least 45 rows as
 * there had been 45 presidents at the time of Hazelcast
 * 3.12's launch of enhanced JSON support.
 * </p>
 */
@Configuration
@Slf4j
public class ApplicationRunner implements CommandLineRunner {

	private static final String NEWLINE = System.getProperty("line.separator");
	
	private static final String[] SQLS = {
			"SELECT COUNT(*) FROM potus_t"
			,"SELECT * FROM potus_t"
	};
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public void run(String... args) throws Exception {
		for (String sql : SQLS) {
			log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~");
			log.info(sql);
			
			try {
				List<Map<String, Object>> resultSet = this.jdbcTemplate.queryForList(sql);
				
		        for (int i = 0; i < resultSet.size(); i++) {
		            Map<String, Object> map = resultSet.get(i);

		            StringBuilder stringBuilder = new StringBuilder();

		            stringBuilder.append((i+1) + ":");
		            
		            Iterator<Map.Entry<String, Object>> iterator = map.entrySet().iterator();
		            int j=0;
		            while (iterator.hasNext()) {
		            	Map.Entry<String, Object> entry = iterator.next();
		            	if (j > 0) {
		            		stringBuilder.append(",");
		            	}
		            	stringBuilder.append(entry.getKey() + "==" + entry.getValue());
		            	j++;
		            }
		            log.info(stringBuilder.toString() + NEWLINE);
		        }
		        log.info("[{} row{}]", resultSet.size(), (resultSet.size() == 1 ? "" : "s"));		
				
			} catch (Exception e) {
				log.error(sql, e);
			}
			
			log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
	}
}
