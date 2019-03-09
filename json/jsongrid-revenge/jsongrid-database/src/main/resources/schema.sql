-- POTUS == President Of The United States

CREATE TABLE IF NOT EXISTS potus_t (
	id					INTEGER PRIMARY KEY,
	firstName			VARCHAR(20) NOT NULL,
	middleName1			VARCHAR(20),
	middleName2			VARCHAR(20),
	lastName			VARCHAR(20) NOT NULL,
	tookOffice          DATE NOT NULL,
	leftOffice          DATE,
	aka                 VARCHAR(20)
);
