package com.crea.jee.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Chambre;
import com.crea.jee.beans.Eleve;
import com.crea.jee.beans.Livre;
import com.crea.jee.dao.ChambreDao;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.InscritDao;
import com.crea.jee.dao.LivreDao;

/*
 * Tests unitaires de EleveDao
 * Ils tournent contre la base ecole_test (schéma identique à ecole, vide) : lancer la JVM
 * avec -Ddb.name=ecole_test pour que DBAction cible cette base, et avoir le docker-compose
 * (service db) démarré avant de lancer les tests
 */
class EleveDaoTest {

	@BeforeEach
	@AfterEach
	void viderLesTables() throws SQLException {
		BaseDeTest.viderLesTables();
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
	void updateNumEleve_numDejaExistant_retourneMoins2() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse 1"));
		EleveDao.addEleve(new Eleve("TEST002", 0, "Testeur Deux", 30, "Adresse 2"));

		int resultat = EleveDao.updateNumEleve("TEST001", "TEST002");

		assertEquals(-2, resultat);
		assertNotNull(EleveDao.getEleveByNum("TEST001"));
	}

	@Test
	void updateNumEleve_nouveauNumInvalide_retourneMoins3() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		int resultat = EleveDao.updateNumEleve("TEST001", "");

		assertEquals(-3, resultat);
		assertNotNull(EleveDao.getEleveByNum("TEST001"));
	}

	@Test
	void getEleveByNo_inconnue_retourneNull() {
		assertNull(EleveDao.getEleveByNo(99999));
	}

	// eleve.no n'est modifiable par aucune méthode publique d'EleveDao : on le positionne directement en SQL
	@Test
	void getEleveByNo_retourneLOccupant() throws SQLException {
		ChambreDao.addChambre(new Chambre(999, null, 100f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		BaseDeTest.executer("UPDATE eleve SET no = ? WHERE num = ?", 999, "TEST001");

		Eleve occupant = EleveDao.getEleveByNo(999);

		assertNotNull(occupant);
		assertEquals("TEST001", occupant.getNum());
	}

	@Test
	void getElevesByNom_aucunResultat_retourneListeVide() {
		assertTrue(EleveDao.getElevesByNom("Inconnu").isEmpty());
	}

	@Test
	void getElevesByAge_aucunResultat_retourneListeVide() {
		assertTrue(EleveDao.getElevesByAge(999).isEmpty());
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

	/*
	 * vérifie la transaction de deleteEleveByNum : chambre et livre doivent être libérés
	 * (num = NULL, pas supprimés) et les inscriptions doivent disparaître, alors que l'élève
	 * lui-même est bien supprimé
	 */
	@Test
	void deleteEleveByNum_libereChambreLivreEtSupprimeLesInscriptions() throws SQLException {
		BaseDeTest.executer("INSERT INTO uv (code, nbh, coord) VALUES (?, ?, ?)", "UV_TEST", 10, "Coordinateur Test");

		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		ChambreDao.addChambre(new Chambre(999, null, 100f));
		ChambreDao.updateOccupantChambre(999, "TEST001");

		LivreDao.addLivre(new Livre("COTE_TEST", null, "Titre Test", null));
		LivreDao.updateEmprunteurLivre("COTE_TEST", "TEST001");

		BaseDeTest.executer("INSERT INTO inscrit (code, num, note) VALUES (?, ?, ?)", "UV_TEST", "TEST001", 15f);

		int nbSupprimes = EleveDao.deleteEleveByNum("TEST001");

		assertEquals(1, nbSupprimes);
		assertNull(EleveDao.getEleveByNum("TEST001"));

		Chambre chambre = ChambreDao.getChambreByNo(999);
		assertNotNull(chambre);
		assertNull(chambre.getNum());

		Livre livre = LivreDao.getLivreByCote("COTE_TEST");
		assertNotNull(livre);
		assertNull(livre.getNum());
		assertNull(livre.getDatepret());

		boolean inscriptionRestante = InscritDao.getAllInscriptions().stream()
				.anyMatch(i -> "TEST001".equals(i.getNum()));
		assertFalse(inscriptionRestante);
	}

}
