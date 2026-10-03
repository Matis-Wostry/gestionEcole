-- Base de test : même schéma que ecole.sql, sans données.
-- Script autonome (CREATE DATABASE + USE) pour pouvoir être exécuté aux côtés
-- de ecole.sql sans écraser la base de démo.

CREATE DATABASE IF NOT EXISTS ecole_test;
USE ecole_test;

CREATE TABLE IF NOT EXISTS `chambre` (
  `no` int(11) NOT NULL COMMENT 'Numéro identifiant de la chambre de l''élève',
  `num` varchar(100) DEFAULT NULL COMMENT 'Numéro identifiant de l''élève',
  `prix` float NOT NULL COMMENT 'Prix de location de la chambre',
  PRIMARY KEY (`no`),
  KEY `Chambre_index_Eleve` (`num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table des chambres éventuellement associées aux élèves.';

CREATE TABLE IF NOT EXISTS `eleve` (
  `num` varchar(100) NOT NULL COMMENT 'Numéro identifiant l''elève',
  `no` int(11) DEFAULT NULL COMMENT 'Numéro de la chambre de l''élève',
  `nom` varchar(50) DEFAULT NULL COMMENT 'Nom de l''élève',
  `age` tinyint(4) DEFAULT NULL COMMENT 'Age de l''élève',
  `adresse` varchar(200) DEFAULT NULL COMMENT 'Adresse de l''élève',
  PRIMARY KEY (`num`),
  KEY `Eleve_index_Chambre` (`no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table contenant l''ensemble des élèves';

CREATE TABLE IF NOT EXISTS `inscrit` (
  `code` varchar(100) NOT NULL COMMENT 'Id Code de l''uv',
  `num` varchar(100) NOT NULL COMMENT 'Numéro identifiant de l''élève',
  `note` float DEFAULT NULL COMMENT 'Note accordée à un élève sur une matière ou UV',
  PRIMARY KEY (`code`,`num`),
  KEY `inscrit_index_Eleve` (`num`),
  KEY `inscrit_index_Uv` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table recapitulant les élèves inscrit aux différents UV';

CREATE TABLE IF NOT EXISTS `livre` (
  `cote` varchar(100) NOT NULL COMMENT 'Numéro identifiant du livre',
  `num` varchar(100) DEFAULT NULL COMMENT 'Numéro identifiant de l''élève qui a emprunté le livre',
  `titre` varchar(100) NOT NULL COMMENT 'Titre du livre',
  `datepret` datetime DEFAULT NULL COMMENT 'Date et heure du pret du livre par l''élève',
  PRIMARY KEY (`cote`),
  KEY `Livre_index_Eleve` (`num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table contenant les livres éventuellement associés aux élèves.';

CREATE TABLE IF NOT EXISTS `uv` (
  `code` varchar(100) NOT NULL COMMENT 'Id Code de l''uv',
  `nbh` tinyint(3) NOT NULL COMMENT 'Nombre d''heure de cour',
  `coord` varchar(255) DEFAULT NULL COMMENT 'COORD',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table des unités de valeurs ou d''enseignement';

ALTER TABLE `chambre`
  ADD CONSTRAINT `chambre_test_fk1` FOREIGN KEY (`num`) REFERENCES `eleve` (`num`) ON UPDATE CASCADE;

ALTER TABLE `eleve`
  ADD CONSTRAINT `Eleve_test_fk1` FOREIGN KEY (`no`) REFERENCES `chambre` (`no`) ON UPDATE CASCADE;

ALTER TABLE `inscrit`
  ADD CONSTRAINT `inscrit_test_fk1` FOREIGN KEY (`num`) REFERENCES `eleve` (`num`) ON UPDATE CASCADE,
  ADD CONSTRAINT `inscrit_test_fk2` FOREIGN KEY (`code`) REFERENCES `uv` (`code`) ON UPDATE CASCADE;

ALTER TABLE `livre`
  ADD CONSTRAINT `livre_test_fk1` FOREIGN KEY (`num`) REFERENCES `eleve` (`num`) ON UPDATE CASCADE;
