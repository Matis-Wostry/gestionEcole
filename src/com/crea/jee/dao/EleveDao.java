package com.crea.jee.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.crea.jee.beans.Eleve;
import com.crea.jee.utils.DBAction;
import com.crea.jee.utils.Validation;

/*
 * Cette classe regroupe les accès en base de données liés à la table eleve
 * Les méthodes d'écriture retournent le nombre de lignes modifiées, ou un code d'erreur :
 * -1 = erreur SQL, -2 = numéro déjà existant, -3 = données invalides (refusées avant tout accès à la base)
 * Les méthodes de lecture retournent null (ou une liste vide) si rien n'est trouvé ou en cas d'erreur
 */
public class EleveDao {

	// construit un objet Eleve à partir d'une ligne du ResultSet
	private static Eleve mapResultSet(ResultSet rs) throws SQLException {
		return new Eleve(rs.getString("num"), rs.getInt("no"), rs.getString("nom"), rs.getInt("age"),
				rs.getString("adresse"));
	}

	// récupère un élève à partir de son numéro
	public static Eleve getEleveByNum(String num) {
		Eleve e = null;
		String request = "SELECT * FROM eleve WHERE num = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, num);
			try (ResultSet response = ps.executeQuery()) {
				if (response.next()) {
					e = mapResultSet(response);
				}
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return e;
	}

	// récupère la liste des élèves portant un nom donné
	public static List<Eleve> getElevesByNom(String nom) {
		List<Eleve> liste = new ArrayList<>();
		String request = "SELECT * FROM eleve WHERE nom = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, nom);
			try (ResultSet response = ps.executeQuery()) {
				while (response.next()) {
					liste.add(mapResultSet(response));
				}
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

	// récupère l'élève qui occupe une chambre donnée
	public static Eleve getEleveByNo(int no) {
		Eleve e = null;
		String request = "SELECT * FROM eleve WHERE no = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setInt(1, no);
			try (ResultSet response = ps.executeQuery()) {
				if (response.next()) {
					e = mapResultSet(response);
				}
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return e;
	}

	/*
	 * supprime un élève à partir de son numéro, retourne le nombre de lignes supprimées
	 * un élève peut être référencé par chambre.num, livre.num et inscrit.num (aucune de ces
	 * contraintes n'a de ON DELETE) : on libère donc chambre/livre (num = NULL) et on supprime
	 * ses inscriptions (impossible de mettre num à NULL dans inscrit, num fait partie de la clé
	 * primaire composée) avant de supprimer l'élève, le tout en une seule transaction
	 */
	public static int deleteEleveByNum(String num) {
		int result = -1;
		DBAction.DBConnexion();
		try {
			DBAction.getCon().setAutoCommit(false);
			try (PreparedStatement libereChambre = DBAction.getCon()
					.prepareStatement("UPDATE chambre SET num = NULL WHERE num = ?");
					PreparedStatement libereLivres = DBAction.getCon()
							.prepareStatement("UPDATE livre SET num = NULL, datepret = NULL WHERE num = ?");
					PreparedStatement supprimeInscriptions = DBAction.getCon()
							.prepareStatement("DELETE FROM inscrit WHERE num = ?");
					PreparedStatement supprimeEleve = DBAction.getCon()
							.prepareStatement("DELETE FROM eleve WHERE num = ?")) {
				libereChambre.setString(1, num);
				libereChambre.executeUpdate();

				libereLivres.setString(1, num);
				libereLivres.executeUpdate();

				supprimeInscriptions.setString(1, num);
				supprimeInscriptions.executeUpdate();

				supprimeEleve.setString(1, num);
				result = supprimeEleve.executeUpdate();

				DBAction.getCon().commit();
			} catch (SQLException ex) {
				DBAction.getCon().rollback();
				System.out.println(ex.getMessage());
			} finally {
				DBAction.getCon().setAutoCommit(true);
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// met à jour l'adresse d'un élève identifié par son numéro
	public static int updateAdresseEleve(String num, String adresse) {
		if (!Validation.estValide(adresse, 200)) {
			return -3;
		}
		int result = -1;
		String request = "UPDATE eleve SET adresse = ? WHERE num = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, adresse);
			ps.setString(2, num);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// met à jour le numéro d'un élève (répercuté automatiquement sur chambre/livre/inscrit via ON UPDATE CASCADE)
	public static int updateNumEleve(String ancienNum, String nouveauNum) {
		if (!Validation.estValide(nouveauNum, 100)) {
			return -3;
		}
		int result = -1;
		String request = "UPDATE eleve SET num = ? WHERE num = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, nouveauNum);
			ps.setString(2, ancienNum);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			if (ex.getErrorCode() == 1062) {// la clé existe déjà
				result = -2;
			}
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// ajoute un nouvel élève, sans chambre attribuée
	public static int addEleve(Eleve nouvelEleve) {
		if (!Validation.estValide(nouvelEleve.getNum(), 100) || !Validation.estValide(nouvelEleve.getNom(), 50)
				|| !Validation.longueurValide(nouvelEleve.getAdresse(), 200) || !Validation.estPositif(nouvelEleve.getAge())
				|| !Validation.estDansPlageTinyint(nouvelEleve.getAge())) {
			return -3;
		}
		int result = -1;
		String req = "INSERT INTO eleve (num, no, nom, age, adresse) VALUES (?, NULL, ?, ?, ?)";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(req)) {
			ps.setString(1, nouvelEleve.getNum());
			ps.setString(2, nouvelEleve.getNom());
			ps.setInt(3, nouvelEleve.getAge());
			ps.setString(4, nouvelEleve.getAdresse());
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			if (ex.getErrorCode() == 1062) {// la clé existe déjà
				result = -2;
			}
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// récupère la liste des élèves ayant un âge donné
	public static List<Eleve> getElevesByAge(int age) {
		List<Eleve> liste = new ArrayList<>();
		String request = "SELECT * FROM eleve WHERE age = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setInt(1, age);
			try (ResultSet response = ps.executeQuery()) {
				while (response.next()) {
					liste.add(mapResultSet(response));
				}
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

	// récupère la liste de tous les élèves
	public static List<Eleve> getAllEleves() {
		List<Eleve> liste = new ArrayList<>();
		String request = "SELECT * FROM eleve";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			try (ResultSet response = ps.executeQuery()) {
				while (response.next()) {
					liste.add(mapResultSet(response));
				}
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

}
