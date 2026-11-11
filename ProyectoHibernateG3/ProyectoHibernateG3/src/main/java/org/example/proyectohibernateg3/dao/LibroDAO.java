package org.example.proyectohibernateg3.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.proyectohibernateg3.database.JPAUtil;
import org.example.proyectohibernateg3.model.Categoria;
import org.example.proyectohibernateg3.model.Libro;

import java.util.List;

public class LibroDAO {

    private Categoria obtenerCategoria(EntityManager em, Libro libro) {
        if (libro == null || libro.getCategoria() == null || libro.getCategoria().getId() == null) {
            throw new IllegalArgumentException("El libro debe contener una categoría con un ID válido.");
        }
        Long categoriaId = libro.getCategoria().getId();
        Categoria categoria = em.find(Categoria.class, categoriaId);

        if (categoria == null) {
            throw new IllegalArgumentException("No se encontró la categoría con ID: " + categoriaId);
        }
        return categoria;
    }

    public void guardar(Libro libro) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Categoria categoriaGestionada = obtenerCategoria(em, libro);
            libro.setCategoria(categoriaGestionada);

            em.persist(libro);
            tx.commit();
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Libro> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // JOIN FETCH previene la LazyInitializationException al cerrar el em
            return em.createQuery("SELECT l FROM Libro l JOIN FETCH l.categoria ORDER BY l.id", Libro.class)
                    .getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public Libro buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            List<Libro> resultados = em.createQuery(
                            "SELECT l FROM Libro l JOIN FETCH l.categoria WHERE l.id = :id", Libro.class)
                    .setParameter("id", id)
                    .getResultList();
            return resultados.isEmpty() ? null : resultados.get(0);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public boolean modificar(Libro libro) {
        if (libro == null || libro.getId() == null) {
            return false;
        }

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Libro libroBD = em.find(Libro.class, libro.getId());

            if (libroBD == null) {
                tx.rollback();
                return false;
            }

            libroBD.setTitulo(libro.getTitulo());
            libroBD.setAutor(libro.getAutor());
            libroBD.setAnioPublicacion(libro.getAnioPublicacion());
            libroBD.setPrecio(libro.getPrecio());

            if (libro.getCategoria() != null && libro.getCategoria().getId() != null) {
                Categoria categoriaGestionada = obtenerCategoria(em, libro);
                libroBD.setCategoria(categoriaGestionada);
            }

            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public boolean eliminar(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Libro libroBD = em.find(Libro.class, id);
            if (libroBD == null) {
                tx.rollback();
                return false;
            }
            em.remove(libroBD);
            tx.commit();
            return true;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}