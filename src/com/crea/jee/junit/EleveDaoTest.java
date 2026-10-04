package com.crea.jee.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Chambre;
import com.crea.jee.beans.Eleve;
import com.crea.jee.beans.Livre;
import com.crea.jee.dao.ChambreDao;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.InscritDao;
import com.crea.jee.dao.LivreDao;
import com.crea.jee.wrappers.EleveWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Tests unitaires de EleveDao
 * Ils tournent contre la base ecole_test (schéma identique à ecole, vide) : lancer la JVM
 * avec -Ddb.name=ecole_test pour que DBAction cible cette base, et avoir le docker-compose
 * (service db) démarré avant de lancer les tests
 * [OK] = cas valide, l'opération doit réussir ; [ERREUR] = cas invalide, le DAO doit refuser ou ne rien trouver
 */
@DisplayName("EleveDao")
class EleveDaoTest {

	@BeforeEach
	@AfterEach
	void viderLesTables() throws SQLException {
		BaseDeTest.viderLesTables();
	}

	@Test
	@DisplayName("[OK] addEleve : un élève ajouté se retrouve en base avec les bonnes informations")
	void addEleve_ajouteEtRecuperable() {
		Eleve nouvelEleve = new Eleve("TEST001", 0, "Testeur Un", 25, "1 rue du Test");

		int resultAjout = EleveDao.addEleve(nouvelEleve);
		assertEquals(1, resultAjout);

		Eleve recupere = EleveDao.getEleveByNum("TEST001").getEleve();
		assertNotNull(recupere);
		assertEquals("Testeur Un", recupere.getNom());
		assertEquals(25, recupere.getAge());
		assertEquals("1 rue du Test", recupere.getAdresse());
	}

	@Test
	@DisplayName("[ERREUR] addEleve : ajouter un numéro déjà existant est refusé (code -2)")
	void addEleve_numDejaExistant_retourneMoins2() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "1 rue du Test"));

		int resultDoublon = EleveDao.addEleve(new Eleve("TEST001", 0, "Autre Nom", 30, "Autre adresse"));

		assertEquals(-2, resultDoublon);
	}

	@Test
	@DisplayName("[ERREUR] addEleve : numéro vide, nom vide ou âge négatif sont refusés (code -3) sans rien écrire en base")
	void addEleve_donneesInvalides_retourneMoins3() {
		assertEquals(-3, EleveDao.addEleve(new Eleve("", 0, "Nom", 25, "Adresse")));
		assertEquals(-3, EleveDao.addEleve(new Eleve("TEST002", 0, "", 25, "Adresse")));
		assertEquals(-3, EleveDao.addEleve(new Eleve("TEST003", 0, "Nom", -5, "Adresse")));

		assertEquals(Wrapper.NON_TROUVE, EleveDao.getEleveByNum("TEST002").getCodeResponse());
		assertEquals(Wrapper.NON_TROUVE, EleveDao.getEleveByNum("TEST003").getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] getEleveByNum : un numéro inconnu renvoie le code NON_TROUVE (0) et aucun élève")
	void getEleveByNum_inconnu_codeNonTrouve() {
		EleveWrapper resultat = EleveDao.getEleveByNum("INCONNU999");

		assertEquals(Wrapper.NON_TROUVE, resultat.getCodeResponse());
		assertNull(resultat.getEleve());
	}

	@Test
	@DisplayName("[OK] getEleveByNum : un élève existant est renvoyé avec le code TROUVE (1)")
	void getEleveByNum_existant_codeTrouve() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		EleveWrapper resultat = EleveDao.getEleveByNum("TEST001");

		assertEquals(Wrapper.TROUVE, resultat.getCodeResponse());
		assertEquals("TEST001", resultat.getEleve().getNum());
	}

	@Test
	@DisplayName("[ERREUR] getEleveByNum : un numéro vide est refusé (code -3) sans interroger la base")
	void getEleveByNum_numInvalide_codeDonneesInvalides() {
		EleveWrapper resultat = EleveDao.getEleveByNum("");

		assertEquals(Wrapper.DONNEES_INVALIDES, resultat.getCodeResponse());
		assertNull(resultat.getEleve());
	}

	@Test
	@DisplayName("[OK] updateAdresseEleve : la nouvelle adresse est bien enregistrée")
	void updateAdresseEleve_modifieLAdresse() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Ancienne adresse"));

		int nbLignes = EleveDao.updateAdresseEleve("TEST001", "Nouvelle adresse");

		assertEquals(1, nbLignes);
		assertEquals("Nouvelle adresse", EleveDao.getEleveByNum("TEST001").getEleve().getAdresse());
	}

	@Test
	@DisplayName("[ERREUR] updateAdresseEleve : une adresse vide est refusée (code -3), l'ancienne est conservée")
	void updateAdresseEleve_adresseInvalide_retourneMoins3() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Ancienne adresse"));

		int resultat = EleveDao.updateAdresseEleve("TEST001", "");

		assertEquals(-3, resultat);
		assertEquals("Ancienne adresse", EleveDao.getEleveByNum("TEST001").getEleve().getAdresse());
	}

	@Test
	@DisplayName("[OK] updateNumEleve : l'élève est accessible sous son nouveau numéro, plus sous l'ancien")
	void updateNumEleve_renommeLEleve() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		int nbLignes = EleveDao.updateNumEleve("TEST001", "TEST001B");

		assertEquals(1, nbLignes);
		assertEquals(Wrapper.NON_TROUVE, EleveDao.getEleveByNum("TEST001").getCodeResponse());
		assertEquals(Wrapper.TROUVE, EleveDao.getEleveByNum("TEST001B").getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] updateNumEleve : renommer vers un numéro déjà pris est refusé (code -2)")
	void updateNumEleve_numDejaExistant_retourneMoins2() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse 1"));
		EleveDao.addEleve(new Eleve("TEST002", 0, "Testeur Deux", 30, "Adresse 2"));

		int resultat = EleveDao.updateNumEleve("TEST001", "TEST002");

		assertEquals(-2, resultat);
		assertEquals(Wrapper.TROUVE, EleveDao.getEleveByNum("TEST001").getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] updateNumEleve : un nouveau numéro vide est refusé (code -3)")
	void updateNumEleve_nouveauNumInvalide_retourneMoins3() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		int resultat = EleveDao.updateNumEleve("TEST001", "");

		assertEquals(-3, resultat);
		assertEquals(Wrapper.TROUVE, EleveDao.getEleveByNum("TEST001").getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] getEleveByNo : une chambre sans occupant renvoie le code NON_TROUVE (0)")
	void getEleveByNo_inconnue_codeNonTrouve() {
		assertEquals(Wrapper.NON_TROUVE, EleveDao.getEleveByNo(99999).getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] getEleveByNo : un numéro de chambre à 0 est refusé (code -3)")
	void getEleveByNo_noInvalide_codeDonneesInvalides() {
		assertEquals(Wrapper.DONNEES_INVALIDES, EleveDao.getEleveByNo(0).getCodeResponse());
	}

	@Test
	@DisplayName("[OK] getEleveByNo : renvoie l'élève rattaché à la chambre demandée")
	void getEleveByNo_retourneLOccupant() {
		ChambreDao.addChambre(new Chambre(999, null, 100f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		ChambreDao.updateOccupantChambre(999, "TEST001");

		Eleve occupant = EleveDao.getEleveByNo(999).getEleve();

		assertNotNull(occupant);
		assertEquals("TEST001", occupant.getNum());
	}

	@Test
	@DisplayName("[ERREUR] getElevesByNom : un nom inconnu renvoie une liste vide (pas null, pas d'exception)")
	void getElevesByNom_aucunResultat_retourneListeVide() {
		assertTrue(EleveDao.getElevesByNom("Inconnu").isEmpty());
	}

	@Test
	@DisplayName("[ERREUR] getElevesByAge : un âge sans élève renvoie une liste vide")
	void getElevesByAge_aucunResultat_retourneListeVide() {
		assertTrue(EleveDao.getElevesByAge(999).isEmpty());
	}

	@Test
	@DisplayName("[OK] getElevesByNom : ne renvoie que les élèves portant ce nom")
	void getElevesByNom_filtreCorrectement() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Dupont", 25, "Adresse 1"));
		EleveDao.addEleve(new Eleve("TEST002", 0, "Dupont", 30, "Adresse 2"));
		EleveDao.addEleve(new Eleve("TEST003", 0, "Martin", 40, "Adresse 3"));

		List<Eleve> dupont = EleveDao.getElevesByNom("Dupont");

		assertEquals(2, dupont.size());
	}

	@Test
	@DisplayName("[OK] getElevesByAge : ne renvoie que les élèves ayant cet âge")
	void getElevesByAge_filtreCorrectement() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Dupont", 25, "Adresse 1"));
		EleveDao.addEleve(new Eleve("TEST002", 0, "Martin", 25, "Adresse 2"));
		EleveDao.addEleve(new Eleve("TEST003", 0, "Durand", 40, "Adresse 3"));

		List<Eleve> vingtCinqAns = EleveDao.getElevesByAge(25);

		assertEquals(2, vingtCinqAns.size());
	}

	@Test
	@DisplayName("[OK] getAllEleves : renvoie tous les élèves de la base")
	void getAllEleves_retourneTousLesEleves() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Dupont", 25, "Adresse 1"));
		EleveDao.addEleve(new Eleve("TEST002", 0, "Martin", 30, "Adresse 2"));

		List<Eleve> tous = EleveDao.getAllEleves();

		assertEquals(2, tous.size());
	}

	@Test
	@DisplayName("[OK] deleteEleveByNum : l'élève supprimé n'est plus en base")
	void deleteEleveByNum_supprimeLEleve() {
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		int nbSupprimes = EleveDao.deleteEleveByNum("TEST001");

		assertEquals(1, nbSupprimes);
		assertEquals(Wrapper.NON_TROUVE, EleveDao.getEleveByNum("TEST001").getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] deleteEleveByNum : supprimer un numéro inconnu ne supprime rien (0 ligne)")
	void deleteEleveByNum_inconnu_retourneZero() {
		int nbSupprimes = EleveDao.deleteEleveByNum("INCONNU999");

		assertEquals(0, nbSupprimes);
	}

	@Test
	@DisplayName("[OK] deleteEleveByNum : libère sa chambre et son livre (sans les supprimer) et efface ses inscriptions")
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
		assertEquals(Wrapper.NON_TROUVE, EleveDao.getEleveByNum("TEST001").getCodeResponse());

		Chambre chambre = ChambreDao.getChambreByNo(999).getChambre();
		assertNotNull(chambre);
		assertNull(chambre.getNum());

		Livre livre = LivreDao.getLivreByCote("COTE_TEST").getLivre();
		assertNotNull(livre);
		assertNull(livre.getNum());
		assertNull(livre.getDatepret());

		boolean inscriptionRestante = InscritDao.getAllInscriptions().stream()
				.anyMatch(i -> "TEST001".equals(i.getNum()));
		assertFalse(inscriptionRestante);
	}

}
