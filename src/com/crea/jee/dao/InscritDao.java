package com.crea.jee.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.crea.jee.beans.Inscrit;
import com.crea.jee.utils.DBAction;
import com.crea.jee.utils.Validation;

/*
 * Cette classe regroupe les accès en base de données liés à la table inscrit
 * Les méthodes d'écriture retournent le nombre de lignes modifiées, ou un code d'erreur :
 * -1 = erreur côté base (connexion impossible, erreur SQL, uv ou élève inexistant), -2 = élève déjà inscrit à cette uv,
 * -3 = données invalides (refusées avant tout accès à la base)
 * Les méthodes de lecture retournent une liste vide si rien n'est trouvé ou en cas d'erreur
 */
public class InscritDao {

	// construit un objet Inscrit à partir d'une ligne du ResultSet
	private static Inscrit mapResultSet(ResultSet rs) throws SQLException {
		return new Inscrit(rs.getString("code"), rs.getString("num"), rs.getFloat("note"));
	}

	// supprime une inscription d'un élève à une uv particulière, retourne le nombre de lignes supprimées
	public static int deleteInscription(String code, String num) {
		int result = -1;
		String request = "DELETE FROM inscrit WHERE code = ? AND num = ?";
		if (DBAction.DBConnexion() != null) {
			return -1;
		}
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setString(1, code);
			ps.setString(2, num);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	/*
	 * inscrit un élève à une uv avec sa note (méthode en plus de la spécification fonctionnelle)
	 * l'uv et l'élève doivent exister : sinon la clé étrangère fait échouer l'insertion (code -1)
	 */
	public static int addInscription(Inscrit nouvelleInscription) {
		if (!Validation.estValide(nouvelleInscription.getCode(), 100)
				|| !Validation.estValide(nouvelleInscription.getNum(), 100) || Float.isNaN(nouvelleInscription.getNote())) {
			return -3;
		}
		int result = -1;
		String req = "INSERT INTO inscrit (code, num, note) VALUES (?, ?, ?)";
		if (DBAction.DBConnexion() != null) {
			return -1;
		}
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(req)) {
			ps.setString(1, nouvelleInscription.getCode());
			ps.setString(2, nouvelleInscription.getNum());
			ps.setFloat(3, nouvelleInscription.getNote());
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			if (ex.getErrorCode() == 1062) {// l'élève est déjà inscrit à cette uv
				result = -2;
			}
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

	// récupère la liste de toutes les inscriptions à des uv
	public static List<Inscrit> getAllInscriptions() {
		List<Inscrit> liste = new ArrayList<>();
		String request = "SELECT * FROM inscrit";
		if (DBAction.DBConnexion() != null) {
			return liste;
		}
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

	// met à jour la note d'un élève inscrit à une uv particulière
	public static int updateNoteInscrit(String code, String num, float note) {
		int result = -1;
		String request = "UPDATE inscrit SET note = ? WHERE code = ? AND num = ?";
		if (DBAction.DBConnexion() != null) {
			return -1;
		}
		try (PreparedStatement ps = DBAction.getCon().prepareStatement(request)) {
			ps.setFloat(1, note);
			ps.setString(2, code);
			ps.setString(3, num);
			result = ps.executeUpdate();
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		} finally {
			DBAction.DBClose();
		}
		return result;
	}

}
