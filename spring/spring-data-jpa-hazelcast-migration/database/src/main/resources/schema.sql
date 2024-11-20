-- Needs     	System.setProperty("hibernate.hbm2ddl.import_files_sql_extractor"
--    	  ,"org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor");

CREATE TABLE IF NOT EXISTS noun (
	id                     INTEGER PRIMARY KEY,
	english                VARCHAR(20),
	french                 VARCHAR(20),
	spanish                VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS verb (
	id                     INTEGER PRIMARY KEY,
	english                VARCHAR(20),
	french                 VARCHAR(20),
	spanish                VARCHAR(20),
	tense                  INTEGER
);
