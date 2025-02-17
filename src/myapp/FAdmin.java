package myapp;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.Pesanan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class FAdmin extends Application {

    @FXML
    private Button btMenu;

    @FXML
    private Button btKeluar;

    @FXML
    private Button btAbout;

    @FXML
    private TableView<Pesanan> tblPesanan;
    @FXML
    private TableColumn<Pesanan, String> tblId, tblNama, tblNoMeja, tblTanggalPesanan, tblMenuMakanan, tblMenuMinuman,
            tblTotalHarga;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("FAdmin.fxml"));
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Admin");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void initialize() {
        loadDataPesanan(); 
        initTable(); 
    }

    @SuppressWarnings("unused")
    private void initTable() {
      
        tblId.setCellValueFactory(new PropertyValueFactory<>("id"));
        tblNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        tblNoMeja.setCellValueFactory(new PropertyValueFactory<>("noMeja"));
        tblTanggalPesanan.setCellValueFactory(new PropertyValueFactory<>("tanggalPesanan"));
        tblMenuMakanan.setCellValueFactory(new PropertyValueFactory<>("menuMakanan"));
        tblMenuMinuman.setCellValueFactory(new PropertyValueFactory<>("menuMinuman"));
        tblTotalHarga.setCellValueFactory(new PropertyValueFactory<>("totalHarga"));

        // Allow editing of specific columns
        tblMenuMakanan.setCellFactory(TextFieldTableCell.forTableColumn());
        tblMenuMakanan.setOnEditCommit(event -> {
            Pesanan pesanan = event.getRowValue();
            pesanan.setMenuMakanan(event.getNewValue());
            updatePesanan(pesanan);
        });

        tblMenuMinuman.setCellFactory(TextFieldTableCell.forTableColumn());
        tblMenuMinuman.setOnEditCommit(event -> {
            Pesanan pesanan = event.getRowValue();
            pesanan.setMenuMinuman(event.getNewValue());
            updatePesanan(pesanan);
        });

        tblTotalHarga.setCellFactory(TextFieldTableCell.forTableColumn());
        tblTotalHarga.setOnEditCommit(event -> {
            Pesanan pesanan = event.getRowValue();
            pesanan.setTotalHarga(event.getNewValue());
            updatePesanan(pesanan);
        });

        // Add action buttons (Delete)
        TableColumn<Pesanan, Void> actionColumn = new TableColumn<>("Aksi");
        actionColumn.setCellFactory(param -> new TableCell<>() {
            private final Button btnDelete = new Button("Hapus");
            private final Button btnDetail = new Button("Detail");
            private final HBox hbox = new HBox(10, btnDelete);
            private final HBox detail = new HBox(10, btnDetail);

            {
                btnDelete.setOnAction(event -> Delete(getTableRow().getItem()));
            }

            {
                btnDetail.setOnAction(event -> {
                    Pesanan pesanan = getTableRow().getItem();
                    if (pesanan != null) {
                        FDetailPesanan.Pesan(pesanan);
                        try {
                            Parent root = FXMLLoader.load(getClass().getResource("FDetailPesanan.fxml"));
                            Stage stage = (Stage) btnDetail.getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Detail");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox container = new HBox(10, btnDelete, btnDetail);
                    setGraphic(container);
                }
            }
        });

        tblPesanan.getColumns().add(actionColumn);
    }

    private void loadDataPesanan() {
        ObservableList<Pesanan> pesananList = FXCollections.observableArrayList();
        try (Connection conn = Config.configDB(); Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT * FROM pesanan");
            while (rs.next()) {
                pesananList.add(new Pesanan(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getInt("no_meja"),
                        rs.getString("tanggal_pesan"),
                        rs.getString("menu_makanan"),
                        rs.getString("menu_minuman"),
                        rs.getString("total_harga")));
            }
            tblPesanan.setItems(pesananList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePesanan(Pesanan pesanan) {
        String query = "UPDATE pesanan SET menu_makanan = ?, menu_minuman = ?, total_harga = ? WHERE id = ?";
        try (Connection conn = Config.configDB(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, pesanan.getMenuMakanan());
            ps.setString(2, pesanan.getMenuMinuman());
            ps.setString(3, pesanan.getTotalHarga());
            ps.setInt(4, pesanan.getId());
            ps.executeUpdate();
            loadDataPesanan();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Delete(Pesanan pesanan) {
        if (pesanan != null) {
            String query = "DELETE FROM pesanan WHERE id = ?";
            try (Connection conn = Config.configDB(); PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, pesanan.getId());
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus pesanan ini?",
                        ButtonType.YES, ButtonType.NO);
                alert.showAndWait();
                if (alert.getResult() == ButtonType.YES) {
                    // Lanjutkan penghapusan
                    ps.executeUpdate();
                    loadDataPesanan();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void Keluar() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("FLogin.fxml"));
            Stage stage = (Stage) btKeluar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void Menu() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("FMakanan.fxml"));
            Stage stage = (Stage) btMenu.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Menu");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void About() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("FAbout.fxml"));
            Stage stage = (Stage) btAbout.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("About");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
