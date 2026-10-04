package com.crea.jee.junit;

import static com.crea.jee.junit.OutilsIhm.cliquer;
import static com.crea.jee.junit.OutilsIhm.lignes;
import static com.crea.jee.junit.OutilsIhm.onglet;
import static com.crea.jee.junit.OutilsIhm.selectionner;
import static com.crea.jee.junit.OutilsIhm.valeur;
import static com.crea.jee.junit.OutilsIhm.verifierStatut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Chambre;
import com.crea.jee.beans.Eleve;
import com.crea.jee.beans.Inscrit;
import com.crea.jee.beans.Livre;
import com.crea.jee.beans.Uv;
import com.crea.jee.dao.ChambreDao;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.InscritDao;
import com.crea.jee.dao.LivreDao;
import com.crea.jee.dao.UvDao;
import com.crea.jee.ihm.FenetreEcole;

/*
 * Tests de la fenêtre principale : présence des onglets et répercussion d'une action d'un onglet sur les autres
 * Contre la base ecole_test (-Ddb.name=ecole_test)
 */
@DisplayName("Interface : fenêtre principale et liens entre onglets")
class FenetreEcoleTest {

	private static final int ELEVES = 0;
	private static final int CHAMBRES = 1;
	private static final int LIVRES = 2;
	private static final int UV = 3;
	private static final int INSCRIPTIONS = 4;

	private FenetreEcole fenetre;

	@BeforeEach
	void ouvrir() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "Tests d'interface ignorés : aucun écran disponible");
		BaseDeTest.viderLesTables();
	}

	@AfterEach
	void fermer() throws Exception {
		OutilsIhm.fermer(fenetre);
		BaseDeTest.viderLesTables();
	}

	@Test
	@DisplayName("[OK] La fenêtre propose les cinq onglets, dans l'ordre, et s'ouvre sur les élèves déjà chargés")
	void ouverture() throws Exception {
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));

		fenetre = OutilsIhm.ouvrir();

		assertEquals(List.of("Élèves", "Chambres", "Livres", "UV", "Inscriptions"), OutilsIhm.titresOnglets(fenetre));
		assertEquals(1, lignes(onglet(fenetre, ELEVES)));
	}

	@Test
	@DisplayName("[OK] Supprimer un élève : les onglets Chambres, Livres et Inscriptions le reflètent dès qu'on les ouvre")
	void suppressionEleve_repercuteeDansLesAutresOnglets() throws Exception {
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));
		ChambreDao.addChambre(new Chambre(1, null, 300f));
		ChambreDao.updateOccupantChambre(1, "TEST1");
		LivreDao.addLivre(new Livre("L1", null, "Le Petit Prince", null));
		LivreDao.updateEmprunteurLivre("L1", "TEST1");
		UvDao.addUv(new Uv("UV1", 30, "Mr Test"));
		InscritDao.addInscription(new Inscrit("UV1", "TEST1", 12f));
		fenetre = OutilsIhm.ouvrir();

		Object chambres = onglet(fenetre, CHAMBRES);
		assertEquals("TEST1", valeur(chambres, 1, 1));
		Object eleves = onglet(fenetre, ELEVES);
		selectionner(eleves, "TEST1");
		cliquer(eleves, "supprimerSelection");
		verifierStatut(eleves, "Élève TEST1 supprimé (code 1)");

		assertEquals("—", valeur(onglet(fenetre, CHAMBRES), 1, 1));
		assertEquals("—", valeur(onglet(fenetre, LIVRES), "L1", 2));
		assertEquals(0, lignes(onglet(fenetre, INSCRIPTIONS)));
	}

	@Test
	@DisplayName("[OK] Supprimer une UV : ses inscriptions disparaissent de l'onglet Inscriptions, les autres restent")
	void suppressionUv_repercuteeDansInscriptions() throws Exception {
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));
		UvDao.addUv(new Uv("UV1", 30, "Mr Test"));
		UvDao.addUv(new Uv("UV2", 12, "Mme Test"));
		InscritDao.addInscription(new Inscrit("UV1", "TEST1", 12f));
		InscritDao.addInscription(new Inscrit("UV2", "TEST1", 14f));
		fenetre = OutilsIhm.ouvrir();

		assertEquals(2, lignes(onglet(fenetre, INSCRIPTIONS)));
		Object uvs = onglet(fenetre, UV);
		selectionner(uvs, "UV1");
		cliquer(uvs, "supprimerSelection");
		verifierStatut(uvs, "supprimée avec ses inscriptions (code 1)");

		Object inscriptions = onglet(fenetre, INSCRIPTIONS);
		assertEquals(1, lignes(inscriptions));
		assertEquals("TEST1", valeur(inscriptions, "UV2", 1));
	}

	@Test
	@DisplayName("[OK] Attribuer puis libérer une chambre : la colonne « Chambre » de l'onglet Élèves suit")
	void attributionChambre_visibleDansEleves() throws Exception {
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));
		ChambreDao.addChambre(new Chambre(7, null, 300f));
		fenetre = OutilsIhm.ouvrir();

		Object chambres = onglet(fenetre, CHAMBRES);
		selectionner(chambres, 7);
		OutilsIhm.saisir(chambres, "champOccupant", "TEST1");
		cliquer(chambres, "attribuer");
		assertEquals("7", valeur(onglet(fenetre, ELEVES), "TEST1", 4));

		chambres = onglet(fenetre, CHAMBRES);
		selectionner(chambres, 7);
		cliquer(chambres, "liberer");
		assertEquals("—", valeur(onglet(fenetre, ELEVES), "TEST1", 4));
	}

	@Test
	@DisplayName("[OK] Un élève ajouté dans l'onglet Élèves peut aussitôt recevoir une chambre dans l'onglet Chambres")
	void ajoutEleve_utilisableDansChambres() throws Exception {
		ChambreDao.addChambre(new Chambre(1, null, 300f));
		fenetre = OutilsIhm.ouvrir();

		Object eleves = onglet(fenetre, ELEVES);
		OutilsIhm.saisir(eleves, "champNum", "TEST1", "champNom", "Alice Test", "champAge", "20", "champAdresse", "");
		cliquer(eleves, "ajouter");

		Object chambres = onglet(fenetre, CHAMBRES);
		selectionner(chambres, 1);
		OutilsIhm.saisir(chambres, "champOccupant", "TEST1");
		cliquer(chambres, "attribuer");
		verifierStatut(chambres, "attribuée à TEST1 (code 1)");
	}

}
