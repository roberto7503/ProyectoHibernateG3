package org.example.proyectohibernateg3.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.proyectohibernateg3.dao.LibroDAO;
import org.example.proyectohibernateg3.model.Libro;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class LibroController {
    private final LibroDAO libroDAO = new LibroDAO();
    private Long idSeleccionado;

    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtAutor;
    @FXML
    private TextField txtAnio;
    @FXML
    private TextField txtPrecio;

    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnCargar;
    @FXML
    private Button btnModificar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Libro> tblLibros;
    @FXML
    private TableColumn<Libro, Long> colId;
    @FXML
    private TableColumn<Libro, String> colTitulo;
    @FXML
    private TableColumn<Libro, String> colAutor;
    @FXML
    private TableColumn<Libro, Integer> colAnio;
    @FXML
    private TableColumn<Libro, BigDecimal> colPrecio;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anioPublicacion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));

        tblLibros.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        seleccionarLibro(newValue);
                    } else {
                        idSeleccionado = null;
                        actualizarBotones();
                    }
                }
        );

        actualizarBotones();
        Platform.runLater(this::cargarLibros);
    }

    private void seleccionarLibro(Libro libro) {
        idSeleccionado = libro.getId();
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtAnio.setText(String.valueOf(libro.getAnioPublicacion()));
        txtPrecio.setText(libro.getPrecio().toPlainString());
        actualizarBotones();
    }

    private void actualizarBotones() {
        boolean sinSeleccion = idSeleccionado == null;
        btnModificar.setDisable(sinSeleccion);
        btnEliminar.setDisable(sinSeleccion);
    }

    private Libro obtenerLibroFormulario() {
        String titulo = txtTitulo.getText().trim();
        String autor = txtAutor.getText().trim();
        if (titulo.isEmpty() || autor.isEmpty()) {
            throw new IllegalArgumentException("Completa el título y el autor.");
        }
        if (titulo.codePointCount(0, titulo.length()) > 150
                || autor.codePointCount(0, autor.length()) > 100) {
            throw new IllegalArgumentException("El título admite 150 caracteres y el autor, 100.");
        }

        int anio;
        try {
            anio = Integer.parseInt(txtAnio.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Escribe el año como un número entero.");
        }
        if (anio < 1450 || anio > 2100) {
            throw new IllegalArgumentException("El año debe estar entre 1450 y 2100.");
        }

        BigDecimal precio;
        try {
            precio = new BigDecimal(txtPrecio.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Escribe un precio numérico, por ejemplo 12,50.");
        }
        if (precio.signum() < 0) {
            throw new IllegalArgumentException("El precio debe ser cero o mayor.");
        }
        if (precio.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new IllegalArgumentException("El precio admite hasta ocho cifras antes del separador decimal.");
        }
        if (precio.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Escribe un precio con hasta dos decimales.");
        }

        return new Libro(idSeleccionado, titulo, autor, anio, precio);
    }

    @FXML
    private void guardarLibro() {
        try {
            Libro libro = obtenerLibroFormulario();
            libro.setId(null); // Debe ser null para autoincremento
            libroDAO.guardar(libro);
            limpiarCampos();
            cargarLibros();
        } catch (IllegalArgumentException ex) {
            mostrarAviso(Alert.AlertType.WARNING, ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarError("No se pudo guardar el libro.", ex);
        }
    }

    @FXML
    private void modificarLibro() {
        try {
            Libro libro = obtenerLibroFormulario();
            boolean modificado = libroDAO.modificar(libro);
            if (!modificado) {
                mostrarAviso(Alert.AlertType.INFORMATION, "El libro seleccionado ya no existe.");
            }

            limpiarCampos();
            cargarLibros();
        } catch (IllegalArgumentException ex) {
            mostrarAviso(Alert.AlertType.WARNING, ex.getMessage());
        } catch (RuntimeException ex) {
            mostrarError("No se pudo modificar el libro.", ex);
        }
    }

    @FXML
    private void eliminarLibro() {
        try {
            Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacion.setTitle("Confirmacion de eliminacion de datos");
            confirmacion.setHeaderText("Eliminar libro");
            confirmacion.setContentText("¿Está seguro que desea eliminar el registro seleccionado?");

            Optional<ButtonType> resultado = confirmacion.showAndWait();
            if (resultado.isEmpty() || resultado.get() != ButtonType.OK) {
                return;
            }

            boolean eliminado = libroDAO.eliminar(idSeleccionado);
            if (!eliminado) {
                mostrarAviso(Alert.AlertType.INFORMATION, "El libro seleccionado ya no existe.");
            }

            limpiarCampos();
            cargarLibros();
        } catch (RuntimeException ex) {
            mostrarError("No se pudo eliminar el libro.", ex);
        }
    }

    @FXML
    private void cargarLibros() {
        try {
            List<Libro> libros = libroDAO.listar();
            tblLibros.setItems(FXCollections.observableList(libros));
        } catch (RuntimeException ex) {
            mostrarError("No se pudieron cargar los libros.", ex);
        }
    }

    private void mostrarError(String mensaje, RuntimeException ex) {
        ex.printStackTrace();
        mostrarAviso(Alert.AlertType.ERROR,
                mensaje + " Revisa PostgreSQL, la tabla catalogos.libro y la variable BD_PASSWORD. "
                        + "Puedes consultar el detalle en la consola.");
    }

    private void mostrarAviso(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje);
        alerta.initOwner(tblLibros.getScene().getWindow());
        alerta.setTitle("Registro de libros");
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    @FXML
    private void limpiarCampos() {
        tblLibros.getSelectionModel().clearSelection();
        idSeleccionado = null;
        txtTitulo.clear();
        txtAutor.clear();
        txtAnio.clear();
        txtPrecio.clear();
        actualizarBotones();
    }

}
