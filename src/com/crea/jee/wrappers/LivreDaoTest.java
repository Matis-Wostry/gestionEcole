package com.crea.jee.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Eleve;
import com.crea.jee.beans.Livre;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.LivreDao;

/*
 * Tests unitaires de LivreDao, contre la base ecole_test (-Ddb.name=ecole_test)
 */
class LivreDaoTest {

	@BeforeEach
	@AfterEach
	void viderLesTables() throws SQLException {
		BaseDeTest.viderLesTables();
	}

	@Test
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
	void addLivre_coteDejaExistante_retourneMoins2() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));

		assertEquals(-2, LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Autre titre", null)));
	}

	@Test
	void addLivre_donneesInvalides_retourneMoins3() {
		assertEquals(-3, LivreDao.addLivre(new Livre("", null, "Titre", null)));
		assertEquals(-3, LivreDao.addLivre(new Livre("ISBN_TEST2", null, "  ", null)));

		assertNull(LivreDao.getLivreByCote("ISBN_TEST2"));
	}

	@Test
	void getLivreByCote_inconnu_retourneNull() {
		assertNull(LivreDao.getLivreByCote("INCONNU999"));
	}

	@Test
	void updateEmprunteurLivre_empruntPoseLaDate() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		assertEquals(1, LivreDao.updateEmprunteurLivre("ISBN_TEST1", "TEST001"));

		Livre emprunte = LivreDao.getLivreByCote("ISBN_TEST1");
		assertEquals("TEST001", emprunte.getNum());
		assertNotNull(emprunte.getDatepret());
	}

	@Test
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
	void updateEmprunteurLivre_eleveInexistant_echoue() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));

		assertEquals(-1, LivreDao.updateEmprunteurLivre("ISBN_TEST1", "INCONNU999"));
		assertNull(LivreDao.getLivreByCote("ISBN_TEST1").getNum());
	}

	@Test
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
	void getLivresEmpruntesByEleve_aucunEmprunt_retourneListeVide() {
		assertTrue(LivreDao.getLivresEmpruntesByEleve("INCONNU999").isEmpty());
	}

	@Test
	void updateTitreLivre_modifieLeTitre() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Ancien titre", null));

		assertEquals(1, LivreDao.updateTitreLivre("ISBN_TEST1", "Nouveau titre"));
		assertEquals("Nouveau titre", LivreDao.getLivreByCote("ISBN_TEST1").getTitre());
	}

	@Test
	void updateTitreLivre_titreInvalide_retourneMoins3() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Ancien titre", null));

		assertEquals(-3, LivreDao.updateTitreLivre("ISBN_TEST1", ""));
		assertEquals("Ancien titre", LivreDao.getLivreByCote("ISBN_TEST1").getTitre());
	}

	@Test
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
	void getAllLivres_retourneTousLesLivres() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));
		LivreDao.addLivre(new Livre("ISBN_TEST2", null, "Titre Deux", null));

		assertEquals(2, LivreDao.getAllLivres().size());
	}

	@Test
	void deleteLivreByCote_supprimeLeLivre() {
		LivreDao.addLivre(new Livre("ISBN_TEST1", null, "Titre Un", null));

		assertEquals(1, LivreDao.deleteLivreByCote("ISBN_TEST1"));
		assertNull(LivreDao.getLivreByCote("ISBN_TEST1"));
	}

	@Test
	void deleteLivreByCote_inconnu_retourneZero() {
		assertEquals(0, LivreDao.deleteLivreByCote("INCONNU999"));
	}

}
