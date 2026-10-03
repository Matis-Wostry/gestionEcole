package com.crea.jee.beans;

/*
 * Cette classe représente un élève de l'école
 * Elle correspond à la table eleve de la base de données
 */
public class Eleve {
	/*
	 * Attributs
	 * num = Numéro identifiant l'élève
	 * no = Numéro de la chambre de l'élève (0 si aucune chambre)
	 * nom = Nom de l'élève
	 * age = Age de l'élève
	 * adresse = Adresse de l'élève
	 */
	private String num;
	private int no;
	private String nom;
	private int age;
	private String adresse;

	/*
	 * Constructeurs
	 */

	// construit un élève avec toutes ses informations
	public Eleve(String num, int no, String nom, int age, String adresse) {
		this.num = num;
		this.no = no;
		this.nom = nom;
		this.age = age;
		this.adresse = adresse;
	}

	// construit un élève vide, avec des valeurs par défaut
	public Eleve() {
		this("", 0, "", 0, "");
	}

	/*
	 * Getters et setters
	 */

	// retourne le numéro de l'élève
	public String getNum() {
		return num;
	}

	// modifie le numéro de l'élève
	public void setNum(String num) {
		this.num = num;
	}

	// retourne le numéro de la chambre de l'élève
	public int getNo() {
		return no;
	}

	// modifie le numéro de la chambre de l'élève
	public void setNo(int no) {
		this.no = no;
	}

	// retourne le nom de l'élève
	public String getNom() {
		return nom;
	}

	// modifie le nom de l'élève
	public void setNom(String nom) {
		this.nom = nom;
	}

	// retourne l'âge de l'élève
	public int getAge() {
		return age;
	}

	// modifie l'âge de l'élève
	public void setAge(int age) {
		this.age = age;
	}

	// retourne l'adresse de l'élève
	public String getAdresse() {
		return adresse;
	}

	// modifie l'adresse de l'élève
	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	/*
	 * Méthodes
	 */

	// construit la représentation texte de l'élève
	@Override
	public String toString() {
		return "Eleve [num=" + num + ", no=" + no + ", nom=" + nom + ", age=" + age + ", adresse=" + adresse + "]";
	}

	// affiche l'élève dans la console
	public void affiche() {
		System.out.println(this.toString());
	}

}
