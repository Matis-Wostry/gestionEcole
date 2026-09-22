package com.crea.jee.beans;

/*
* Cette classe représente l'inscription d'un élève à une uv, avec sa note
* Elle correspond à la table inscrit de la base de données (clé composée code+num)
*/
public class Inscrit {
	/*
	 * Attributs
	 * code = Id code de l'uv
	 * num = Numero identifiant l'élève
	 * note = Note accordée à l'élève sur cette uv
	 */
	private String code;
	private String num;
	private float note;

	/*
	 * Constructeur
	 */
	// construit une inscription avec toutes ses informations
	public Inscrit(String code, String num, float note) {
		super();
		this.code = code;
		this.num = num;
		this.note = note;
	}

	// construit une inscription vide, avec des valeurs par défaut
	public Inscrit() {
		this("", "", 0f);
	}

	/*
	 * Getteurs et Setteurs
	 */
	// retourne le code de l'uv concernée
	public String getCode() {
		return code;
	}

	// modifie le code de l'uv concernée
	public void setCode(String code) {
		this.code = code;
	}

	// retourne le numéro de l'élève inscrit
	public String getNum() {
		return num;
	}

	// modifie le numéro de l'élève inscrit
	public void setNum(String num) {
		this.num = num;
	}

	// retourne la note de l'élève sur cette uv
	public float getNote() {
		return note;
	}

	// modifie la note de l'élève sur cette uv
	public void setNote(float note) {
		this.note = note;
	}

	/*
	 * Methode
	 */
	// construit la représentation texte de l'inscription
	@Override
	public String toString() {
		return "Inscrit [code=" + code + ", num=" + num + ", note=" + note + "]";
	}

	// affiche l'inscription dans la console
	public void affiche() {
		System.out.println(this.toString());
	}

}
