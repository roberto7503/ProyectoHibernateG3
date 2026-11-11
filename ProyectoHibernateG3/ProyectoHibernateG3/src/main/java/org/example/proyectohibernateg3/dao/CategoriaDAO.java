package org.example.proyectohibernateg3.dao;

import jakarta.persistence.EntityManager;
import org.example.proyectohibernateg3.database.JPAUtil;
import org.example.proyectohibernateg3.model.Categoria;

import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listar() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Categoria c ORDER BY c.nombre", Categoria.class).getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public Categoria buscarPorId(Long id) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.find(Categoria.class, id);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}