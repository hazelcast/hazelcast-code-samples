DELETE FROM noun;

INSERT INTO noun (id, english, french, spanish)
VALUES
(1, 'milk', 'lait', 'leche')
,(2, 'water', 'eau', 'agua')
;

DELETE FROM verb;

INSERT INTO verb (id, english, french, spanish, tense)
VALUES
(9, 'drink', 'bois', 'bebe', 1)
,(10, 'eat', 'manger', 'come', 1)
;
