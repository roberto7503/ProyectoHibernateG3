package org.example.proyectohibernateg3.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.example.proyectohibernateg3.dao.CategoriaDAO;
import org.example.proyectohibernateg3.dao.LibroDAO;
import org.example.proyectohibernateg3.model.Categoria;
import org.example.proyectohibernateg3.model.Libro;

import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class LibroController implements Initializable {

    // Campos de texto y selección
    @FXML
    private TextField txtTitulo;
    @FXML
    private TextField txtAutor;
    @FXML
    private TextField txtAnio;
    @FXML
    private TextField txtPrecio;
    @FXML
    private ComboBox<Categoria> cmbCategoria;

    // Botones
    @FXML
    private Button btnGuardar;
    @FXML
    private Button btnModificar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnCargar;

    // Tabla y columnas
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
    private TableColumn<Libro, String> colCategoria;

    // DAO y variables de estado
    private final LibroDAO libroDAO = new LibroDAO();
    private final CategoriaDAO categoriaDAO = new CategoriaDAO();
    private final ObservableList<Libro> listaLibros = FXCollections.observableArrayList();
    private final ObservableList<Categoria> listaCategorias = FXCollections.observableArrayList();
    private Long idSeleccionado = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarColumnasTabla();
        configurarComboBoxCategoria();
        configurarSeleccionTabla();

        // Cargar datos iniciales
        cargarCategorias();
        cargarLibros();
    }

    private void configurarColumnasTabla() {
        colId.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getId()));
        colTitulo.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitulo()));
        colAutor.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAutor()));
        colAnio.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getAnioPublicacion()).asObject());
        colPrecio.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getPrecio()));

        // Muestra el nombre de la categoría asociada al libro
        colCategoria.setCellValueFactory(cell -> {
            Categoria cat = cell.getValue().getCategoria();
            return new SimpleStringProperty(cat != null ? cat.getNombre() : "Sin categoría");
        });

        tblLibros.setItems(listaLibros);
    }

    private void configurarComboBoxCategoria() {
        cmbCategoria.setItems(listaCategorias);

        cmbCategoria.setConverter(new StringConverter<>() {
            @Override
            public String toString(Categoria categoria) {
                return categoria != null ? categoria.getNombre() : "";
            }

            @Override
            public Categoria fromString(String string) {
                return null;
            }
        });
    }

    private void configurarSeleccionTabla() {
        tblLibros.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, nuevoLibro) -> {
            if (nuevoLibro != null) {
                idSeleccionado = nuevoLibro.getId();
                txtTitulo.setText(nuevoLibro.getTitulo());
                txtAutor.setText(nuevoLibro.getAutor());
                txtAnio.setText(String.valueOf(nuevoLibro.getAnioPublicacion()));
                txtPrecio.setText(nuevoLibro.getPrecio().toString());

                if (nuevoLibro.getCategoria() != null) {
                    for (Categoria cat : listaCategorias) {
                        if (cat.getId().equals(nuevoLibro.getCategoria().getId())) {
                            cmbCategoria.setValue(cat);
                            break;
                        }
                    }
                } else {
                    cmbCategoria.setValue(null);
                }
            }
        });
    }

    public void cargarCategorias() {
        try {
            List<Categoria> categorias = categoriaDAO.listar();
            listaCategorias.setAll(categorias);
        } catch (Exception ex) {
            ex.printStackTrace();
            Throwable cause = ex;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudieron cargar las categorías: " + cause.getMessage());
        }
    }

    @FXML
    public void cargarLibros() {
        try {
            List<Libro> libros = libroDAO.listar();
            listaLibros.setAll(libros);
        } catch (Exception ex) {
            ex.printStackTrace();
            Throwable cause = ex;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al cargar libros: " + cause.getMessage());
        }
    }

    @FXML
    public void guardarLibro(ActionEvent event) {
        try {
            idSeleccionado = null;
            Libro nuevoLibro = obtenerLibroFormulario();

            libroDAO.guardar(nuevoLibro);
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Libro guardado correctamente.");
            limpiarCampos();
            cargarLibros();
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo guardar el libro: " + ex.getMessage());
        }
    }

    @FXML
    public void modificarLibro(ActionEvent event) {
        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecciona un libro de la tabla para modificar.");
            return;
        }

        try {
            Libro libroModificado = obtenerLibroFormulario();
            boolean exito = libroDAO.modificar(libroModificado);

            if (exito) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Libro modificado correctamente.");
                limpiarCampos();
                cargarLibros();
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se encontró el libro en la base de datos.");
            }
        } catch (IllegalArgumentException ex) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación", ex.getMessage());
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo modificar el libro: " + ex.getMessage());
        }
    }

    @FXML
    public void eliminarLibro(ActionEvent event) {
        if (idSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Aviso", "Selecciona un libro de la tabla para eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION, "¿Seguro que deseas eliminar este libro?", ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.showAndWait();

        if (confirmacion.getResult() == ButtonType.YES) {
            try {
                boolean exito = libroDAO.eliminar(idSeleccionado);
                if (exito) {
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Libro eliminado correctamente.");
                    limpiarCampos();
                    cargarLibros();
                } else {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo encontrar el libro para eliminar.");
                }
            } catch (Exception ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al eliminar: " + ex.getMessage());
            }
        }
    }

    @FXML
    public void limpiarCampos() {
        txtTitulo.clear();
        txtAutor.clear();
        txtAnio.clear();
        txtPrecio.clear();
        cmbCategoria.setValue(null);
        idSeleccionado = null;
        tblLibros.getSelectionModel().clearSelection();
    }

    private Libro obtenerLibroFormulario() {
        String titulo = txtTitulo.getText() != null ? txtTitulo.getText().trim() : "";
        String autor = txtAutor.getText() != null ? txtAutor.getText().trim() : "";

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
            throw new IllegalArgumentException("Escribe un precio numérico válido, por ejemplo 12.50.");
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

        Categoria categoria = cmbCategoria.getValue();
        if (categoria == null) {
            throw new IllegalArgumentException("Debes seleccionar una categoría.");
        }

        return new Libro(idSeleccionado, titulo, autor, anio, precio, categoria);
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}