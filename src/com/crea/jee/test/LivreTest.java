package com.crea.jee.test;

import java.util.List;

import com.crea.jee.beans.Livre;
import com.crea.jee.dao.LivreDao;

/*
 * Test manuel de LivreDao : appelle chaque méthode et affiche le résultat dans la console
 * Prévu pour les données de démo de la base ecole (AGUE001 sert d'emprunteur) : le livre de test
 * ISBN99999 est rendu puis supprimé à la fin, la base revient à son état initial
 * Version automatisée avec vérifications : com.crea.jee.wrappers.LivreDaoTest
 */
public class LivreTest {
	public static void main(String[] args) {

		System.out.println("=== getLivreByCote ===");
		Livre l1 = LivreDao.getLivreByCote("ISBN10000");
		if (l1 != null) {
			l1.affiche();
		} else {
			System.out.println("Aucun livre ISBN10000");
		}

		System.out.println("=== addLivre ===");
		Livre l2 = new Livre("ISBN99999", null, "Les Misérables", null);
		int nb = LivreDao.addLivre(l2);
		if (nb == 1) {
			System.out.println("ISBN99999 a été bien ajouté");
		} else if (nb == -2) {
			System.out.println("ISBN99999 est déjà dans la base");
		} else {
			System.out.println("Echec de l'ajout, code retour = " + nb);
		}

		System.out.println("=== getLivreByCote (après ajout) ===");
		Livre l3 = LivreDao.getLivreByCote("ISBN99999");
		if (l3 != null) {
			l3.affiche();
		}

		System.out.println("=== updateTitreLivre ===");
		int majTitre = LivreDao.updateTitreLivre("ISBN99999", "Les Misérables - Tome 1");
		System.out.println("Lignes mises à jour : " + majTitre);
		Livre l4 = LivreDao.getLivreByCote("ISBN99999");
		if (l4 != null) {
			l4.affiche();
		}

		System.out.println("=== updateEmprunteurLivre (emprunt) ===");
		int majEmprunteur = LivreDao.updateEmprunteurLivre("ISBN99999", "AGUE001");
		System.out.println("Lignes mises à jour : " + majEmprunteur);
		Livre l5 = LivreDao.getLivreByCote("ISBN99999");
		if (l5 != null) {
			l5.affiche();
		}

		System.out.println("=== getLivresEmpruntesByEleve ===");
		List<Livre> empruntes = LivreDao.getLivresEmpruntesByEleve("AGUE001");
		empruntes.forEach(Livre::affiche);

		System.out.println("=== updateEmprunteurLivre (retour) ===");
		LivreDao.updateEmprunteurLivre("ISBN99999", null);
		Livre l6 = LivreDao.getLivreByCote("ISBN99999");
		if (l6 != null) {
			l6.affiche();
		}

		System.out.println("=== getLivresDisponibles ===");
		List<Livre> disponibles = LivreDao.getLivresDisponibles();
		disponibles.forEach(Livre::affiche);

		System.out.println("=== getAllLivres ===");
		List<Livre> tous = LivreDao.getAllLivres();
		tous.forEach(Livre::affiche);

		System.out.println("=== deleteLivreByCote ===");
		int suppr = LivreDao.deleteLivreByCote("ISBN99999");
		System.out.println("Lignes supprimées : " + suppr);
		Livre l7 = LivreDao.getLivreByCote("ISBN99999");
		if (l7 == null) {
			System.out.println("ISBN99999 a bien été supprimé");
		}
	}
}
