package com.crea.jee.ihm;

import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.crea.jee.beans.Eleve;
import com.crea.jee.dao.EleveDao;
import com.crea.jee.wrappers.EleveWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Onglet des élèves : recherche par numéro, liste, ajout et suppression
 */
class OngletEleves extends Onglet {

	private final JTextField champRecherche = new JTextField(12);
	private final JTextField champNum = new JTextField(8);
	private final JTextField champNom = new JTextField(14);
	private final JTextField champAge = new JTextField(4);
	private final JTextField champAdresse = new JTextField(22);

	OngletEleves() {
		super("Numéro", "Nom", "Âge", "Adresse", "Chambre");

		JButton boutonRechercher = new JButton("Rechercher");
		JButton boutonActualiser = new JButton("Actualiser");
		JButton boutonSupprimer = new JButton("Supprimer la sélection");
		JButton boutonAjouter = new JButton("Ajouter");
		boutonRechercher.addActionListener(e -> rechercher());
		champRecherche.addActionListener(e -> rechercher());
		boutonActualiser.addActionListener(e -> actualiser());
		boutonSupprimer.addActionListener(e -> supprimerSelection());
		boutonAjouter.addActionListener(e -> ajouter());

		JPanel haut = barre();
		haut.add(new JLabel("Numéro :"));
		haut.add(champRecherche);
		haut.add(boutonRechercher);
		haut.add(Box.createHorizontalStrut(24));
		haut.add(boutonActualiser);
		haut.add(boutonSupprimer);

		JPanel ajout = formulaire("Ajouter un élève (numéro, nom et âge obligatoires)");
		ajout.add(new JLabel("Numéro"));
		ajout.add(champNum);
		ajout.add(new JLabel("Nom"));
		ajout.add(champNom);
		ajout.add(new JLabel("Âge"));
		ajout.add(champAge);
		ajout.add(new JLabel("Adresse"));
		ajout.add(champAdresse);
		ajout.add(boutonAjouter);

		assembler(haut, ajout);
	}

	@Override
	void actualiser() {
		List<Eleve> eleves = EleveDao.getAllEleves();
		modeleTable.setRowCount(0);
		for (Eleve eleve : eleves) {
			modeleTable.addRow(new Object[] { eleve.getNum(), eleve.getNom(), eleve.getAge(), eleve.getAdresse(),
					eleve.getNo() == 0 ? "—" : eleve.getNo() });
		}
		afficherBilan(eleves.size(), "Aucun élève à afficher (base vide ou injoignable).", "élève(s)");
	}

	// cherche l'élève saisi et réagit selon le code réponse du wrapper
	private void rechercher() {
		String num = texte(champRecherche);
		EleveWrapper resultat = EleveDao.getEleveByNum(num);
		int code = resultat.getCodeResponse();
		switch (code) {
		case Wrapper.TROUVE:
			Eleve eleve = resultat.getEleve();
			selectionnerLigne(eleve.getNum());
			afficherMessage("Élève " + eleve.getNum() + " trouvé (code 1).", VERT);
			afficherFiche("Élève " + eleve.getNum(), "Numéro : " + eleve.getNum()
					+ "\nNom : " + eleve.getNom()
					+ "\nÂge : " + eleve.getAge()
					+ "\nAdresse : " + eleve.getAdresse()
					+ "\nChambre : " + (eleve.getNo() == 0 ? "aucune" : eleve.getNo()));
			break;
		case Wrapper.NON_TROUVE:
			afficherMessage("Aucun élève avec le numéro " + num + " (code 0).", ROUGE);
			break;
		case Wrapper.DONNEES_INVALIDES:
			afficherMessage("Saisissez un numéro à rechercher (code -3).", ROUGE);
			break;
		default:
			afficherErreurBase(code);
		}
	}

	// ajoute l'élève saisi dans le formulaire et réagit selon le code retour du DAO
	private void ajouter() {
		String num = texte(champNum);
		Eleve eleve = new Eleve(num, 0, texte(champNom), entier(champAge), texte(champAdresse));

		int code = EleveDao.addEleve(eleve);
		switch (code) {
		case 1:
			actualiser();
			selectionnerLigne(num);
			champNum.setText("");
			champNom.setText("");
			champAge.setText("");
			champAdresse.setText("");
			afficherMessage("Élève " + num + " ajouté (code 1).", VERT);
			break;
		case -2:
			afficherMessage("Le numéro " + num + " est déjà pris (code -2).", ROUGE);
			break;
		case -3:
			afficherMessage("Données invalides : numéro et nom obligatoires, âge entre 1 et 127 (code -3).", ROUGE);
			break;
		default:
			afficherErreurBase(code);
		}
	}

	// supprime l'élève sélectionné dans le tableau, après confirmation
	private void supprimerSelection() {
		Object num = valeurSelectionnee(0);
		if (num == null || !confirmer("Supprimer l'élève " + num
				+ " ?\nSa chambre et ses livres seront libérés, ses inscriptions supprimées.")) {
			return;
		}

		int code = EleveDao.deleteEleveByNum((String) num);
		if (code > 0) {
			actualiser();
			afficherMessage("Élève " + num + " supprimé (code 1).", VERT);
		} else if (code == 0) {
			actualiser();
			afficherMessage("L'élève " + num + " n'existe plus (code 0).", ROUGE);
		} else {
			afficherErreurBase(code);
		}
	}

}
