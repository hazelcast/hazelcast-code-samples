-- POTUS == President Of The United States
-- VPOTUS == Vice-President Of The United States

-- Deliberately we have some different columns in POTUS and VPOTUS

CREATE TABLE IF NOT EXISTS potus (
	id			INTEGER PRIMARY KEY,
	firstName		VARCHAR(20) NOT NULL,
	middleName1		VARCHAR(20),
	middleName2		VARCHAR(20),
	lastName		VARCHAR(20) NOT NULL,
	tookOffice		DATE NOT NULL,
	leftOffice		DATE,
	aka			VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS vpotus (
	id			INTEGER PRIMARY KEY,
	name			VARCHAR(40),
	tookOffice		DATE NOT NULL,
	leftOffice		DATE
);

