package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.service.PropriedadeOperacaoException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TalhaoSpatialRepository {
    private final JdbcTemplate jdbc;

    public TalhaoSpatialRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void validarEAtualizarRepresentacao(Long talhaoId) {
        if (talhaoId == null) {
            return;
        }
        Integer resultado = jdbc.queryForObject("""
                DECLARE @talhao_id BIGINT = ?;
                DECLARE @quantidade INT = (
                    SELECT COUNT(*) FROM dbo.propriedade_talhao_vertices WHERE talhao_id = @talhao_id
                );

                IF @quantidade < 3
                BEGIN
                    UPDATE dbo.propriedade_talhoes
                    SET geometria_status_crs = 'CONFIRMADO',
                        geometria_crs_epsg = 4674,
                        geometria_geography = NULL,
                        area_gis_m2 = NULL
                    WHERE id = @talhao_id;
                    SELECT 0;
                    RETURN;
                END;

                DECLARE @wkt NVARCHAR(MAX);
                SELECT @wkt = 'POLYGON((' + STRING_AGG(
                    CONVERT(VARCHAR(40), longitude) + ' ' + CONVERT(VARCHAR(40), latitude)
                        + COALESCE(' ' + CONVERT(VARCHAR(40), altitude_geodesica_m), ''),
                    ', '
                ) WITHIN GROUP (ORDER BY ordem) + ', ' + (
                    SELECT TOP (1) CONVERT(VARCHAR(40), longitude) + ' ' + CONVERT(VARCHAR(40), latitude)
                        + COALESCE(' ' + CONVERT(VARCHAR(40), altitude_geodesica_m), '')
                    FROM dbo.propriedade_talhao_vertices
                    WHERE talhao_id = @talhao_id
                    ORDER BY ordem
                ) + '))'
                FROM dbo.propriedade_talhao_vertices
                WHERE talhao_id = @talhao_id;

                DECLARE @g geography = geography::STGeomFromText(@wkt, 4674);
                IF @g.STIsValid() <> 1
                BEGIN
                    SELECT -1;
                    RETURN;
                END;
                DECLARE @r geography = @g.ReorientObject();
                IF @r.STIsValid() = 1 AND @r.STArea() < @g.STArea()
                    SET @g = @r;

                DECLARE @perimetro geography = (
                    SELECT TOP (1) p.poligono_geography
                    FROM dbo.propriedade_perimetros p
                    JOIN dbo.propriedade_talhoes t ON t.propriedade_id = p.propriedade_id
                    WHERE t.id = @talhao_id
                );
                IF @perimetro IS NULL OR @g.STWithin(@perimetro) <> 1
                BEGIN
                    SELECT -2;
                    RETURN;
                END;

                UPDATE dbo.propriedade_talhoes
                SET geometria_status_crs = 'CONFIRMADO',
                    geometria_crs_epsg = 4674,
                    geometria_geography = @g,
                    area_gis_m2 = ROUND(@g.STArea(), 4)
                WHERE id = @talhao_id;
                SELECT 1;
                """, Integer.class, talhaoId);
        if (resultado == null) {
            throw new PropriedadeOperacaoException(null, "Não foi possível validar a geometria do talhão.");
        }
        if (resultado == -1) {
            throw new PropriedadeOperacaoException("vertices", "A geometria informada para o talhão é inválida.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (resultado == -2) {
            throw new PropriedadeOperacaoException("vertices",
                    "A geometria do talhão deve ficar dentro do perímetro confirmado da propriedade.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }
}
