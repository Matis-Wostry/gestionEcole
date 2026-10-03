-- phpMyAdmin SQL Dump
-- version 4.1.14
-- http://www.phpmyadmin.net
--
-- Client :  127.0.0.1
-- Généré le :  Mar 18 Novembre 2018 à 00:17
-- Version du serveur :  5.6.17
-- Version de PHP :  5.5.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Base de données :  `ecole`
--

-- --------------------------------------------------------

--
-- Structure de la table `chambre`
--

CREATE TABLE IF NOT EXISTS `chambre` (
  `no` int(11) NOT NULL COMMENT 'Numéro identifiant de la chambre de l''élève',
  `num` varchar(100) DEFAULT NULL COMMENT 'Numéro identifiant de l''élève',
  `prix` float NOT NULL COMMENT 'Prix de location de la chambre',
  PRIMARY KEY (`no`),
  KEY `Chambre_index_Eleve` (`num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table des chambres éventuellement associées aux élèves.';

--
-- Contenu de la table `chambre`
--

INSERT INTO `chambre` (`no`, `num`, `prix`) VALUES
(1, NULL, 350.25),
(2, NULL, 400.55),
(3, NULL, 350.25),
(4, NULL, 400.55),
(5, NULL, 250.45),
(6, NULL, 150.75),
(7, NULL, 200.25);

-- --------------------------------------------------------

--
-- Structure de la table `eleve`
--

CREATE TABLE IF NOT EXISTS `eleve` (
  `num` varchar(100) NOT NULL COMMENT 'Numéro identifiant l''elève',
  `no` int(11) DEFAULT NULL COMMENT 'Numéro de la chambre de l''élève',
  `nom` varchar(50) DEFAULT NULL COMMENT 'Nom de l''élève',
  `age` tinyint(4) DEFAULT NULL COMMENT 'Age de l''élève',
  `adresse` varchar(200) DEFAULT NULL COMMENT 'Adresse de l''élève',
  PRIMARY KEY (`num`),
  KEY `Eleve_index_Chambre` (`no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table contenant l''ensemble des élèves';

--
-- Contenu de la table `eleve`
--

INSERT INTO `eleve` (`num`, `no`, `nom`, `age`, `adresse`) VALUES
('AGUE001', NULL, 'AGUE MAX', 40, '18 Rue Labat 75018 Paris'),
('KAMTO005', NULL, 'KAMTO Diogène', 50, '54 Rue des Ebisoires 78300 Poissy'),
('LAURENCY004', NULL, 'LAURENCY Patrick', 52, '79 Rue des Poules 75015 Paris'),
('TABIS003', NULL, 'Ghislaine TABIS', 30, '12 Rue du louvre 75013 Paris'),
('TAHAE002', NULL, 'TAHA RIDENE', 30, '12 Rue des Chantiers 78000 Versailles');

-- --------------------------------------------------------

--
-- Structure de la table `inscrit`
--

CREATE TABLE IF NOT EXISTS `inscrit` (
  `code` varchar(100) NOT NULL COMMENT 'Id Code de l''uv',
  `num` varchar(100) NOT NULL COMMENT 'Numéro identifiant de l''élève',
  `note` float DEFAULT NULL COMMENT 'Note accordée à un élève sur une matière ou UV',
  PRIMARY KEY (`code`,`num`),
  KEY `inscrit_index_Eleve` (`num`),
  KEY `inscrit_index_Uv` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table recapitulant les élèves inscrit aux différents UV';

-- --------------------------------------------------------

--
-- Structure de la table `livre`
--

CREATE TABLE IF NOT EXISTS `livre` (
  `cote` varchar(100) NOT NULL COMMENT 'Numéro identifiant du livre',
  `num` varchar(100) DEFAULT NULL COMMENT 'Numéro identifiant de l''élève qui a emprunté le livre',
  `titre` varchar(100) NOT NULL COMMENT 'Titre du livre',
  `datepret` datetime DEFAULT NULL COMMENT 'Date et heure du pret du livre par l''élève',
  PRIMARY KEY (`cote`),
  KEY `Livre_index_Eleve` (`num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table contenant les livres éventuellement associés aux élèves.';

--
-- Contenu de la table `livre`
--

INSERT INTO `livre` (`cote`, `num`, `titre`, `datepret`) VALUES
('ISBN10000', NULL, 'Un vase d''honneur', NULL),
('ISBN10001', NULL, 'Seul au monde', NULL),
('ISBN10002', NULL, 'Meutre à la maison blanche', NULL),
('ISBN10003', NULL, 'Double Impact', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `uv`
--

CREATE TABLE IF NOT EXISTS `uv` (
  `code` varchar(100) NOT NULL COMMENT 'Id Code de l''uv',
  `nbh` tinyint(3) NOT NULL COMMENT 'Nombre d''heure de cour',
  `coord` varchar(255) DEFAULT NULL COMMENT 'COORD',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Table des unités de valeurs ou d''enseignement';

--
-- Contenu de la table `uv`
--

INSERT INTO `uv` (`code`, `nbh`, `coord`) VALUES
('JAVA_Grp1', 30, 'Mr RIDENE'),
('maths_info_Grp1', 26, 'Mme ASSELAH'),
('Web_Service_Grp1', 10, 'Mr PLASSE');

--
-- Contraintes pour les tables exportées
--

--
-- Contraintes pour la table `chambre`
--
ALTER TABLE `chambre`
  ADD CONSTRAINT `chambre_fk1` FOREIGN KEY (`num`) REFERENCES `eleve` (`num`) ON UPDATE CASCADE;

--
-- Contraintes pour la table `eleve`
--
ALTER TABLE `eleve`
  ADD CONSTRAINT `Eleve_fk1` FOREIGN KEY (`no`) REFERENCES `chambre` (`no`) ON UPDATE CASCADE;

--
-- Contraintes pour la table `inscrit`
--
ALTER TABLE `inscrit`
  ADD CONSTRAINT `inscrit_fk1` FOREIGN KEY (`num`) REFERENCES `eleve` (`num`) ON UPDATE CASCADE,
  ADD CONSTRAINT `inscrit_fk2` FOREIGN KEY (`code`) REFERENCES `uv` (`code`) ON UPDATE CASCADE;

--
-- Contraintes pour la table `livre`
--
ALTER TABLE `livre`
  ADD CONSTRAINT `livre_fk1` FOREIGN KEY (`num`) REFERENCES `eleve` (`num`) ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
