package com.crea.jee.beans;

/*
* Cette classe représente une unité de valeur (UV) ou d'enseignement
* Elle correspond à la table uv de la base de données
*/
public class Uv {
	/*
	 * Attributs
	 * code = Id code de l'uv
	 * nbh = Nombre d'heure de cours
	 * coord = Coordinateur de l'uv
	 */
	private String code;
	private int nbh;
	private String coord;

	/*
	 * Constructeur
	 */
	// construit une uv avec toutes ses informations
	public Uv(String code, int nbh, String coord) {
		super();
		this.code = code;
		this.nbh = nbh;
		this.coord = coord;
	}

	// construit une uv vide, avec des valeurs par défaut
	public Uv() {
		this("", 0, "");
	}

	/*
	 * Getteurs et Setteurs
	 */
	// retourne le code de l'uv
	public String getCode() {
		return code;
	}

	// modifie le code de l'uv
	public void setCode(String code) {
		this.code = code;
	}

	// retourne le nombre d'heure de cours de l'uv
	public int getNbh() {
		return nbh;
	}

	// modifie le nombre d'heure de cours de l'uv
	public void setNbh(int nbh) {
		this.nbh = nbh;
	}

	// retourne le coordinateur de l'uv
	public String getCoord() {
		return coord;
	}

	// modifie le coordinateur de l'uv
	public void setCoord(String coord) {
		this.coord = coord;
	}

	/*
	 * Methode
	 */
	// construit la représentation texte de l'uv
	@Override
	public String toString() {
		return "Uv [code=" + code + ", nbh=" + nbh + ", coord=" + coord + "]";
	}

	// affiche l'uv dans la console
	public void affiche() {
		System.out.println(this.toString());
	}

}
