package com.crea.jee.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.crea.jee.beans.Livre;
import com.crea.jee.utils.DBAction;

/*
 * Cette classe regroupe les accès en base de données liés à la table livre
 */
public class LivreDao {

	// construit un objet Livre à partir d'une ligne du ResultSet
	private static Livre mapResultSet(ResultSet rs) throws SQLException {
		return new Livre(rs.getString("cote"), rs.getString("num"), rs.getString("titre"),
				rs.getTimestamp("datepret"));
	}

	// récupère un livre à partir de sa cote
	public static Livre getLivreByCote(String cote) {
		Livre l = null;
		String request = "SELECT * FROM livre WHERE cote = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, cote);
			try (ResultSet response = ps.executeQuery()) {
				if (response.next()) {
					l = mapResultSet(response);
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			DBAction.DBClose();
		}
		return l;
	}

	// récupère la liste des livres empruntés par un élève donné
	public static List<Livre> getLivresEmpruntesByEleve(String num) {
		List<Livre> liste = new ArrayList<>();
		String request = "SELECT * FROM livre WHERE num = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, num);
			try (ResultSet response = ps.executeQuery()) {
				while (response.next()) {
					liste.add(mapResultSet(response));
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

	// supprime un livre à partir de sa cote, retourne le nombre de lignes supprimées
	public static int deleteLivreByCote(String cote) {
		int result = -1;
		String request = "DELETE FROM livre WHERE cote = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, cote);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	/*
	 * modifie l'emprunteur d'un livre identifié par sa cote (num = null pour le rendre disponible)
	 * la date de prêt est posée à l'instant présent lors d'un emprunt, remise à null lors d'un retour
	 */
	public static int updateEmprunteurLivre(String cote, String num) {
		int result = -1;
		String request = "UPDATE livre SET num = ?, datepret = ? WHERE cote = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, num);
			ps.setTimestamp(2, num != null ? new Timestamp(System.currentTimeMillis()) : null);
			ps.setString(3, cote);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// met à jour le titre d'un livre identifié par sa cote
	public static int updateTitreLivre(String cote, String titre) {
		int result = -1;
		String request = "UPDATE livre SET titre = ? WHERE cote = ?";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, titre);
			ps.setString(2, cote);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// ajoute un nouveau livre, disponible (sans emprunteur)
	public static int addLivre(Livre new_livre) {
		int result = -1;
		String req = "INSERT INTO livre (cote, num, titre, datepret) VALUES (?, NULL, ?, NULL)";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(req)) {
			ps.setString(1, new_livre.getCote());
			ps.setString(2, new_livre.getTitre());
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

	// récupère la liste des livres disponibles (non empruntés)
	public static List<Livre> getLivresDisponibles() {
		List<Livre> liste = new ArrayList<>();
		String request = "SELECT * FROM livre WHERE num IS NULL";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			try (ResultSet response = ps.executeQuery()) {
				while (response.next()) {
					liste.add(mapResultSet(response));
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

	// récupère la liste de tous les livres
	public static List<Livre> getAllLivres() {
		List<Livre> liste = new ArrayList<>();
		String request = "SELECT * FROM livre";
		DBAction.DBConnexion();
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			try (ResultSet response = ps.executeQuery()) {
				while (response.next()) {
					liste.add(mapResultSet(response));
				}
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			DBAction.DBClose();
		}
		return liste;
	}

}
