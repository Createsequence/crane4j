DROP TABLE IF EXISTS foo;

CREATE TABLE `foo` (
   `id` int PRIMARY KEY NOT NULL AUTO_INCREMENT,
   `name` varchar(255) DEFAULT '',
   `age` int DEFAULT NULL,
   `sex` int DEFAULT NULL
);