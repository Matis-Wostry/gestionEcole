package com.crea.jee.test;

import java.util.List;

import com.crea.jee.beans.Chambre;
import com.crea.jee.dao.ChambreDao;

/*
 * Test manuel de ChambreDao : appelle chaque méthode et affiche le résultat dans la console
 * Prévu pour les données de démo de la base ecole (AGUE001 sert d'occupant) : la chambre de test 100
 * est supprimée à la fin, la base revient à son état initial
 * Version automatisée avec vérifications : com.crea.jee.wrappers.ChambreDaoTest
 */
public class ChambreTest {
	public static void main(String[] args) {

		System.out.println("=== getChambreByNo ===");
		Chambre c1 = ChambreDao.getChambreByNo(1);
		if (c1 != null) {
			c1.affiche();
		} else {
			System.out.println("Aucune chambre no=1");
		}

		System.out.println("=== addChambre ===");
		Chambre c2 = new Chambre(100, null, 275.50f);
		int nb = ChambreDao.addChambre(c2);
		if (nb == 1) {
			System.out.println("Chambre 100 a été bien ajoutée");
		} else if (nb == -2) {
			System.out.println("Chambre 100 est déjà dans la base");
		} else {
			System.out.println("Echec de l'ajout, code retour = " + nb);
		}

		System.out.println("=== getChambreByNo (après ajout) ===");
		Chambre c3 = ChambreDao.getChambreByNo(100);
		if (c3 != null) {
			c3.affiche();
		}

		System.out.println("=== updatePrixChambre ===");
		int majPrix = ChambreDao.updatePrixChambre(100, 300.0f);
		System.out.println("Lignes mises à jour : " + majPrix);
		Chambre c4 = ChambreDao.getChambreByNo(100);
		if (c4 != null) {
			c4.affiche();
		}

		System.out.println("=== updateOccupantChambre ===");
		int majOccupant = ChambreDao.updateOccupantChambre(100, "AGUE001");
		System.out.println("Lignes mises à jour : " + majOccupant);
		Chambre c5 = ChambreDao.getChambreByNo(100);
		if (c5 != null) {
			c5.affiche();
		}

		System.out.println("=== getChambreByOccupant ===");
		Chambre c6 = ChambreDao.getChambreByOccupant("AGUE001");
		if (c6 != null) {
			c6.affiche();
		} else {
			System.out.println("AGUE001 n'occupe aucune chambre");
		}

		System.out.println("=== getChambresPrixSuperieur ===");
		List<Chambre> cheres = ChambreDao.getChambresPrixSuperieur(300.0f);
		cheres.forEach(Chambre::affiche);

		System.out.println("=== getChambresNonOccupees ===");
		List<Chambre> libres = ChambreDao.getChambresNonOccupees();
		libres.forEach(Chambre::affiche);

		System.out.println("=== getAllChambres ===");
		List<Chambre> toutes = ChambreDao.getAllChambres();
		toutes.forEach(Chambre::affiche);

		System.out.println("=== deleteChambreByNo ===");
		int suppr = ChambreDao.deleteChambreByNo(100);
		System.out.println("Lignes supprimées : " + suppr);
		Chambre c7 = ChambreDao.getChambreByNo(100);
		if (c7 == null) {
			System.out.println("Chambre 100 a bien été supprimée");
		}
	}
}
