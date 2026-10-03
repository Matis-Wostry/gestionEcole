package com.crea.jee.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.crea.jee.beans.Chambre;
import com.crea.jee.utils.DBAction;
import com.crea.jee.utils.Validation;
import com.crea.jee.wrappers.ChambreWrapper;
import com.crea.jee.wrappers.Wrapper;

/*
 * Cette classe regroupe les accès en base de données liés à la table chambre
 * Les méthodes d'écriture retournent le nombre de lignes modifiées, ou un code d'erreur :
 * -1 = erreur SQL, -2 = numéro déjà existant, -3 = données invalides (refusées avant tout accès à la base)
 * Les lectures d'une seule chambre retournent un ChambreWrapper (la chambre + un code réponse, voir Wrapper)
 * Les lectures de listes retournent une liste vide si rien n'est trouvé ou en cas d'erreur
 */
public class ChambreDao {

	// construit un objet Chambre à partir d'une ligne du ResultSet
	private static Chambre mapResultSet(ResultSet rs) throws SQLException {
		return new Chambre(rs.getInt("no"), rs.getString("num"), rs.getFloat("prix"));
	}

	// récupère une chambre à partir de son numéro, avec le code réponse de la recherche
	public static ChambreWrapper getChambreByNo(int no) {
		if (!Validation.estPositif(no)) {
			return new ChambreWrapper(null, Wrapper.DONNEES_INVALIDES);
		}
		if (DBAction.DBConnexion() != null) {
			return new ChambreWrapper(null, Wrapper.ERREUR_BASE);
		}
		String request = "SELECT * FROM chambre WHERE no = ?";
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setInt(1, no);
			try (ResultSet response = ps.executeQuery()) {
				if (response.next()) {
					return new ChambreWrapper(mapResultSet(response), Wrapper.TROUVE);
				}
				return new ChambreWrapper(null, Wrapper.NON_TROUVE);
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
			return new ChambreWrapper(null, Wrapper.ERREUR_BASE);
		} finally {
			DBAction.DBClose();
		}
	}

	// récupère la chambre occupée par un élève donné, avec le code réponse de la recherche
	public static ChambreWrapper getChambreByOccupant(String num) {
		if (!Validation.estValide(num, 100)) {
			return new ChambreWrapper(null, Wrapper.DONNEES_INVALIDES);
		}
		if (DBAction.DBConnexion() != null) {
			return new ChambreWrapper(null, Wrapper.ERREUR_BASE);
		}
		String request = "SELECT * FROM chambre WHERE num = ?";
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, num);
			try (ResultSet response = ps.executeQuery()) {
				if (response.next()) {
					return new ChambreWrapper(mapResultSet(response), Wrapper.TROUVE);
				}
				return new ChambreWrapper(null, Wrapper.NON_TROUVE);
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
			return new ChambreWrapper(null, Wrapper.ERREUR_BASE);
		} finally {
			DBAction.DBClose();
		}
	}

	/*
	 * supprime une chambre à partir de son numéro, retourne le nombre de lignes supprimées
	 * la contrainte Eleve_fk1 (eleve.no -> chambre.no) interdit de supprimer une chambre
	 * tant qu'un élève y est rattaché : on libère donc d'abord les élèves concernés
	 * (eleve.no = NULL) avant de supprimer la chambre, les deux requêtes formant une
	 * seule transaction pour ne jamais laisser la BD dans un état intermédiaire
	 */
	public static int deleteChambreByNo(int no) {
		int result = -1;
		DBAction.DBConnexion();
		try {
			DBAction.getCon().setAutoCommit(false);
			try (PreparedStatement libere = DBAction.getCon()
					.prepareStatement("UPDATE eleve SET no = NULL WHERE no = ?");
					PreparedStatement supprime = DBAction.getCon()
							.prepareStatement("DELETE FROM chambre WHERE no = ?")) {
				libere.setInt(1, no);
				libere.executeUpdate();

				supprime.setInt(1, no);
				result = supprime.executeUpdate();

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

	// modifie l'occupant d'une chambre identifiée par son numéro (num = null pour la libérer)
	public static int updateOccupantChambre(int no, String num) {
		int result = -1;
		String request = "UPDATE chambre SET num = ? WHERE no = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, num);
			ps.setInt(2, no);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// met à jour le prix d'une chambre identifiée par son numéro
	public static int updatePrixChambre(int no, float prix) {
		if (!Validation.estPositif(prix)) {
			return -3;
		}
		int result = -1;
		String request = "UPDATE chambre SET prix = ? WHERE no = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setFloat(1, prix);
			ps.setInt(2, no);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// ajoute une nouvelle chambre, sans occupant
	public static int addChambre(Chambre nouvelleChambre) {
		if (!Validation.estPositif(nouvelleChambre.getNo()) || !Validation.estPositif(nouvelleChambre.getPrix())) {
			return -3;
		}
		int result = -1;
		String req = "INSERT INTO chambre (no, num, prix) VALUES (?, NULL, ?)";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(req)) {
			ps.setInt(1, nouvelleChambre.getNo());
			ps.setFloat(2, nouvelleChambre.getPrix());
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

	// récupère la liste des chambres dont le prix dépasse une valeur donnée
	public static List<Chambre> getChambresPrixSuperieur(float prix) {
		List<Chambre> liste = new ArrayList<>();
		String request = "SELECT * FROM chambre WHERE prix > ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setFloat(1, prix);
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

	// récupère la liste de toutes les chambres
	public static List<Chambre> getAllChambres() {
		List<Chambre> liste = new ArrayList<>();
		String request = "SELECT * FROM chambre";
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

	// récupère la liste des chambres non occupées
	public static List<Chambre> getChambresNonOccupees() {
		List<Chambre> liste = new ArrayList<>();
		String request = "SELECT * FROM chambre WHERE num IS NULL";
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
