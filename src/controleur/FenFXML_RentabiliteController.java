package controleur;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import modele.Session;
import modele.Client;

// Imports relatifs à SQL
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import modele.GestionSql;

public class FenFXML_RentabiliteController implements Initializable
{
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
    
    //Le tableau et les trois colonnes relatives aux sessions finies.
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
    private TableColumn<Client, String> colonnePresent;
    
    // Le bouton pour fermer.
    @FXML
    Button buttonClose;
    
    // La connection.
    Connection conn;
    
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {   
        ObservableList<Session> lesSessionsFinies = GestionSql.getLesSessionsFinies();
        colonneId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colonneFormationId.setCellValueFactory(new PropertyValueFactory<>("libFormation"));
        colonneDateSession.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        
        tableSessionFinies.getItems().addAll(lesSessionsFinies);
        
        
      tableSessionFinies.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Session>()
        {
            @Override
            public void changed(ObservableValue<? extends Session> observable, Session oldValue, Session newValue)
            {
                if (newValue != null)
                {
                    //obtenir :
                    // - Le libelle :
                    lblLibelle.setText(newValue.getLibFormation()); 
                    // - Le label de date :
                    lblDateSession.setText(newValue.getDate_debut().toString());
                    // - Le label du nombre d'inscrits :
                    lblNbInscrits.setText(String.valueOf(newValue.getNb_inscrits()));
                    // - Le label du nombre d'absents : 
                    // plus tard
                    //lblTauxRemplissage
                    int taux = newValue.getNb_inscrits() * 100 / newValue.getNb_places();
                    lblTauxRemplissage.setText(String.valueOf(taux)+"%");
                    //
                }
            }
        });
    }    
    
    @FXML
    public void handleClosingButton()
    {
        Stage stage = (Stage) lblMarge.getScene().getWindow();
        stage.close();
    }
    
    
    
}
