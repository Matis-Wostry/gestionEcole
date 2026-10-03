package com.crea.jee.junit;

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
import com.crea.jee.beans.Uv;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.InscritDao;
import com.crea.jee.dao.UvDao;
import com.crea.jee.wrappers.UvWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Tests unitaires de UvDao, contre la base ecole_test (-Ddb.name=ecole_test)
 * UvDao n'a pas de méthode d'ajout : les uv de test sont insérées directement en SQL
 * [OK] = cas valide, l'opération doit réussir ; [ERREUR] = cas invalide, le DAO doit refuser ou ne rien trouver
 */
@DisplayName("UvDao")
class UvDaoTest {

	@BeforeEach
	@AfterEach
	void viderLesTables() throws SQLException {
		BaseDeTest.viderLesTables();
	}

	private static void ajouterUv(String code, int nbh, String coord) throws SQLException {
		BaseDeTest.executer("INSERT INTO uv (code, nbh, coord) VALUES (?, ?, ?)", code, nbh, coord);
	}

	@Test
	@DisplayName("[OK] getUvByCode : renvoie l'UV avec son nombre d'heures et son coordinateur")
	void getUvByCode_retourneLUv() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");

		Uv uv = UvDao.getUvByCode("UV_TEST1").getUv();

		assertNotNull(uv);
		assertEquals(30, uv.getNbh());
		assertEquals("Mr Test", uv.getCoord());
	}

	@Test
	@DisplayName("[ERREUR] getUvByCode : un code inconnu renvoie le code NON_TROUVE (0) et aucune UV")
	void getUvByCode_inconnue_codeNonTrouve() {
		UvWrapper resultat = UvDao.getUvByCode("INCONNU999");

		assertEquals(Wrapper.NON_TROUVE, resultat.getCodeResponse());
		assertNull(resultat.getUv());
	}

	@Test
	@DisplayName("[OK] getUvByCode : une UV existante est renvoyée avec le code TROUVE (1)")
	void getUvByCode_existante_codeTrouve() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");

		UvWrapper resultat = UvDao.getUvByCode("UV_TEST1");

		assertEquals(Wrapper.TROUVE, resultat.getCodeResponse());
		assertEquals("UV_TEST1", resultat.getUv().getCode());
	}

	@Test
	@DisplayName("[ERREUR] getUvByCode : un code vide est refusé (code -3) sans interroger la base")
	void getUvByCode_codeInvalide_codeDonneesInvalides() {
		UvWrapper resultat = UvDao.getUvByCode("");

		assertEquals(Wrapper.DONNEES_INVALIDES, resultat.getCodeResponse());
		assertNull(resultat.getUv());
	}

	@Test
	@DisplayName("[OK] updateNbhUv : le nouveau nombre d'heures est bien enregistré")
	void updateNbhUv_modifieLeNombreDHeures() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");

		assertEquals(1, UvDao.updateNbhUv("UV_TEST1", 45));
		assertEquals(45, UvDao.getUvByCode("UV_TEST1").getUv().getNbh());
	}

	@Test
	@DisplayName("[ERREUR] updateNbhUv : 0 heure ou plus de 127 heures (limite du tinyint) sont refusés (code -3)")
	void updateNbhUv_valeurInvalide_retourneMoins3() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");

		assertEquals(-3, UvDao.updateNbhUv("UV_TEST1", 0));
		assertEquals(-3, UvDao.updateNbhUv("UV_TEST1", 200));
		assertEquals(30, UvDao.getUvByCode("UV_TEST1").getUv().getNbh());
	}

	@Test
	@DisplayName("[OK] updateCoordUv : le nouveau coordinateur est bien enregistré")
	void updateCoordUv_modifieLeCoordinateur() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");

		assertEquals(1, UvDao.updateCoordUv("UV_TEST1", "Mme Nouvelle"));
		assertEquals("Mme Nouvelle", UvDao.getUvByCode("UV_TEST1").getUv().getCoord());
	}

	@Test
	@DisplayName("[ERREUR] updateCoordUv : un coordinateur de plus de 255 caractères est refusé (code -3)")
	void updateCoordUv_tropLong_retourneMoins3() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");

		assertEquals(-3, UvDao.updateCoordUv("UV_TEST1", "x".repeat(256)));
		assertEquals("Mr Test", UvDao.getUvByCode("UV_TEST1").getUv().getCoord());
	}

	@Test
	@DisplayName("[OK] getAllUvs : renvoie toutes les UV de la base")
	void getAllUvs_retourneToutesLesUvs() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");
		ajouterUv("UV_TEST2", 10, "Mme Test");

		assertEquals(2, UvDao.getAllUvs().size());
	}

	@Test
	@DisplayName("[OK] getUvsNbhSuperieur : ne renvoie que les UV strictement au-dessus du seuil d'heures")
	void getUvsNbhSuperieur_filtreStrictement() throws SQLException {
		ajouterUv("UV_TEST1", 10, "Mr Test");
		ajouterUv("UV_TEST2", 26, "Mme Test");
		ajouterUv("UV_TEST3", 30, "Mr Autre");

		List<Uv> longues = UvDao.getUvsNbhSuperieur(26);

		assertEquals(1, longues.size());
		assertEquals("UV_TEST3", longues.get(0).getCode());
	}

	@Test
	@DisplayName("[ERREUR] getUvsNbhSuperieur : un seuil qu'aucune UV ne dépasse renvoie une liste vide")
	void getUvsNbhSuperieur_aucunResultat_retourneListeVide() throws SQLException {
		ajouterUv("UV_TEST1", 10, "Mr Test");

		assertTrue(UvDao.getUvsNbhSuperieur(100).isEmpty());
	}

	@Test
	@DisplayName("[OK] deleteUvByCode : l'UV supprimée n'est plus en base")
	void deleteUvByCode_supprimeLUv() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");

		assertEquals(1, UvDao.deleteUvByCode("UV_TEST1"));
		assertEquals(Wrapper.NON_TROUVE, UvDao.getUvByCode("UV_TEST1").getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] deleteUvByCode : supprimer un code inconnu ne supprime rien (0 ligne)")
	void deleteUvByCode_inconnue_retourneZero() {
		assertEquals(0, UvDao.deleteUvByCode("INCONNU999"));
	}

	@Test
	@DisplayName("[OK] deleteUvByCode : efface les inscriptions de cette UV sans toucher à celles des autres UV")
	void deleteUvByCode_supprimeSesInscriptions() throws SQLException {
		ajouterUv("UV_TEST1", 30, "Mr Test");
		ajouterUv("UV_TEST2", 10, "Mme Test");
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		BaseDeTest.executer("INSERT INTO inscrit (code, num, note) VALUES (?, ?, ?)", "UV_TEST1", "TEST001", 12f);
		BaseDeTest.executer("INSERT INTO inscrit (code, num, note) VALUES (?, ?, ?)", "UV_TEST2", "TEST001", 14f);

		assertEquals(1, UvDao.deleteUvByCode("UV_TEST1"));

		assertEquals(Wrapper.NON_TROUVE, UvDao.getUvByCode("UV_TEST1").getCodeResponse());
		assertEquals(1, InscritDao.getAllInscriptions().size());
		assertEquals("UV_TEST2", InscritDao.getAllInscriptions().get(0).getCode());
	}

}
