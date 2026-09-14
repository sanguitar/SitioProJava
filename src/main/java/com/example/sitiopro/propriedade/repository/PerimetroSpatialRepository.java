package com.example.sitiopro.propriedade.repository;

import com.example.sitiopro.propriedade.dto.PerimetroConferenciaResumo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class PerimetroSpatialRepository {
    private final JdbcTemplate jdbc;

    public PerimetroSpatialRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<PerimetroConferenciaResumo> buscar(Long perimetroId) {
        if (perimetroId == null) {
            return Optional.empty();
        }
        return jdbc.query("""
                SELECT sistema_geodesico, crs_epsg, area_documental_ha, perimetro_documental_m,
                       area_calculada_m2, perimetro_calculado_m,
                       CASE WHEN poligono_geography IS NULL THEN NULL ELSE poligono_geography.STIsValid() END AS geometria_valida,
                       CASE WHEN poligono_geography IS NULL THEN NULL ELSE poligono_geography.STAsText() END AS poligono_wkt
                FROM dbo.propriedade_perimetros
                WHERE id = ?
                """, rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
            Boolean valida = rs.getObject("geometria_valida") == null ? null : rs.getBoolean("geometria_valida");
            return Optional.of(new PerimetroConferenciaResumo(
                    rs.getString("sistema_geodesico"),
                    (Integer) rs.getObject("crs_epsg"),
                    rs.getBigDecimal("area_documental_ha"),
                    rs.getBigDecimal("perimetro_documental_m"),
                    rs.getBigDecimal("area_calculada_m2"),
                    rs.getBigDecimal("perimetro_calculado_m"),
                    valida,
                    rs.getString("poligono_wkt")));
        }, perimetroId);
    }

    public void atualizarRepresentacao(Long perimetroId) {
        if (perimetroId == null) {
            return;
        }
        jdbc.update("""
                DECLARE @perimetro_id BIGINT = ?;
                DECLARE @srid INT;
                DECLARE @status_crs VARCHAR(20);
                SELECT @srid = crs_epsg, @status_crs = status_crs
                FROM dbo.propriedade_perimetros
                WHERE id = @perimetro_id;
                IF @status_crs <> 'CONFIRMADO' OR @srid <> 4674
                    OR (SELECT COUNT(*) FROM dbo.propriedade_perimetro_vertices WHERE perimetro_id = @perimetro_id) < 3
                BEGIN
                    UPDATE dbo.propriedade_perimetros
                    SET poligono_geography = NULL, area_calculada_m2 = NULL, perimetro_calculado_m = NULL
                    WHERE id = @perimetro_id;
                    RETURN;
                END;
                DECLARE @wkt NVARCHAR(MAX);
                SELECT @wkt = 'POLYGON((' + STRING_AGG(
                    CONVERT(VARCHAR(40), longitude) + ' ' + CONVERT(VARCHAR(40), latitude)
                    + CASE WHEN altitude_geodesica_m IS NULL THEN '' ELSE ' ' + CONVERT(VARCHAR(40), altitude_geodesica_m) END,
                    ', ') WITHIN GROUP (ORDER BY ordem)
                    + ', ' + (
                        SELECT TOP (1) CONVERT(VARCHAR(40), longitude) + ' ' + CONVERT(VARCHAR(40), latitude)
                            + CASE WHEN altitude_geodesica_m IS NULL THEN '' ELSE ' ' + CONVERT(VARCHAR(40), altitude_geodesica_m) END
                        FROM dbo.propriedade_perimetro_vertices
                        WHERE perimetro_id = @perimetro_id
                        ORDER BY ordem
                    ) + '))'
                FROM dbo.propriedade_perimetro_vertices
                WHERE perimetro_id = @perimetro_id;
                DECLARE @g geography = geography::STGeomFromText(@wkt, 4674);
                IF @g.STIsValid() = 1
                    SET @g = @g.ReorientObject();
                UPDATE dbo.propriedade_perimetros
                SET poligono_geography = CASE WHEN @g.STIsValid() = 1 THEN @g ELSE NULL END,
                    area_calculada_m2 = CASE WHEN @g.STIsValid() = 1 THEN ROUND(@g.STArea(), 4) ELSE NULL END,
                    perimetro_calculado_m = CASE WHEN @g.STIsValid() = 1 THEN ROUND(@g.STLength(), 4) ELSE NULL END
                WHERE id = @perimetro_id;
                """, perimetroId);
    }
}
