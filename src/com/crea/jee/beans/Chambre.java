package com.crea.jee.beans;

/*
 * Cette classe représente une chambre éventuellement occupée par un élève
 * Elle correspond à la table chambre de la base de données
 */
public class Chambre {
	/*
	 * Attributs
	 * no = ID de la chambre
	 * num = Numero de l'élève qui occupe la chambre (null si inoccupée)
	 * prix = Prix de la chambre
	 */
	private int no;
	private String num;
	private float prix;

	/*
	 * Constructeur
	 */

	// construit une chambre avec toutes ses informations
	public Chambre(int no, String num, float prix) {
		super();
		this.no = no;
		this.num = num;
		this.prix = prix;
	}

	// construit une chambre vide, avec des valeurs par défaut
	public Chambre() {
		this(0, null, 0f);
	}

	/*
	 * Getteurs et Setteurs
	 */

	// retourne le numéro de la chambre
	public int getNo() {
		return no;
	}

	// modifie le numéro de la chambre
	public void setNo(int no) {
		this.no = no;
	}

	// retourne le numéro de l'élève qui occupe la chambre
	public String getNum() {
		return num;
	}

	// modifie le numéro de l'élève qui occupe la chambre
	public void setNum(String num) {
		this.num = num;
	}

	// retourne le prix de la chambre
	public float getPrix() {
		return prix;
	}

	// modifie le prix de la chambre
	public void setPrix(float prix) {
		this.prix = prix;
	}

	/*
	 * Methode
	 */

	// construit la représentation texte de la chambre
	@Override
	public String toString() {
		return "Chambre [no=" + no + ", num=" + num + ", prix=" + prix + "]";
	}

	// affiche la chambre dans la console
	public void affiche() {
		System.out.println(this.toString());
	}

}
