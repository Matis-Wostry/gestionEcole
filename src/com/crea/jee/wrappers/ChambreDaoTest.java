package com.crea.jee.wrappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Chambre;
import com.crea.jee.beans.Eleve;
import com.crea.jee.dao.ChambreDao;
import com.crea.jee.dao.EleveDao;

/*
 * Tests unitaires de ChambreDao, contre la base ecole_test (-Ddb.name=ecole_test)
 */
class ChambreDaoTest {

	private static final float DELTA = 0.001f;

	@BeforeEach
	@AfterEach
	void viderLesTables() throws SQLException {
		BaseDeTest.viderLesTables();
	}

	@Test
	void addChambre_ajouteEtRecuperable() {
		int resultat = ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(1, resultat);
		Chambre recuperee = ChambreDao.getChambreByNo(1);
		assertNotNull(recuperee);
		assertNull(recuperee.getNum());
		assertEquals(350.25f, recuperee.getPrix(), DELTA);
	}

	@Test
	void addChambre_noDejaExistant_retourneMoins2() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(-2, ChambreDao.addChambre(new Chambre(1, null, 200f)));
	}

	@Test
	void addChambre_donneesInvalides_retourneMoins3() {
		assertEquals(-3, ChambreDao.addChambre(new Chambre(0, null, 100f)));
		assertEquals(-3, ChambreDao.addChambre(new Chambre(2, null, -10f)));

		assertNull(ChambreDao.getChambreByNo(2));
	}

	@Test
	void getChambreByNo_inconnue_retourneNull() {
		assertNull(ChambreDao.getChambreByNo(99999));
	}

	@Test
	void updateOccupantChambre_attribueEtLibere() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		assertEquals(1, ChambreDao.updateOccupantChambre(1, "TEST001"));
		assertEquals("TEST001", ChambreDao.getChambreByNo(1).getNum());

		assertEquals(1, ChambreDao.updateOccupantChambre(1, null));
		assertNull(ChambreDao.getChambreByNo(1).getNum());
	}

	@Test
	void updateOccupantChambre_eleveInexistant_echoue() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(-1, ChambreDao.updateOccupantChambre(1, "INCONNU999"));
		assertNull(ChambreDao.getChambreByNo(1).getNum());
	}

	@Test
	void getChambreByOccupant_retourneLaChambre() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		ChambreDao.updateOccupantChambre(1, "TEST001");

		Chambre chambre = ChambreDao.getChambreByOccupant("TEST001");

		assertNotNull(chambre);
		assertEquals(1, chambre.getNo());
	}

	@Test
	void getChambreByOccupant_sansChambre_retourneNull() {
		assertNull(ChambreDao.getChambreByOccupant("INCONNU999"));
	}

	@Test
	void updatePrixChambre_modifieLePrix() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(1, ChambreDao.updatePrixChambre(1, 400.5f));
		assertEquals(400.5f, ChambreDao.getChambreByNo(1).getPrix(), DELTA);
	}

	@Test
	void updatePrixChambre_prixInvalide_retourneMoins3() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(-3, ChambreDao.updatePrixChambre(1, 0f));
		assertEquals(350.25f, ChambreDao.getChambreByNo(1).getPrix(), DELTA);
	}

	@Test
	void getChambresPrixSuperieur_filtreStrictement() {
		ChambreDao.addChambre(new Chambre(1, null, 150f));
		ChambreDao.addChambre(new Chambre(2, null, 300f));
		ChambreDao.addChambre(new Chambre(3, null, 400f));

		List<Chambre> cheres = ChambreDao.getChambresPrixSuperieur(300f);

		assertEquals(1, cheres.size());
		assertEquals(3, cheres.get(0).getNo());
	}

	@Test
	void getAllChambres_retourneToutesLesChambres() {
		ChambreDao.addChambre(new Chambre(1, null, 150f));
		ChambreDao.addChambre(new Chambre(2, null, 300f));

		assertEquals(2, ChambreDao.getAllChambres().size());
	}

	@Test
	void getChambresNonOccupees_exclutLesChambresOccupees() {
		ChambreDao.addChambre(new Chambre(1, null, 150f));
		ChambreDao.addChambre(new Chambre(2, null, 300f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		ChambreDao.updateOccupantChambre(1, "TEST001");

		List<Chambre> libres = ChambreDao.getChambresNonOccupees();

		assertEquals(1, libres.size());
		assertEquals(2, libres.get(0).getNo());
	}

	@Test
	void deleteChambreByNo_supprimeLaChambre() {
		ChambreDao.addChambre(new Chambre(1, null, 150f));

		assertEquals(1, ChambreDao.deleteChambreByNo(1));
		assertNull(ChambreDao.getChambreByNo(1));
	}

	@Test
	void deleteChambreByNo_inconnue_retourneZero() {
		assertEquals(0, ChambreDao.deleteChambreByNo(99999));
	}

	// la transaction doit détacher l'élève (eleve.no = NULL) au lieu d'échouer sur la clé étrangère
	@Test
	void deleteChambreByNo_detacheLEleveRattache() throws SQLException {
		ChambreDao.addChambre(new Chambre(1, null, 150f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		BaseDeTest.executer("UPDATE eleve SET no = ? WHERE num = ?", 1, "TEST001");

		assertEquals(1, ChambreDao.deleteChambreByNo(1));

		assertNull(ChambreDao.getChambreByNo(1));
		Eleve eleve = EleveDao.getEleveByNum("TEST001");
		assertNotNull(eleve);
		assertEquals(0, eleve.getNo());
		assertNull(EleveDao.getEleveByNo(1));
	}

}
