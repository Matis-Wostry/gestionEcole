package com.crea.jee.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Eleve;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.utils.DBAction;

/*
 * Tests unitaires de EleveDao
 * Ils tournent contre la base ecole_test (schéma identique à ecole, vide) : lancer la JVM
 * avec -Ddb.name=ecole_test pour que DBAction cible cette base, et avoir le docker-compose
 * (service db) démarré avant de lancer les tests
 */
class EleveDaoTest {

	// vide la table eleve pour repartir d'un état connu avant chaque test
	@BeforeEach
	void viderTableEleve() throws SQLException {
		DBAction.DBConnexion();
		try (Statement stm = DBAction.getCon().createStatement()) {
			stm.executeUpdate("DELETE FROM eleve");
		} finally {
			DBAction.DBClose();
		}
	}

	// ne laisse aucune donnée de test derrière soi
	@AfterEach
	void nettoyerApresTest() throws SQLException {
		viderTableEleve();
	}

	@Test
	void addEleve_ajouteEtRecuperable() {
		Eleve nouvel_eleve = new Eleve("TEST001", 0, "Testeur Un", 25, "1 rue du Test");

		int resultAjout = EleveDao.addEleve(nouvel_eleve);
		assertEquals(1, resultAjout);

		Eleve recupere = EleveDao.getEleveByNum("TEST001");
		assertNotNull(recupere);
		assertEquals("Testeur Un", recupere.getNom());
		assertEquals(25, recupere.getAge());
		assertEquals("1 rue du Test", recupere.getAdresse());
	}

	@Test
	void addEleve_numDejaExistant_retourneMoins2() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "1 rue du Test"));

		int resultDoublon = EleveDao.addEleve(new Eleve("TEST001", 0, "Autre Nom", 30, "Autre adresse"));

		assertEquals(-2, resultDoublon);
	}

	@Test
	void addEleve_donneesInvalides_retourneMoins3() {
		assertEquals(-3, EleveDao.addEleve(new Eleve("", 0, "Nom", 25, "Adresse")));
		assertEquals(-3, EleveDao.addEleve(new Eleve("TEST002", 0, "", 25, "Adresse")));
		assertEquals(-3, EleveDao.addEleve(new Eleve("TEST003", 0, "Nom", -5, "Adresse")));

		assertNull(EleveDao.getEleveByNum("TEST002"));
		assertNull(EleveDao.getEleveByNum("TEST003"));
	}

	@Test
	void getEleveByNum_inconnu_retourneNull() {
		assertNull(EleveDao.getEleveByNum("INCONNU999"));
	}

	@Test
	void updateAdresseEleve_modifieLAdresse() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Ancienne adresse"));

		int nbLignes = EleveDao.updateAdresseEleve("TEST001", "Nouvelle adresse");

		assertEquals(1, nbLignes);
		assertEquals("Nouvelle adresse", EleveDao.getEleveByNum("TEST001").getAdresse());
	}

	@Test
	void updateAdresseEleve_adresseInvalide_retourneMoins3() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Ancienne adresse"));

		int resultat = EleveDao.updateAdresseEleve("TEST001", "");

		assertEquals(-3, resultat);
		assertEquals("Ancienne adresse", EleveDao.getEleveByNum("TEST001").getAdresse());
	}

	@Test
	void updateNumEleve_renommeLEleve() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		int nbLignes = EleveDao.updateNumEleve("TEST001", "TEST001B");

		assertEquals(1, nbLignes);
		assertNull(EleveDao.getEleveByNum("TEST001"));
		assertNotNull(EleveDao.getEleveByNum("TEST001B"));
	}

	@Test
	void getElevesByNom_filtreCorrectement() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Dupont", 25, "Adresse 1"));
		EleveDao.addEleve(new Eleve("TEST002", 0, "Dupont", 30, "Adresse 2"));
		EleveDao.addEleve(new Eleve("TEST003", 0, "Martin", 40, "Adresse 3"));

		List<Eleve> dupont = EleveDao.getElevesByNom("Dupont");

		assertEquals(2, dupont.size());
	}

	@Test
	void getElevesByAge_filtreCorrectement() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Dupont", 25, "Adresse 1"));
		EleveDao.addEleve(new Eleve("TEST002", 0, "Martin", 25, "Adresse 2"));
		EleveDao.addEleve(new Eleve("TEST003", 0, "Durand", 40, "Adresse 3"));

		List<Eleve> vingtCinqAns = EleveDao.getElevesByAge(25);

		assertEquals(2, vingtCinqAns.size());
	}

	@Test
	void getAllEleves_retourneTousLesEleves() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Dupont", 25, "Adresse 1"));
		EleveDao.addEleve(new Eleve("TEST002", 0, "Martin", 30, "Adresse 2"));

		List<Eleve> tous = EleveDao.getAllEleves();

		assertEquals(2, tous.size());
	}

	@Test
	void deleteEleveByNum_supprimeLEleve() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		int nbSupprimes = EleveDao.deleteEleveByNum("TEST001");

		assertEquals(1, nbSupprimes);
		assertNull(EleveDao.getEleveByNum("TEST001"));
	}

	@Test
	void deleteEleveByNum_inconnu_retourneZero() {
		int nbSupprimes = EleveDao.deleteEleveByNum("INCONNU999");

		assertEquals(0, nbSupprimes);
	}

}
