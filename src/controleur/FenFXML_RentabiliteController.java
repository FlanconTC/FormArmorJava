package controleur;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import modele.Session;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import modele.Client;
import modele.GestionSql;


public class FenFXML_RentabiliteController implements Initializable {
    // Les différents labels pour glisser les résultats.
    @FXML
    Label lblLibelle;
    @FXML
    Label lblDateSession;
    @FXML
    Label lblNbInscrits;
    @FXML
    Label lblTauxRemplissage;
    @FXML
    Label lblNbAbsents;
    @FXML
    Label lblMarge;

    @FXML
    Button btnAbsent;
    // Le tableau et les trois colonnes relatives aux sessions finies.
    @FXML
    private TableView<modele.Session> tableSessionFinies;
    @FXML
    private TableColumn<modele.Session, String> colonneId;
    @FXML
    private TableColumn<modele.Session, String> colonneFormationId;
    @FXML
    private TableColumn<modele.Session, String> colonneDateSession;

    // Le tableau des inscrits à la session.
    @FXML
    private TableView<Client> tableClientsInscrits;
    @FXML
    private TableColumn<Client, String> colonneNomClient;
    @FXML
    private TableColumn<Client, String> colonneTauxHoraire;
    @FXML
    private TableColumn<Client, Boolean> colonnePresent;

    @FXML
    Button buttonClose;
    
    private int idFormClick;
    int nbInscrits;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<modele.Session> lesSessionsFinies = GestionSql.getLesSessionsFinies();
        colonneId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colonneFormationId.setCellValueFactory(new PropertyValueFactory<>("libFormation"));
        colonneDateSession.setCellValueFactory(new PropertyValueFactory<>("date_debut"));

        tableSessionFinies.getItems().addAll(lesSessionsFinies);

  
          
              
            
        
   
}
    public void recalculerStat()
    {
        int compte = 0;
        int nbAbsent = 0;
         List<Client> clients = tableClientsInscrits.getItems();
         for(Client unClient : clients)
         {
             compte ++;
             if(unClient.getPresent()=="Absent")
             {
                 compte --;
                 nbAbsent ++;
             }
         }
         
         lblNbAbsents.setText( String.valueOf(nbAbsent));

    }
    
    @FXML
    public void onSessionChosen()
    {

            tableClientsInscrits.getItems().clear();
              
            modele.Session newValue = tableSessionFinies.getSelectionModel().getSelectedItem();
            if(newValue != null)
            {
                 idFormClick = newValue.getId();
                lblLibelle.setText(newValue.getLibFormation());
                
                lblDateSession.setText(newValue.getDate_debut().toString());
                lblNbInscrits.setText(String.valueOf(newValue.getNb_inscrits()));
                nbInscrits = newValue.getNb_inscrits();
                int taux = newValue.getNb_inscrits() * 100 / newValue.getNb_places();
                lblTauxRemplissage.setText(String.valueOf(taux) + "%");
               
                ObservableList<Client> lesClients = GestionSql.getLesClientsInscrits(newValue.getId());
                colonneNomClient.setCellValueFactory(new PropertyValueFactory<>("Nom"));
                colonneTauxHoraire.setCellValueFactory(new PropertyValueFactory<>("nbhbur"));
                colonnePresent.setCellValueFactory(new PropertyValueFactory<>("present"));
                tableClientsInscrits.getItems().addAll(lesClients);
            }

    }
                
    
   @FXML
    public void changePresence() 
    {
        TableView.TableViewSelectionModel<Client> selectionModel = tableClientsInscrits.getSelectionModel();
        Client client = selectionModel.getSelectedItem();

        if (client != null) 
        {
            ObservableList<Client> items = tableClientsInscrits.getItems(); 

            int selectedIndex = items.indexOf(client);

            if (client.getPresent().equals("Présent")) 
            {
                client.setPresent("Absent");
           
                    
            } 
            else 
            {
                

                client.setPresent("Présent");   
            }
            GestionSql.setUnClientAbsent(client.getId(), idFormClick, client.getPresent());

            items.remove(client);
         
            items.add(selectedIndex, client);

            tableClientsInscrits.getSelectionModel().clearSelection();
            tableClientsInscrits.refresh();

            recalculerStat();
        }
    }

    @FXML
    public void handleClosingButton() {
        Stage stage = (Stage) lblMarge.getScene().getWindow();
        stage.close();
    }
    
   @FXML
    public void sendAMail() {
        String username = "philippe.logiou@orange.fr";
        String password = "Dbrcecpldb2024!";

        Properties props = new Properties();
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.trust", "smtp.orange.fr");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.orange.fr");
        props.put("mail.smtp.port", "587");
        props.setProperty("mail.smtp.starttls.enable", "true");
        // Création de la session
        javax.mail.Session session = javax.mail.Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
 
        try {
            // Récupérer la liste des clients absents
            ObservableList<Client> lesClientsAbsents = tableClientsInscrits.getItems();

            // Parcourir la liste
            for (Client unClient : lesClientsAbsents) {
           
                if (unClient.getPresent().equals("Absent")) {
                    // Créer le message
                    Message message = new MimeMessage(session);
           
                    message.setFrom(new InternetAddress("philippe.logiou@orange.fr"));
     
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(unClient.getEmail()));

                    message.setSubject("Sujet de l'e-mail pour " + unClient.getNom());
                    message.setText("Contenu de l'e-mail pour " + unClient.getNom());

                    
                  //  Transport.send(message);
                    
                    System.out.println("E-mail envoyé à " + unClient.getNom());  

                    
                }
            }
        } catch (MessagingException e) {
            
            throw new RuntimeException(e);
        }
    }
}
