package com.crea.jee.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Eleve;
import com.crea.jee.beans.Livre;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.LivreDao;

/*
 * Tests unitaires de LivreDao, contre la base ecole_test (-Ddb.name=ecole_test)
 * [OK] = cas valide, l'opération doit réussir ; [ERREUR] = cas invalide, le DAO doit refuser ou ne rien trouver
 */
@DisplayName("LivreDao")
class LivreDaoTest {

	@BeforeEach
	@AfterEach
	void viderLesTables() throws SQLException {
		BaseDeTest.viderLesTables();
	}

	@Test
	@DisplayName("[OK] addLivre : un livre ajouté se retrouve en base, disponible (sans emprunteur ni date)")
	void addLivre_ajouteDisponible() {
		int resultat = LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));

		assertEquals(1, resultat);
		Livre recupere = LivreDao.getLivreByCote("ISBN_TEST1");
		assertNotNull(recupere);
		assertEquals("Titre Un", recupere.getTitre());
		assertNull(recupere.getNum());
		assertNull(recupere.getDatepret());
	}

	@Test
	@DisplayName("[ERREUR] addLivre : ajouter une cote déjà existante est refusé (code -2)")
	void addLivre_coteDejaExistante_retourneMoins2() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));

		assertEquals(-2, LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Autre titre", null)));
	}

	@Test
	@DisplayName("[ERREUR] addLivre : une cote vide ou un titre vide sont refusés (code -3)")
	void addLivre_donneesInvalides_retourneMoins3() {
		assertEquals(-3, LivreDao.addLivre(new Livre("", null, "Titre", null)));
		assertEquals(-3, LivreDao.addLivre(new Livre("ISBN_TEST2", null, "  ", null)));

		assertNull(LivreDao.getLivreByCote("ISBN_TEST2"));
	}

	@Test
	@DisplayName("[ERREUR] getLivreByCote : une cote inconnue renvoie null")
	void getLivreByCote_inconnu_retourneNull() {
		assertNull(LivreDao.getLivreByCote("INCONNU999"));
	}

	@Test
	@DisplayName("[OK] updateEmprunteurLivre : un emprunt enregistre l'élève et la date de prêt")
	void updateEmprunteurLivre_empruntPoseLaDate() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		assertEquals(1, LivreDao.updateEmprunteurLivre("ISBN_TEST1", "TEST001"));

		Livre emprunte = LivreDao.getLivreByCote("ISBN_TEST1");
		assertEquals("TEST001", emprunte.getNum());
		assertNotNull(emprunte.getDatepret());
	}

	@Test
	@DisplayName("[OK] updateEmprunteurLivre : un retour remet l'emprunteur et la date de prêt à vide")
	void updateEmprunteurLivre_retourRemetLaDateANull() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		LivreDao.updateEmprunteurLivre("ISBN_TEST1", "TEST001");

		assertEquals(1, LivreDao.updateEmprunteurLivre("ISBN_TEST1", null));

		Livre rendu = LivreDao.getLivreByCote("ISBN_TEST1");
		assertNull(rendu.getNum());
		assertNull(rendu.getDatepret());
	}

	@Test
	@DisplayName("[ERREUR] updateEmprunteurLivre : un emprunt par un élève inexistant échoue (code -1)")
	void updateEmprunteurLivre_eleveInexistant_echoue() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));

		assertEquals(-1, LivreDao.updateEmprunteurLivre("ISBN_TEST1", "INCONNU999"));
		assertNull(LivreDao.getLivreByCote("ISBN_TEST1").getNum());
	}

	@Test
	@DisplayName("[OK] getLivresEmpruntesByEleve : renvoie uniquement les livres empruntés par l'élève")
	void getLivresEmpruntesByEleve_retourneSesLivres() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));
		LivreDao.addLivre(new Livre("ISBN_TEST2", null, "Titre Deux", null));
		LivreDao.addLivre(new Livre("ISBN_TEST3", null, "Titre Trois", null));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		LivreDao.updateEmprunteurLivre("ISBN_TEST1", "TEST001");
		LivreDao.updateEmprunteurLivre("ISBN_TEST2", "TEST001");

		assertEquals(2, LivreDao.getLivresEmpruntesByEleve("TEST001").size());
	}

	@Test
	@DisplayName("[ERREUR] getLivresEmpruntesByEleve : un élève sans emprunt renvoie une liste vide")
	void getLivresEmpruntesByEleve_aucunEmprunt_retourneListeVide() {
		assertTrue(LivreDao.getLivresEmpruntesByEleve("INCONNU999").isEmpty());
	}

	@Test
	@DisplayName("[OK] updateTitreLivre : le nouveau titre est bien enregistré")
	void updateTitreLivre_modifieLeTitre() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Ancien titre", null));

		assertEquals(1, LivreDao.updateTitreLivre("ISBN_TEST1", "Nouveau titre"));
		assertEquals("Nouveau titre", LivreDao.getLivreByCote("ISBN_TEST1").getTitre());
	}

	@Test
	@DisplayName("[ERREUR] updateTitreLivre : un titre vide est refusé (code -3), l'ancien est conservé")
	void updateTitreLivre_titreInvalide_retourneMoins3() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Ancien titre", null));

		assertEquals(-3, LivreDao.updateTitreLivre("ISBN_TEST1", ""));
		assertEquals("Ancien titre", LivreDao.getLivreByCote("ISBN_TEST1").getTitre());
	}

	@Test
	@DisplayName("[OK] getLivresDisponibles : ne renvoie que les livres non empruntés")
	void getLivresDisponibles_exclutLesLivresEmpruntes() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));
		LivreDao.addLivre(new Livre("ISBN_TEST2", null, "Titre Deux", null));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		LivreDao.updateEmprunteurLivre("ISBN_TEST1", "TEST001");

		List<Livre> disponibles = LivreDao.getLivresDisponibles();

		assertEquals(1, disponibles.size());
		assertEquals("ISBN_TEST2", disponibles.get(0).getCote());
	}

	@Test
	@DisplayName("[OK] getAllLivres : renvoie tous les livres de la base")
	void getAllLivres_retourneTousLesLivres() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));
		LivreDao.addLivre(new Livre("ISBN_TEST2", null, "Titre Deux", null));

		assertEquals(2, LivreDao.getAllLivres().size());
	}

	@Test
	@DisplayName("[OK] deleteLivreByCote : le livre supprimé n'est plus en base")
	void deleteLivreByCote_supprimeLeLivre() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));

		assertEquals(1, LivreDao.deleteLivreByCote("ISBN_TEST1"));
		assertNull(LivreDao.getLivreByCote("ISBN_TEST1"));
	}

	@Test
	@DisplayName("[ERREUR] deleteLivreByCote : supprimer une cote inconnue ne supprime rien (0 ligne)")
	void deleteLivreByCote_inconnu_retourneZero() {
		assertEquals(0, LivreDao.deleteLivreByCote("INCONNU999"));
	}

}
