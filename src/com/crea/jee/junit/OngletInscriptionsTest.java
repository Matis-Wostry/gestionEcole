package com.crea.jee.junit;

import static com.crea.jee.junit.OutilsIhm.cliquer;
import static com.crea.jee.junit.OutilsIhm.deselectionner;
import static com.crea.jee.junit.OutilsIhm.lignes;
import static com.crea.jee.junit.OutilsIhm.saisir;
import static com.crea.jee.junit.OutilsIhm.selectionner;
import static com.crea.jee.junit.OutilsIhm.verifierStatut;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.util.List;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.crea.jee.beans.Eleve;
import com.crea.jee.beans.Inscrit;
import com.crea.jee.beans.Uv;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.dao.InscritDao;
import com.crea.jee.dao.UvDao;
import com.crea.jee.ihm.FenetreEcole;

/*
 * Tests de l'onglet Inscriptions de l'interface, contre la base ecole_test (-Ddb.name=ecole_test)
 * Chaque test dispose d'un élève TEST1 et de deux UV (UV1, UV2) créés avant l'ouverture de la fenêtre
 * [OK] = cas valide, l'action doit réussir ; [ERREUR] = cas invalide, l'interface doit l'afficher clairement
 */
@DisplayName("Interface : onglet Inscriptions")
class OngletInscriptionsTest {

	private FenetreEcole fenetre;
	private Object onglet;

	@BeforeEach
	void ouvrir() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless(), "Tests d'interface ignorés : aucun écran disponible");
		BaseDeTest.viderLesTables();
		EleveDao.addEleve(new Eleve("TEST1", 0, "Alice Test", 20, "Adresse"));
		UvDao.addUv(new Uv("UV1", 30, "Mr Test"));
		UvDao.addUv(new Uv("UV2", 12, "Mme Test"));
		fenetre = OutilsIhm.ouvrir();
		onglet = OutilsIhm.onglet(fenetre, 4);
	}

	@AfterEach
	void fermer() throws Exception {
		OutilsIhm.fermer(fenetre);
		BaseDeTest.viderLesTables();
	}

	private void creerInscription(String code, float note) throws Exception {
		InscritDao.addInscription(new Inscrit(code, "TEST1", note));
		cliquer(onglet, "actualiser");
	}

	// note enregistrée en base pour l'inscription (code, TEST1)
	private float note(String code) {
		List<Inscrit> inscriptions = InscritDao.getAllInscriptions();
		return inscriptions.stream().filter(i -> code.equals(i.getCode())).findFirst().orElseThrow().getNote();
	}

	@Test
	@DisplayName("[OK] Inscrire un élève à une UV, note saisie à la française (virgule) (code 1)")
	void inscrire_valide() throws Exception {
		saisir(onglet, "champCode", "UV1", "champNum", "TEST1", "champNote", "12,5");
		cliquer(onglet, "inscrire");

		verifierStatut(onglet, "Élève TEST1 inscrit à l'UV UV1 (code 1)");
		assertEquals(1, lignes(onglet));
		assertEquals(12.5f, note("UV1"), 0.001f);
	}

	@Test
	@DisplayName("[ERREUR] Inscrire deux fois le même élève à la même UV : refusé avec le code -2")
	void inscrire_dejaInscrit() throws Exception {
		creerInscription("UV1", 12f);

		saisir(onglet, "champCode", "UV1", "champNum", "TEST1", "champNote", "10");
		cliquer(onglet, "inscrire");

		verifierStatut(onglet, "déjà inscrit à l'UV UV1 (code -2)");
		assertEquals(1, lignes(onglet));
	}

	@Test
	@DisplayName("[ERREUR] Inscrire un élève ou à une UV qui n'existent pas : refusé avec le code -1")
	void inscrire_referenceInexistante() throws Exception {
		saisir(onglet, "champCode", "UV1", "champNum", "INCONNU", "champNote", "10");
		cliquer(onglet, "inscrire");
		verifierStatut(onglet, "n'existe pas");

		saisir(onglet, "champCode", "UV_INCONNUE", "champNum", "TEST1", "champNote", "10");
		cliquer(onglet, "inscrire");
		verifierStatut(onglet, "n'existe pas");

		assertEquals(0, lignes(onglet));
	}

	@Test
	@DisplayName("[ERREUR] Inscrire avec une note vide ou un code d'UV vide : refusé avec le code -3")
	void inscrire_donneesInvalides() throws Exception {
		saisir(onglet, "champCode", "UV1", "champNum", "TEST1", "champNote", "");
		cliquer(onglet, "inscrire");
		verifierStatut(onglet, "(code -3)");

		saisir(onglet, "champCode", "", "champNum", "TEST1", "champNote", "10");
		cliquer(onglet, "inscrire");
		verifierStatut(onglet, "(code -3)");

		assertEquals(0, lignes(onglet));
	}

	@Test
	@DisplayName("[OK] Modifier la note de l'inscription sélectionnée (code 1)")
	void modifierNote_valide() throws Exception {
		creerInscription("UV1", 12f);
		selectionner(onglet, "UV1", "TEST1");

		saisir(onglet, "champNouvelleNote", "15,5");
		cliquer(onglet, "modifierNote");

		verifierStatut(onglet, "modifiée (code 1)");
		assertEquals(15.5f, note("UV1"), 0.001f);
	}

	@Test
	@DisplayName("[ERREUR] Modifier la note sans sélection, ou avec une note non numérique : la note est conservée")
	void modifierNote_casInvalides() throws Exception {
		creerInscription("UV1", 12f);

		deselectionner(onglet);
		saisir(onglet, "champNouvelleNote", "15");
		cliquer(onglet, "modifierNote");
		verifierStatut(onglet, "Sélectionnez d'abord");

		selectionner(onglet, "UV1", "TEST1");
		saisir(onglet, "champNouvelleNote", "abc");
		cliquer(onglet, "modifierNote");
		verifierStatut(onglet, "Saisissez une note");

		assertEquals(12f, note("UV1"), 0.001f);
	}

	@Test
	@DisplayName("[OK] Supprimer une inscription : Non annule, Oui supprime uniquement celle-ci (code 1)")
	void supprimer() throws Exception {
		creerInscription("UV1", 12f);
		creerInscription("UV2", 14f);
		selectionner(onglet, "UV2", "TEST1");

		OutilsIhm.reponse = JOptionPane.NO_OPTION;
		cliquer(onglet, "supprimerSelection");
		assertEquals(2, lignes(onglet));

		OutilsIhm.reponse = JOptionPane.YES_OPTION;
		cliquer(onglet, "supprimerSelection");
		verifierStatut(onglet, "supprimée (code 1)");
		assertEquals(1, lignes(onglet));
		assertEquals(12f, note("UV1"), 0.001f);
	}

}
