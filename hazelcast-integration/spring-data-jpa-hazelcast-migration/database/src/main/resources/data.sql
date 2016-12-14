-- Needs     	System.setProperty("hibernate.hbm2ddl.import_files_sql_extractor"
--    	  ,"org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor");

INSERT INTO noun (id, english, french, spanish)
VALUES
(1, 'cat', 'chat', 'gato')
,(2, 'dog', 'chien', 'perro')
,(3, 'milk', 'lait', 'leche')
,(4, 'world', 'monde', 'mundo')
;

INSERT INTO verb (id, english, french, spanish, tense)
VALUES
(1, 'drink', 'bois', 'bebe', 1)
;
