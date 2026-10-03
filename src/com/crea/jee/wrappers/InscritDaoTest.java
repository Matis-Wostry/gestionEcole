package com.crea.jee.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Eleve;
import com.crea.jee.beans.Inscrit;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.InscritDao;

/*
 * Tests unitaires de InscritDao, contre la base ecole_test (-Ddb.name=ecole_test)
 * InscritDao n'a pas de méthode d'ajout : uv et inscriptions de test sont insérées directement en SQL
 * [OK] = cas valide, l'opération doit réussir ; [ERREUR] = cas invalide, le DAO doit refuser ou ne rien trouver
 */
@DisplayName("InscritDao")
class InscritDaoTest {

	private static final float DELTA = 0.001f;

	@BeforeEach
	void preparerLesDonnees() throws SQLException {
		BaseDeTest.viderLesTables();
		BaseDeTest.executer("INSERT INTO uv (code, nbh, coord) VALUES (?, ?, ?)", "UV_TEST1", 30, "Mr Test");
		BaseDeTest.executer("INSERT INTO uv (code, nbh, coord) VALUES (?, ?, ?)", "UV_TEST2", 10, "Mme Test");
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
	}

	@AfterEach
	void viderLesTables() throws SQLException {
		BaseDeTest.viderLesTables();
	}

	private static void inscrire(String code, String num, float note) throws SQLException {
		BaseDeTest.executer("INSERT INTO inscrit (code, num, note) VALUES (?, ?, ?)", code, num, note);
	}

	private static Inscrit trouver(String code, String num) {
		return InscritDao.getAllInscriptions().stream()
				.filter(i -> code.equals(i.getCode()) && num.equals(i.getNum()))
				.findFirst()
				.orElse(null);
	}

	@Test
	@DisplayName("[ERREUR] getAllInscriptions : sans aucune inscription, renvoie une liste vide")
	void getAllInscriptions_baseVide_retourneListeVide() {
		assertTrue(InscritDao.getAllInscriptions().isEmpty());
	}

	@Test
	@DisplayName("[OK] getAllInscriptions : renvoie toutes les inscriptions avec leur note")
	void getAllInscriptions_retourneToutesLesInscriptions() throws SQLException {
		inscrire("UV_TEST1", "TEST001", 12f);
		inscrire("UV_TEST2", "TEST001", 15.5f);

		List<Inscrit> inscriptions = InscritDao.getAllInscriptions();

		assertEquals(2, inscriptions.size());
		assertEquals(15.5f, trouver("UV_TEST2", "TEST001").getNote(), DELTA);
	}

	@Test
	@DisplayName("[OK] updateNoteInscrit : la nouvelle note est bien enregistrée")
	void updateNoteInscrit_modifieLaNote() throws SQLException {
		inscrire("UV_TEST1", "TEST001", 12f);

		assertEquals(1, InscritDao.updateNoteInscrit("UV_TEST1", "TEST001", 17.5f));
		assertEquals(17.5f, trouver("UV_TEST1", "TEST001").getNote(), DELTA);
	}

	@Test
	@DisplayName("[OK] updateNoteInscrit : seule la note de l'UV visée change, pas celle des autres UV de l'élève")
	void updateNoteInscrit_neToucheQueLInscriptionCiblee() throws SQLException {
		inscrire("UV_TEST1", "TEST001", 12f);
		inscrire("UV_TEST2", "TEST001", 14f);

		InscritDao.updateNoteInscrit("UV_TEST1", "TEST001", 18f);

		assertEquals(14f, trouver("UV_TEST2", "TEST001").getNote(), DELTA);
	}

	@Test
	@DisplayName("[ERREUR] updateNoteInscrit : modifier une inscription inexistante ne modifie rien (0 ligne)")
	void updateNoteInscrit_inscriptionInconnue_retourneZero() {
		assertEquals(0, InscritDao.updateNoteInscrit("UV_TEST1", "INCONNU999", 10f));
	}

	@Test
	@DisplayName("[OK] deleteInscription : supprime seulement l'inscription visée, les autres restent")
	void deleteInscription_supprimeUniquementLInscriptionCiblee() throws SQLException {
		inscrire("UV_TEST1", "TEST001", 12f);
		inscrire("UV_TEST2", "TEST001", 14f);

		assertEquals(1, InscritDao.deleteInscription("UV_TEST1", "TEST001"));

		List<Inscrit> restantes = InscritDao.getAllInscriptions();
		assertEquals(1, restantes.size());
		assertEquals("UV_TEST2", restantes.get(0).getCode());
	}

	@Test
	@DisplayName("[ERREUR] deleteInscription : supprimer une inscription inexistante ne supprime rien (0 ligne)")
	void deleteInscription_inconnue_retourneZero() {
		assertEquals(0, InscritDao.deleteInscription("UV_TEST1", "INCONNU999"));
	}

}
