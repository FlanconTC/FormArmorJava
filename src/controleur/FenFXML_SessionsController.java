package controleur;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import modele.GestionSql;
import modele.Session;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.List;
import javafx.event.ActionEvent;
import modele.Client;

public class FenFXML_SessionsController implements Initializable
{

    @FXML
    private TableView<Session> tableSessions;
    @FXML
    private TableColumn<Session, String> colonneId;
    @FXML
    private TableColumn<Session, String> colonneLibFormation;
    @FXML
    private TableColumn<Session, String> colonneDateDebut;
    @FXML
    private TableColumn<Session, String> colonneNbPlaces;
    @FXML
    private TableColumn<Session, String> colonneNbInscrits;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        ObservableList<Session> lesSessionsFutures = GestionSql.getLesSessionsFutures();
        colonneId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colonneLibFormation.setCellValueFactory(new PropertyValueFactory<>("libFormation"));
        colonneDateDebut.setCellValueFactory(new PropertyValueFactory<>("date_debut"));
        colonneNbPlaces.setCellValueFactory(new PropertyValueFactory<>("nb_places"));
        colonneNbInscrits.setCellValueFactory(new PropertyValueFactory<>("nb_inscrits"));

        tableSessions.getItems().addAll(lesSessionsFutures);
    }

    @FXML
    private void handleGeneratePDF(ActionEvent event)
    {
        genererListeEmargementPDF();
    }

    @FXML
    public void genererListeEmargementPDF()
    {
        Session selectedSession = tableSessions.getSelectionModel().getSelectedItem();

        if (selectedSession != null)
        {
            int idSession = selectedSession.getId();

            try
            {
                PDDocument document = new PDDocument();
                PDPage page = new PDPage();
                document.addPage(page);

                try (PDPageContentStream contentStream = new PDPageContentStream(document, page))
                {

                    float yStart = 725;
                    float yPosition = yStart;
                    
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.TIMES_ROMAN, 16);
                    contentStream.setLeading(14.5f);
                    contentStream.newLineAtOffset(25, yPosition);
                    
                    contentStream.showText("Liste d'émargement pour la session " + selectedSession.getLibFormation() + " du " + selectedSession.getDate_debut());
                    yPosition -= 15;
                    contentStream.newLine();

                    List<Client> registeredClients = getLesClientsInscrits(idSession);
                    for (Client client : registeredClients)
                    {
                        contentStream.showText("Client ID: " + client.getId()
                                + ", Nom: " + client.getNom()
                                + ", Email: " + client.getEmail()
                                + ", Presence: " + client.getPresent()
                        );
                        yPosition -= 15; // Adjust the value based on your requirements
                        contentStream.newLine();
                    }

                    contentStream.endText();
                }

                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName("ListeEmargement_Session_" + idSession + ".pdf");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

                Stage stage = (Stage) tableSessions.getScene().getWindow();
                File file = fileChooser.showSaveDialog(stage);

                if (file != null)
                {
                    document.save(file);
                    document.close();

                    Desktop.getDesktop().open(file);
                }

            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private List<Client> getLesClientsInscrits(int sessionId)
    {
        return GestionSql.getLesClientsInscrits(sessionId);
    }

    @FXML
    public void handleClosingButton()
    {
        Stage stage = (Stage) tableSessions.getScene().getWindow();
        stage.close();
    }
}
