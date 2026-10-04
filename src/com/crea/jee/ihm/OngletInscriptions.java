package com.crea.jee.ihm;

import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.crea.jee.beans.Inscrit;
import com.crea.jee.dao.InscritDao;

/*
 * Onglet des inscriptions d'élèves aux UV : liste, inscription, modification de la note et suppression
 * Une inscription est identifiée par le couple (code de l'uv, numéro de l'élève)
 */
class OngletInscriptions extends Onglet {

	private final JTextField champCode = new JTextField(12);
	private final JTextField champNum = new JTextField(10);
	private final JTextField champNote = new JTextField(5);
	private final JTextField champNouvelleNote = new JTextField(5);

	OngletInscriptions() {
		super("UV", "Élève", "Note");

		JButton boutonActualiser = new JButton("Actualiser");
		JButton boutonSupprimer = new JButton("Supprimer la sélection");
		JButton boutonInscrire = new JButton("Inscrire");
		JButton boutonNote = new JButton("Modifier la note");
		boutonActualiser.addActionListener(e -> actualiser());
		boutonSupprimer.addActionListener(e -> supprimerSelection());
		boutonInscrire.addActionListener(e -> inscrire());
		boutonNote.addActionListener(e -> modifierNote());

		JPanel haut = barre();
		haut.add(boutonActualiser);
		haut.add(boutonSupprimer);

		JPanel ajout = formulaire("Inscrire un élève à une UV (l'UV et l'élève doivent exister)");
		ajout.add(new JLabel("Code de l'UV"));
		ajout.add(champCode);
		ajout.add(new JLabel("N° d'élève"));
		ajout.add(champNum);
		ajout.add(new JLabel("Note"));
		ajout.add(champNote);
		ajout.add(boutonInscrire);

		JPanel modification = formulaire("Inscription sélectionnée dans le tableau");
		modification.add(new JLabel("Nouvelle note"));
		modification.add(champNouvelleNote);
		modification.add(boutonNote);

		assembler(haut, ajout, modification);
	}

	@Override
	void actualiser() {
		List<Inscrit> inscriptions = InscritDao.getAllInscriptions();
		modeleTable.setRowCount(0);
		for (Inscrit inscrit : inscriptions) {
			modeleTable.addRow(new Object[] { inscrit.getCode(), inscrit.getNum(), inscrit.getNote() });
		}
		afficherBilan(inscriptions.size(), "Aucune inscription à afficher.", "inscription(s)");
	}

	private void inscrire() {
		String code = texte(champCode);
		String num = texte(champNum);
		int codeRetour = InscritDao.addInscription(new Inscrit(code, num, decimal(champNote)));
		switch (codeRetour) {
		case 1:
			actualiser();
			selectionnerInscription(code, num);
			champCode.setText("");
			champNum.setText("");
			champNote.setText("");
			afficherMessage("Élève " + num + " inscrit à l'UV " + code + " (code 1).", VERT);
			break;
		case -2:
			afficherMessage("L'élève " + num + " est déjà inscrit à l'UV " + code + " (code -2).", ROUGE);
			break;
		case -3:
			afficherMessage("Données invalides : code d'UV, n° d'élève et note obligatoires (code -3).", ROUGE);
			break;
		default:
			afficherMessage("Impossible : l'UV " + code + " ou l'élève " + num
					+ " n'existe pas, ou la base ne répond pas (code " + codeRetour + ").", ROUGE);
		}
	}

	private void modifierNote() {
		Object code = valeurSelectionnee(0);
		if (code == null) {
			return;
		}
		Object num = valeurSelectionnee(1);
		float note = decimal(champNouvelleNote);
		if (Float.isNaN(note)) {
			afficherMessage("Saisissez une note (nombre, virgule ou point acceptés).", ROUGE);
			return;
		}
		int codeRetour = InscritDao.updateNoteInscrit((String) code, (String) num, note);
		if (codeRetour > 0) {
			actualiser();
			selectionnerInscription((String) code, (String) num);
			champNouvelleNote.setText("");
			afficherMessage("Note de " + num + " en " + code + " modifiée (code 1).", VERT);
		} else if (codeRetour == 0) {
			actualiser();
			afficherMessage("Cette inscription n'existe plus (code 0).", ROUGE);
		} else {
			afficherErreurBase(codeRetour);
		}
	}

	private void supprimerSelection() {
		Object code = valeurSelectionnee(0);
		if (code == null) {
			return;
		}
		Object num = valeurSelectionnee(1);
		if (!confirmer("Supprimer l'inscription de l'élève " + num + " à l'UV " + code + " ?")) {
			return;
		}
		int codeRetour = InscritDao.deleteInscription((String) code, (String) num);
		if (codeRetour > 0) {
			actualiser();
			afficherMessage("Inscription de " + num + " à l'UV " + code + " supprimée (code 1).", VERT);
		} else if (codeRetour == 0) {
			actualiser();
			afficherMessage("Cette inscription n'existe plus (code 0).", ROUGE);
		} else {
			afficherErreurBase(codeRetour);
		}
	}

	// sélectionne la ligne de l'inscription (code, num), la clé occupant deux colonnes
	private void selectionnerInscription(String code, String num) {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (code.equals(table.getValueAt(i, 0)) && num.equals(table.getValueAt(i, 1))) {
				table.setRowSelectionInterval(i, i);
				table.scrollRectToVisible(table.getCellRect(i, 0, true));
				return;
			}
		}
	}

}
