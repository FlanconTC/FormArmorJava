package controleur;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import modele.Client;
import modele.GestionSql;
import modele.Session;

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

    // Le tableau et les trois colonnes relatives aux sessions finies.
    @FXML
    private TableView<Session> tableSessionFinies;
    @FXML
    private TableColumn<Session, String> colonneId;
    @FXML
    private TableColumn<Session, String> colonneFormationId;
    @FXML
    private TableColumn<Session, String> colonneDateSession;

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
    
    
    int nbInscrits;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ObservableList<Session> lesSessionsFinies = GestionSql.getLesSessionsFinies();
        colonneId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colonneFormationId.setCellValueFactory(new PropertyValueFactory<>("libFormation"));
        colonneDateSession.setCellValueFactory(new PropertyValueFactory<>("date_debut"));

        tableSessionFinies.getItems().addAll(lesSessionsFinies);

        tableSessionFinies.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Session>() {
            @Override
            public void changed(ObservableValue<? extends Session> observable, Session oldValue, Session newValue) {
                if (newValue != null) {
                    tableClientsInscrits.getItems().clear();
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
    });
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
    public void changePresence()
    {
        Client client = tableClientsInscrits.getSelectionModel().getSelectedItem();
          if (client != null) {
        if (client.getPresent().equals("Présent")) {
            client.setPresent("Absent");
        } else {
            client.setPresent("Présent");
        }
        tableClientsInscrits.getItems().remove(client);
        colonnePresent.setCellValueFactory(new PropertyValueFactory<>("present"));
        tableClientsInscrits.getItems().add(client);
        recalculerStat();
        }
    }
    @FXML
    public void handleClosingButton() {
        Stage stage = (Stage) lblMarge.getScene().getWindow();
        stage.close();
    }
}
