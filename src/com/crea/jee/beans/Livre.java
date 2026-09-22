package com.crea.jee.beans;

import java.sql.Timestamp;

/*
* Cette classe représente un livre éventuellement emprunté par un élève
* Elle correspond à la table livre de la base de données
*/
public class Livre {
	/*
	 * Attributs
	 * cote = Numero identifiant du livre
	 * num = Numero de l'élève qui a emprunté le livre (null si disponible)
	 * titre = Titre du livre
	 * datepret = Date et heure du pret du livre par l'élève
	 */
	private String cote;
	private String num;
	private String titre;
	private Timestamp datepret;

	/*
	 * Constructeur
	 */

	// construit un livre avec toutes ses informations
	public Livre(String cote, String num, String titre, Timestamp datepret) {
		super();
		this.cote = cote;
		this.num = num;
		this.titre = titre;
		this.datepret = datepret;
	}

	// construit un livre vide, avec des valeurs par défaut
	public Livre() {
		this("", null, "", null);
	}

	/*
	 * Getteurs et Setteurs
	 */

	// retourne la cote du livre
	public String getCote() {
		return cote;
	}

	// modifie la cote du livre
	public void setCote(String cote) {
		this.cote = cote;
	}

	// retourne le numéro de l'élève qui a emprunté le livre
	public String getNum() {
		return num;
	}

	// modifie le numéro de l'élève qui a emprunté le livre
	public void setNum(String num) {
		this.num = num;
	}

	// retourne le titre du livre
	public String getTitre() {
		return titre;
	}

	// modifie le titre du livre
	public void setTitre(String titre) {
		this.titre = titre;
	}

	// retourne la date de prêt du livre
	public Timestamp getDatepret() {
		return datepret;
	}

	// modifie la date de prêt du livre
	public void setDatepret(Timestamp datepret) {
		this.datepret = datepret;
	}

	/*
	 * Methode
	 */

	// construit la représentation texte du livre
	@Override
	public String toString() {
		return "Livre [cote=" + cote + ", num=" + num + ", titre=" + titre + ", datepret=" + datepret + "]";
	}

	// affiche le livre dans la console
	public void affiche() {
		System.out.println(this.toString());
	}

}
