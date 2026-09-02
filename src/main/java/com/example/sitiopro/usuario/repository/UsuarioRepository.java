package com.example.sitiopro.usuario.repository;

import com.example.sitiopro.usuario.entity.PerfilUsuario;
import com.example.sitiopro.usuario.entity.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

    boolean existsByLogin(String login);

    long countByPerfilAndAtivoTrue(PerfilUsuario perfil);

    long countByAtivoTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.perfil = :perfil and u.ativo = true order by u.id")
    List<Usuario> buscarAtivosParaAtualizacao(@Param("perfil") PerfilUsuario perfil);

    List<Usuario> findByAtivoTrueOrderByNomeAsc();
}
