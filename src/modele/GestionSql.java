package modele;

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
    static String url = new String("jdbc:mysql://localhost/symfony6_formarmor?characterEncoding=UTF8");
    //Requete permettant de retourner l'ensemble des clients
    public static ObservableList<Client> getLesClients()
    {
        Connection conn;
        Statement stmt1;
        Client monClient;
        ObservableList<Client> lesClients = FXCollections.observableArrayList();
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url,"root","");
            stmt1 = conn.createStatement();
            
            // Liste des clients qui "ont un plan de formation"
            String req = "select distinct c.id, statut_id, username, password, adresse, cp, ville, email, nbhcompta, nbhbur from client c, plan_formation p "
            + "where c.id = p.client_id order by c.id";
            ResultSet rs = stmt1.executeQuery(req);
            while (rs.next())
            {
                monClient = new Client(rs.getInt("id"), rs.getInt("statut_id"), rs.getInt("nbhcompta"), rs.getInt("nbhbur"), rs.getString("username"), rs.getString("password"), rs.getString("adresse"), rs.getString("cp"), rs.getString("ville"), rs.getString("email"));
                lesClients.add(monClient);
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return lesClients;
    }
    
    //Requête permettant de  retourner les sessions autorisées pour le client sélectionné
    public static ObservableList<Session> getLesSessions(int client_id)
    {
        Connection conn;
        Statement stmt1;
        Session maSession;
        ObservableList<Session> lesSessions = FXCollections.observableArrayList();
        try
        {
            // On prévoit 2 connexions à la base

            Class.forName(pilote);
            conn = DriverManager.getConnection(url,"root","");
            stmt1 = conn.createStatement();
            
            // Sélection des sessions autorisées pour le client choisi
            String req = "select c.username, s.id, f.libelle, f.niveau, date_debut, duree, nb_places, nb_inscrits, coutrevient ";
            req += "from session_formation s, client c, plan_formation p, formation f ";
            req += "where c.id = '" + client_id + "' ";
            req += "and p.client_id = c.id and nb_places > nb_inscrits ";
            req += "and p.formation_id = f.id ";
            req += "and s.formation_id = f.id ";
            req += "and close = 0 and effectue = 0 and s.id Not In ";
            req += "(Select sessionformation_id From inscription Where client_id = '" + client_id + "')";
            ResultSet rs = stmt1.executeQuery(req);
            while (rs.next())
            {
                // A MODIFIER
                maSession = new Session(rs.getInt("id"), rs.getString("libelle"), rs.getDate("date_debut"), rs.getInt("nb_places"), rs.getInt("nb_inscrits"));
                lesSessions.add(maSession);
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesSessions : " + cnfe.getMessage());
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesSessions : " + se.getMessage());
        }
        return lesSessions;
    }
    
    //Requête permettant l'insertion de l'inscription dans la table inscription et
    //la mise à jour de la table session_formation (+1 inscrit) et
    //la mise à jour de la table plan_formation (effectue passe à 1)
    public static void insereInscription(int matricule, int session_formation_id)
    {
        Connection conn;
        Statement stmt2=null;
        
        GregorianCalendar dateJour = new GregorianCalendar();
        String ddate = dateJour.get(GregorianCalendar.YEAR) + "-" + (dateJour.get(GregorianCalendar.MONTH) + 1) + "-" + dateJour.get(GregorianCalendar.DATE);
        // Insertion dans la table inscription
        String req = "Insert into inscription(client_id, sessionformation_id, date_inscription) values (" + matricule;
        req += ", " + session_formation_id + ",'" + ddate + "')";
        // M.A.J de la table session_formation (un inscrit de plus)
        String req2 = "Update session_formation set nb_inscrits = nb_inscrits +1 Where id = " + session_formation_id;
        // Récupération du numéro de la session concernée
        String req3 = "Select formation_id from session_formation where id = " + session_formation_id;
        //stmt1 = GestionBdd.connexionBdd(GestionBdd.TYPE_MYSQL, "symfony6_formarmor", "localhost", "root", "");
        int numForm=0;
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url,"root","");
            stmt2 = conn.createStatement();
            System.out.println("EXECUTION REQ3 : ");
            ResultSet rs = stmt2.executeQuery(req3);
            rs.next();
            numForm = rs.getInt(1);
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver insereInscription : " + cnfe.getMessage());
        }
        catch(Exception e)
        {
            System.out.println("Erreur requete insereInscription " + e.getMessage());
        }
        // M.A.J de la table plan_formation (effectue passe à 1 pour le client et la session concernés)
        String req4 = "Update plan_formation set effectue = 1 Where client_id = " + matricule;
        req4 += " And formation_id = " + numForm;
        try
        {
            System.out.println("EXECUTION REQ : ");
            int nb1 = stmt2.executeUpdate(req);
            System.out.println("EXECUTION REQ2 : ");
            int nb2 = stmt2.executeUpdate(req2);
            System.out.println("EXECUTION REQ4 : ");
            int nb3 = stmt2.executeUpdate(req4);
        }
        catch(SQLException sqle)
        {
            System.out.println("Erreur SQL maj des inscriptions : " + sqle.getMessage());
        }
    }
    
    
    
      public static ObservableList<Session> getLesSessionsFinies()
    {
        Connection conn;
        Statement stmt1;
        Session maSession;
        ObservableList<Session> lesSessions = FXCollections.observableArrayList();
        try
        {
            // On prévoit 2 connexions à la base

            Class.forName(pilote);
            conn = DriverManager.getConnection(url,"root","");
            stmt1 = conn.createStatement();
            LocalDate dateDuJour = LocalDate.now();
            // Sélection des sessions autorisées pour le client choisi
            String req = "select s.id, f.libelle, s.date_debut, s.nb_places, s.nb_inscrits ";
            req += "from session_formation s, formation f ";
            req += "where s.formation_id = f.id ";
            req += "and s.date_debut <'"+ dateDuJour+"'";
            ResultSet rs = stmt1.executeQuery(req);
            while (rs.next())
            {
                // A MODIFIER
                maSession = new Session(rs.getInt("id"), rs.getString("libelle"), rs.getDate("date_debut"), rs.getInt("nb_places"), rs.getInt("nb_inscrits"));
                lesSessions.add(maSession);
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesSessions : " + cnfe.getMessage());
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesSessions : " + se.getMessage());
        }
        return lesSessions;
    }
      
     public static ObservableList<Client> getLesClientsInscrits(int id)
    {
        Connection conn;
        Statement stmt1;
        Client monClient;
        ObservableList<Client> lesClients = FXCollections.observableArrayList();
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url,"root","");
            stmt1 = conn.createStatement();
            
            // Liste des clients qui "ont un plan de formation"
            String req = "SELECT c.*, i.presence \n" +
            " FROM client c\n" +
            " JOIN inscription i ON c.id = i.client_id\n" +
            " JOIN session_formation sf ON i.sessionformation_id = sf.id\n" +
            " WHERE i.sessionformation_id = sf.id\n" +
            " AND sf.id = '" + id + "'";
            ResultSet rs = stmt1.executeQuery(req);
            while (rs.next())
            {
                monClient = new Client(rs.getInt("id"), rs.getInt("statut_id"), rs.getInt("nbhcompta"), rs.getInt("nbhbur"), rs.getString("username"), rs.getString("password"), rs.getString("adresse"), rs.getString("cp"), rs.getString("ville"), rs.getString("email"), rs.getString("presence"));
                lesClients.add(monClient);
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return lesClients;
    }
     
     
      public static void setUnClientAbsent(int id, int formation, String presence)
    {
        Connection conn;
        Statement stmt1;
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url,"root","");
            stmt1 = conn.createStatement();
            
       
            String req = "Update inscription" +
            " Set presence = '"+ presence +
            "' Where client_id = '"+ id +
            "' and sessionformation_id = '"+ formation +"';";
            stmt1.executeUpdate(req);
         
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        } 
    }
      
      
       public static float getMarge(int id)
    {
        Connection conn;
        Statement stmt1;
        float laMarge = 0;
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url,"root","");
            stmt1 = conn.createStatement();
            
            String req = "SELECT  DISTINCT(c.id), s.taux_horaire  \n" +
            " from statut s, client c, formation f  \n" +
            " where c.statut_id = s.id and c.id in  \n" +
                " (SELECT DISTINCT c.id  \n" +
                " FROM client c  \n" +
                " JOIN inscription i ON c.id = i.client_id \n" +
                " JOIN session_formation sf ON i.sessionformation_id = sf.id  \n" +
                " WHERE sf.id = '"+ id +"');";
            ResultSet rs = stmt1.executeQuery(req);
            while (rs.next())
            {
                laMarge += rs.getFloat("taux_horaire");
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return laMarge;
    }
       
       
        public static float getPrixSession(int id)
    {
        Connection conn;
        Statement stmt1;
       
        float prix = 0;
        try
        {
            Class.forName(pilote);
            conn = DriverManager.getConnection(url,"root","");
            stmt1 = conn.createStatement();
            
            // Liste des clients qui "ont un plan de formation"
            String req = "SELECT f.coutrevient\n" +
            " FROM formation f, session_formation sf \n" +
            " WHERE sf.id = '"+id+"'" +
            " and f.id = sf.formation_id;";
            ResultSet rs = stmt1.executeQuery(req);
            while (rs.next())
            {
                prix = rs.getFloat("coutrevient");
            }
        }
        catch (ClassNotFoundException cnfe)
        {
            System.out.println("Erreur chargement driver getLesClients : " + cnfe.getMessage());
        }
        catch (SQLException se)
        {
            System.out.println("Erreur SQL requete getLesClients : " + se.getMessage());
        }
        return prix;
    }
}
