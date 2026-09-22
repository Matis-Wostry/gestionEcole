package com.crea.jee.test;

import java.util.List;

import com.crea.jee.beans.Eleve;
import com.crea.jee.dao.EleveDao;

public class EleveTest {
	public static void main(String[] args) {

		System.out.println("=== getEleveByNum ===");
		Eleve e1 = EleveDao.getEleveByNum("AGUE001");
		if (e1 != null) {
			e1.affiche();
		} else {
			System.out.println("L'élève AGUE001 n'est pas dans la liste");
		}

		Eleve e2 = EleveDao.getEleveByNum("JULIA002");
		if (e2 != null) {
			e2.affiche();
		} else {
			System.out.println("L'élève JULIA002 n'est pas dans la liste");
		}

		System.out.println("=== addEleve ===");
		Eleve e3 = new Eleve("MOMO88", 0, "Mohamed", 22, "Rue de la belle étoile");
		int nb = EleveDao.addEleve(e3);
		if (nb == 1) {
			System.out.println("MOMO88 a été bien ajouté");
		} else if (nb == -2) {
			System.out.println("MOMO88 est déjà dans la base");
		} else {
			System.out.println("Echec de l'ajout, code retour = " + nb);
		}

		System.out.println("=== getEleveByNum (après ajout) ===");
		Eleve e4 = EleveDao.getEleveByNum("MOMO88");
		if (e4 != null) {
			e4.affiche();
		} else {
			System.out.println("L'élève MOMO88 n'est pas dans la liste");
		}

		System.out.println("=== getElevesByNom ===");
		List<Eleve> parNom = EleveDao.getElevesByNom("AGUE MAX");
		parNom.forEach(Eleve::affiche);

		System.out.println("=== updateAdresseEleve ===");
		int majAdresse = EleveDao.updateAdresseEleve("MOMO88", "10 Rue Nouvelle");
		System.out.println("Lignes mises à jour : " + majAdresse);
		Eleve e5 = EleveDao.getEleveByNum("MOMO88");
		if (e5 != null) {
			e5.affiche();
		}

		System.out.println("=== updateNumEleve ===");
		int majNum = EleveDao.updateNumEleve("MOMO88", "MOMO89");
		System.out.println("Lignes mises à jour : " + majNum);
		Eleve e6 = EleveDao.getEleveByNum("MOMO89");
		if (e6 != null) {
			e6.affiche();
		}

		System.out.println("=== getEleveByNo ===");
		Eleve occupant = EleveDao.getEleveByNo(1);
		if (occupant != null) {
			occupant.affiche();
		} else {
			System.out.println("Aucun élève dans la chambre no=1");
		}

		System.out.println("=== getElevesByAge ===");
		List<Eleve> memeAge = EleveDao.getElevesByAge(30);
		memeAge.forEach(Eleve::affiche);

		System.out.println("=== getAllEleves ===");
		List<Eleve> tous = EleveDao.getAllEleves();
		tous.forEach(Eleve::affiche);

		System.out.println("=== deleteEleveByNum ===");
		int suppr = EleveDao.deleteEleveByNum("MOMO89");
		System.out.println("Lignes supprimées : " + suppr);
		Eleve e7 = EleveDao.getEleveByNum("MOMO89");
		if (e7 == null) {
			System.out.println("MOMO89 a bien été supprimé");
		}
	}
}
