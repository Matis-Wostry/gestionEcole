package com.crea.jee.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Chambre;
import com.crea.jee.beans.Eleve;
import com.crea.jee.dao.ChambreDao;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.wrappers.ChambreWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Tests unitaires de ChambreDao, contre la base ecole_test (-Ddb.name=ecole_test)
 * [OK] = cas valide, l'opération doit réussir ; [ERREUR] = cas invalide, le DAO doit refuser ou ne rien trouver
 */
@DisplayName("ChambreDao")
class ChambreDaoTest {

	private static final float DELTA = 0.001f;

	@BeforeEach
	@AfterEach
	void viderLesTables() throws SQLException {
		BaseDeTest.viderLesTables();
	}

	@Test
	@DisplayName("[OK] addChambre : une chambre ajoutée se retrouve en base, libre et au bon prix")
	void addChambre_ajouteEtRecuperable() {
		int resultat = ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(1, resultat);
		Chambre recuperee = ChambreDao.getChambreByNo(1).getChambre();
		assertNotNull(recuperee);
		assertNull(recuperee.getNum());
		assertEquals(350.25f, recuperee.getPrix(), DELTA);
	}

	@Test
	@DisplayName("[ERREUR] addChambre : ajouter un numéro de chambre déjà existant est refusé (code -2)")
	void addChambre_noDejaExistant_retourneMoins2() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(-2, ChambreDao.addChambre(new Chambre(1, null, 200f)));
	}

	@Test
	@DisplayName("[ERREUR] addChambre : un numéro à 0 ou un prix négatif sont refusés (code -3)")
	void addChambre_donneesInvalides_retourneMoins3() {
		assertEquals(-3, ChambreDao.addChambre(new Chambre(0, null, 100f)));
		assertEquals(-3, ChambreDao.addChambre(new Chambre(2, null, -10f)));

		assertEquals(Wrapper.NON_TROUVE, ChambreDao.getChambreByNo(2).getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] getChambreByNo : un numéro inconnu renvoie le code NON_TROUVE (0) et aucune chambre")
	void getChambreByNo_inconnue_codeNonTrouve() {
		ChambreWrapper resultat = ChambreDao.getChambreByNo(99999);

		assertEquals(Wrapper.NON_TROUVE, resultat.getCodeResponse());
		assertNull(resultat.getChambre());
	}

	@Test
	@DisplayName("[OK] getChambreByNo : une chambre existante est renvoyée avec le code TROUVE (1)")
	void getChambreByNo_existante_codeTrouve() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		ChambreWrapper resultat = ChambreDao.getChambreByNo(1);

		assertEquals(Wrapper.TROUVE, resultat.getCodeResponse());
		assertEquals(1, resultat.getChambre().getNo());
	}

	@Test
	@DisplayName("[ERREUR] getChambreByNo : un numéro à 0 est refusé (code -3) sans interroger la base")
	void getChambreByNo_noInvalide_codeDonneesInvalides() {
		ChambreWrapper resultat = ChambreDao.getChambreByNo(0);

		assertEquals(Wrapper.DONNEES_INVALIDES, resultat.getCodeResponse());
		assertNull(resultat.getChambre());
	}

	@Test
	@DisplayName("[OK] updateOccupantChambre : on peut attribuer la chambre à un élève puis la libérer")
	void updateOccupantChambre_attribueEtLibere() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));

		assertEquals(1, ChambreDao.updateOccupantChambre(1, "TEST001"));
		assertEquals("TEST001", ChambreDao.getChambreByNo(1).getChambre().getNum());

		assertEquals(1, ChambreDao.updateOccupantChambre(1, null));
		assertNull(ChambreDao.getChambreByNo(1).getChambre().getNum());
	}

	@Test
	@DisplayName("[ERREUR] updateOccupantChambre : attribuer la chambre à un élève inexistant échoue (code -1)")
	void updateOccupantChambre_eleveInexistant_echoue() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(-1, ChambreDao.updateOccupantChambre(1, "INCONNU999"));
		assertNull(ChambreDao.getChambreByNo(1).getChambre().getNum());
	}

	@Test
	@DisplayName("[OK] getChambreByOccupant : renvoie la chambre occupée par l'élève")
	void getChambreByOccupant_retourneLaChambre() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		ChambreDao.updateOccupantChambre(1, "TEST001");

		Chambre chambre = ChambreDao.getChambreByOccupant("TEST001").getChambre();

		assertNotNull(chambre);
		assertEquals(1, chambre.getNo());
	}

	@Test
	@DisplayName("[ERREUR] getChambreByOccupant : un élève sans chambre renvoie le code NON_TROUVE (0)")
	void getChambreByOccupant_sansChambre_codeNonTrouve() {
		assertEquals(Wrapper.NON_TROUVE, ChambreDao.getChambreByOccupant("INCONNU999").getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] getChambreByOccupant : un numéro d'élève vide est refusé (code -3)")
	void getChambreByOccupant_numInvalide_codeDonneesInvalides() {
		assertEquals(Wrapper.DONNEES_INVALIDES, ChambreDao.getChambreByOccupant("").getCodeResponse());
	}

	@Test
	@DisplayName("[OK] updatePrixChambre : le nouveau prix est bien enregistré")
	void updatePrixChambre_modifieLePrix() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(1, ChambreDao.updatePrixChambre(1, 400.5f));
		assertEquals(400.5f, ChambreDao.getChambreByNo(1).getChambre().getPrix(), DELTA);
	}

	@Test
	@DisplayName("[ERREUR] updatePrixChambre : un prix à 0 est refusé (code -3), l'ancien prix est conservé")
	void updatePrixChambre_prixInvalide_retourneMoins3() {
		ChambreDao.addChambre(new Chambre(1, null, 350.25f));

		assertEquals(-3, ChambreDao.updatePrixChambre(1, 0f));
		assertEquals(350.25f, ChambreDao.getChambreByNo(1).getChambre().getPrix(), DELTA);
	}

	@Test
	@DisplayName("[OK] getChambresPrixSuperieur : ne renvoie que les chambres strictement plus chères que le seuil")
	void getChambresPrixSuperieur_filtreStrictement() {
		ChambreDao.addChambre(new Chambre(1, null, 150f));
		ChambreDao.addChambre(new Chambre(2, null, 300f));
		ChambreDao.addChambre(new Chambre(3, null, 400f));

		List<Chambre> cheres = ChambreDao.getChambresPrixSuperieur(300f);

		assertEquals(1, cheres.size());
		assertEquals(3, cheres.get(0).getNo());
	}

	@Test
	@DisplayName("[OK] getAllChambres : renvoie toutes les chambres de la base")
	void getAllChambres_retourneToutesLesChambres() {
		ChambreDao.addChambre(new Chambre(1, null, 150f));
		ChambreDao.addChambre(new Chambre(2, null, 300f));

		assertEquals(2, ChambreDao.getAllChambres().size());
	}

	@Test
	@DisplayName("[OK] getChambresNonOccupees : ne renvoie que les chambres sans occupant")
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
	@DisplayName("[OK] deleteChambreByNo : la chambre supprimée n'est plus en base")
	void deleteChambreByNo_supprimeLaChambre() {
		ChambreDao.addChambre(new Chambre(1, null, 150f));

		assertEquals(1, ChambreDao.deleteChambreByNo(1));
		assertEquals(Wrapper.NON_TROUVE, ChambreDao.getChambreByNo(1).getCodeResponse());
	}

	@Test
	@DisplayName("[ERREUR] deleteChambreByNo : supprimer un numéro inconnu ne supprime rien (0 ligne)")
	void deleteChambreByNo_inconnue_retourneZero() {
		assertEquals(0, ChambreDao.deleteChambreByNo(99999));
	}

	@Test
	@DisplayName("[OK] deleteChambreByNo : l'élève rattaché est détaché de la chambre au lieu de bloquer la suppression")
	void deleteChambreByNo_detacheLEleveRattache() throws SQLException {
		ChambreDao.addChambre(new Chambre(1, null, 150f));
		EleveDao.addEleve(new Eleve("TEST001", 0, "Testeur Un", 25, "Adresse"));
		BaseDeTest.executer("UPDATE eleve SET no = ? WHERE num = ?", 1, "TEST001");

		assertEquals(1, ChambreDao.deleteChambreByNo(1));

		assertEquals(Wrapper.NON_TROUVE, ChambreDao.getChambreByNo(1).getCodeResponse());
		Eleve eleve = EleveDao.getEleveByNum("TEST001").getEleve();
		assertNotNull(eleve);
		assertEquals(0, eleve.getNo());
		assertEquals(Wrapper.NON_TROUVE, EleveDao.getEleveByNo(1).getCodeResponse());
	}

}
