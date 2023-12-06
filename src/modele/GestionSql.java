package modele;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import javafx.collections.FXCollections;
import java.time.LocalDate;
import javafx.collections.ObservableList;

public class GestionSql
{

    static String pilote = "com.mysql.cj.jdbc.Driver";
    static String url = "jdbc:mysql://localhost/symfony6_formarmor?characterEncoding=UTF8";

    //OK NIVEAU PARAM
    public static ObservableList<Client> getLesClients()
    {
        Connection conn;
        Statement stmt1;
        Client monClient;
        ObservableList<Client> lesClients = FXCollections.observableArrayList();
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url, "root", "");
            stmt1 = conn.createStatement();

            //REQ
            String req = "select distinct c.id, statut_id, username, password, adresse, cp, ville, email, nbhcompta, nbhbur from client c, plan_formation p "
                    + "where c.id = p.client_id order by c.id";

            ResultSet rs = stmt1.executeQuery(req);
            while (rs.next())
            {
                monClient = new Client(rs.getInt("id"), rs.getInt("statut_id"), rs.getInt("nbhcompta"), rs.getInt("nbhbur"), rs.getString("username"), rs.getString("password"), rs.getString("adresse"), rs.getString("cp"), rs.getString("ville"), rs.getString("email"));
                lesClients.add(monClient);
            }
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        } catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return lesClients;
    }

    //OK NIVEAU PARAM
    public static ObservableList<Session> getLesSessions(int client_id)
    {
        Connection conn;
        Session maSession;
        ObservableList<Session> lesSessions = FXCollections.observableArrayList();
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url, "root", "");

            //REQ
            PreparedStatement stmt1 = conn.prepareStatement("call sessions_autorisees(?)");
            //AJOUT DU PARAMETRE
            stmt1.setInt(1, client_id);

            ResultSet rs = stmt1.executeQuery();
            while (rs.next())
            {
                maSession = new Session(rs.getInt("id"), rs.getString("libelle"), rs.getDate("date_debut"), rs.getInt("nb_places"), rs.getInt("nb_inscrits"));
                lesSessions.add(maSession);
            }
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesSessions : " + cnfe.getMessage());
        } catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesSessions : " + se.getMessage());
        }
        return lesSessions;
    }

    //OK NIVEAU PARAM
    public static boolean insereInscription(int matricule, int session_formation_id)
    {
        int numForm = 0;
        String present = "Present";
        GregorianCalendar dateJour = new GregorianCalendar();
        String ddate = dateJour.get(GregorianCalendar.YEAR) + "-" + (dateJour.get(GregorianCalendar.MONTH) + 1) + "-" + dateJour.get(GregorianCalendar.DATE);
        try
        {
            Connection conn;
            conn = DriverManager.getConnection(url, "root", "");

            //REQ 3
            PreparedStatement stmt3 = conn.prepareStatement("Select formation_id from session_formation where id = ?;");
            //AJOUT DU PARAMETRE
            stmt3.setInt(1, session_formation_id);

            Class.forName(pilote);
            System.out.println("EXECUTION REQ3 : ");
            ResultSet rs = stmt3.executeQuery();
            rs.next();
            numForm = rs.getInt(1);
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver insereInscription : " + cnfe.getMessage());
            return false;
        } catch (SQLException e)
        {
            System.out.println("Erreur requete insereInscription " + e.getMessage());
            return false;
        }
        try
        {
            Connection conn;
            conn = DriverManager.getConnection(url, "root", "");

            //REQ 1
            PreparedStatement stmt1 = conn.prepareStatement("Insert into inscription(client_id, sessionformation_id, date_inscription, presence) values ( ?, ?, ?, ?);");
            //AJOUT DES PARAMETRES
            stmt1.setInt(1, matricule);
            stmt1.setInt(2, session_formation_id);
            stmt1.setString(3, ddate);
            stmt1.setString(4, present);

            //REQ 2
            PreparedStatement stmt2 = conn.prepareStatement("Update session_formation set nb_inscrits = nb_inscrits +1 Where id = ?;");
            //AJOUT DU PARAMETRE
            stmt2.setInt(1, session_formation_id);

            //REQ 4
            PreparedStatement stmt4 = conn.prepareStatement("Update plan_formation set effectue = 1 Where client_id = ? And formation_id = ?;");
            //AJOUT DU PARAMETRE
            stmt4.setInt(1, matricule);
            stmt4.setInt(2, numForm);

            System.out.println("EXECUTION REQ 1: ");
            stmt1.executeUpdate();
            System.out.println("EXECUTION REQ 2: ");
            stmt2.executeUpdate();
            System.out.println("EXECUTION REQ 4: ");
            stmt4.executeUpdate();
        } catch (SQLException sqle)
        {
            System.out.println("Erreur SQL maj des inscriptions : " + sqle.getMessage());
            return false;
        }
        return true;
    }

    //OK NIVEAU PARAM
    public static ObservableList<Session> getLesSessionsFinies()
    {
        Connection conn;
        Session maSession;
        ObservableList<Session> lesSessions = FXCollections.observableArrayList();
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url, "root", "");
            Date dateDuJour = Date.valueOf(LocalDate.now());

            //REQ
            PreparedStatement stmt1 = conn.prepareStatement("select s.id, f.libelle, s.date_debut, s.nb_places, s.nb_inscrits "
                    + "from session_formation s, formation f "
                    + "where s.formation_id = f.id "
                    + "and s.date_debut < ?");
            //AJOUT DU PARAMETRE
            stmt1.setDate(1, dateDuJour);

            ResultSet rs = stmt1.executeQuery();
            while (rs.next())
            {
                maSession = new Session(rs.getInt("id"), rs.getString("libelle"), rs.getDate("date_debut"), rs.getInt("nb_places"), rs.getInt("nb_inscrits"));
                lesSessions.add(maSession);
            }
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesSessions : " + cnfe.getMessage());
        } catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesSessions : " + se.getMessage());
        }
        return lesSessions;
    }

    //OK NIVEAU PARAM
    public static ObservableList<Client> getLesClientsInscrits(int id)
    {
        Connection conn;
        Client monClient;
        ObservableList<Client> lesClients = FXCollections.observableArrayList();
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url, "root", "");

            //REQ
            PreparedStatement stmt1 = conn.prepareStatement("SELECT c.*, i.presence \n"
                    + " FROM client c\n"
                    + " JOIN inscription i ON c.id = i.client_id\n"
                    + " JOIN session_formation sf ON i.sessionformation_id = sf.id\n"
                    + " WHERE i.sessionformation_id = sf.id\n"
                    + " AND sf.id = ?");
            //AJOUT DU PARAMETRE
            stmt1.setInt(1, id);

            ResultSet rs = stmt1.executeQuery();
            while (rs.next())
            {
                monClient = new Client(rs.getInt("id"), rs.getInt("statut_id"), rs.getInt("nbhcompta"), rs.getInt("nbhbur"), rs.getString("username"), rs.getString("password"), rs.getString("adresse"), rs.getString("cp"), rs.getString("ville"), rs.getString("email"), rs.getString("presence"));
                lesClients.add(monClient);
            }
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        } catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return lesClients;
    }

    //OK NIVEAU PARAM
    public static void setUnClientAbsent(int id, int formation, String presence)
    {
        Connection conn;
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url, "root", "");

            //REQ
            PreparedStatement stmt1 = conn.prepareStatement("Update inscription"
                    + " Set presence = ?"
                    + " Where client_id = ?"
                    + " and sessionformation_id = ?;");
            //AJOUT DES PARAMETREs
            stmt1.setString(1, presence);
            stmt1.setInt(2, id);
            stmt1.setInt(3, formation);

            stmt1.executeUpdate();
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        } catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
    }

    //OK NIVEAU PARAM
    public static float getMarge(int id)
    {
        Connection conn;
        float laMarge = 0;
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url, "root", "");

            //REQ
            PreparedStatement stmt1 = conn.prepareStatement("SELECT  DISTINCT(c.id), s.taux_horaire  \n"
                    + " from statut s, client c, formation f  \n"
                    + " where c.statut_id = s.id and c.id in  \n"
                    + " (SELECT DISTINCT c.id  \n"
                    + " FROM client c  \n"
                    + " JOIN inscription i ON c.id = i.client_id \n"
                    + " JOIN session_formation sf ON i.sessionformation_id = sf.id  \n"
                    + " WHERE sf.id = ?);");
            //AJOUT DU PARAMETRE
            stmt1.setInt(1, id);

            ResultSet rs = stmt1.executeQuery();
            while (rs.next())
            {
                laMarge += rs.getFloat("taux_horaire");
            }
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        } catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return laMarge;
    }

    //OK NIVEAU PARAM 
    public static float getPrixSession(int id)
    {
        Connection conn;
        float prix = 0;
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url, "root", "");

            //REQ
            PreparedStatement stmt1 = conn.prepareStatement("SELECT f.coutrevient\n"
                    + " FROM formation f, session_formation sf \n"
                    + " WHERE sf.id = ?"
                    + " and f.id = sf.formation_id;");
            //AJOUT DU PARAMETRE
            stmt1.setInt(1, id);

            ResultSet rs = stmt1.executeQuery();
            while (rs.next())
            {
                prix = rs.getFloat("coutrevient");
            }
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        } catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return prix;
    }

    public static ObservableList<Session> getLesSessionsFutures()
    {
        Connection conn;
        Session maSession;
        ObservableList<Session> lesSessions = FXCollections.observableArrayList();

        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url, "root", "");

            //REQ
            PreparedStatement stmt1 = conn.prepareStatement("SELECT session_formation.id, formation.libelle as libFormation, date_debut, nb_places, nb_inscrits "
                    + "FROM session_formation, formation "
                    + "WHERE date_debut > CURRENT_DATE() "
                    + "AND formation.id = session_formation.id "
                    + "ORDER BY date_debut");

            ResultSet rs = stmt1.executeQuery();
            while (rs.next())
            {
                maSession = new Session(rs.getInt("id"), rs.getString("libFormation"), rs.getDate("date_debut"), rs.getInt("nb_places"), rs.getInt("nb_inscrits"));
                lesSessions.add(maSession);
            }
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesSessionsFutures : " + cnfe.getMessage());
        } catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesSessionsFutures : " + se.getMessage());
        }
        return lesSessions;
    }
}
