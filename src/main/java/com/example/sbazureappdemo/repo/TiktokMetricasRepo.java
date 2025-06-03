package com.example.sbazureappdemo.repo;
import com.example.sbazureappdemo.AUTHORGRAPHS.AuthorGraphsRenderer;
import com.example.sbazureappdemo.DBQUERY.FiltrosRequestDB;
import com.example.sbazureappdemo.PAGRAPHS.PaGraphsRenderer;
import com.example.sbazureappdemo.model.TiktokMetricas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.ParseException;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


// Repositorio que maneja las operaciones de acceso a la base de datos
@Repository
public class TiktokMetricasRepo {
    Logger logger = LoggerFactory.getLogger(TiktokMetricasRepo.class);


    // Utiliza `JdbcTemplate` para interactuar con la base de datos y realizar operaciones de inserción de registros.
    // Inyección de la plantilla JDBC para ejecutar consultas SQL
    @Autowired
    private JdbcTemplate jdbc;

    public void saveAll(List<TiktokMetricas> metricas) {
        try {
        //  Sentencia SQL para la inserción de datos en la tabla h_metricapublicacion
        String sql = "INSERT INTO h_metricapublicacion (" +
                     "codpublicacion, codautora, codescena, codlibro, numescena, tippublicacion, " +
                     "codposteador, fecpublicacion, horapublicacion, nbrcuentatiktok, urlpublicacion, " +
                     "numviews, numlikes, numsaves, numreposts, numcomments, " +
                     "numengagement, numinteractions, deshashtags, nrohashtag, urlsounds, codregionposteo, fecreacionregistro, horacreacionregistro, codusuarioauditoria" +
                     ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        // Lista para almacenar los parámetros de cada fila a insertar
        List<Object[]> batchArgs = new ArrayList<>();

        // Recorrer la lista de métricas y agregar los valores de cada objeto en batchArgs
        for (TiktokMetricas m : metricas) {    
            batchArgs.add(new Object[]{
                m.getCodpublicacion(), 
                m.getCodautora(), 
                m.getCodescena(), 
                m.getCodlibro(), 
                m.getNumescena(),
                m.getTippublicacion(), 
                m.getCodposteador(), 
                m.getFecpublicacionAsDate(),    // Convierte String a java.sql.Date
                m.getHorapublicacionAsTime(),   // Convierte String a java.sql.Time
                m.getNbrcuentatiktok(), 
                m.getUrlpublicacion(),
                m.getNumviews(), 
                m.getNumlikes(), 
                m.getNumsaves(), 
                m.getNumreposts(),
                m.getNumcomments(), 
                m.getNumengagement(), 
                m.getNuminteractions(), 
                m.getDeshashtags(), 
                m.getNrohashtag(), 
                m.getUrlsounds(), 
                m.getCodregionposteo(), 
                m.getFecreacionregistroAsDate(),        // Convierte String a java.sql.Date
                m.getHoracreacionregistroAsTime(),       // Convierte String a java.sql.Time
                m.getUserIdentification()
        });
        }

    // Se usa `batchUpdate` para insertar múltiples registros de manera eficiente.
    //  arreglo de enteros donde cada posición indica el número de filas afectadas por cada ejecución del batch
    int[] insertedRows = jdbc.batchUpdate(sql, batchArgs);
    logger.info("Registros insertados en BD: " + Arrays.stream(insertedRows).sum());
    } catch (Exception e) {
        logger.error("Error al insertar registros en la BD", e);
    }
    }




    
    public List<Map<String,Object>> FilterConnection(FiltrosRequestDB request) {
        try {
            String sql =  """ 
            SELECT 
                mp.codpublicacion AS "Post Code",
                mp.codautora AS "Author Code",
                COALESCE(a.nbautora, 'Not found: N/A') || ' ' || COALESCE(a.apeautora, '') AS "Author name",
                mp.codlibro AS "Book Code",
                COALESCE(lb.deslibro, 'Not found: N/A') AS "Book name",
                mp.numescena AS "Number of Scene",
                mp.codescena AS "Scene Code",
                COALESCE(esc.desscena, 'Not found: N/A') AS "Scene name",
                mp.tippublicacion AS "Code Post Type",
                COALESCE(tp.despost, 'Not found: N/A') AS "Post Type",
                mp.codposteador AS "PA Code",
                COALESCE(po.nbposteador, 'Not found: N/A') || ' ' || COALESCE(po.apepatposteador, '') || ' ' || COALESCE(po.apematposteador, '') AS "PA name",
                mp.fecpublicacion AS "Date posted",
                mp.horapublicacion AS "Time posted",
                mp.nbrcuentatiktok AS "TikTok Username",
                mp.urlpublicacion AS "Post URL",
                mp.numviews AS "Views",
                mp.numlikes AS "Likes",
                mp.numcomments AS "Comments",
                mp.numreposts AS "Reposted",
                mp.numsaves AS "Saves",
                mp.numengagement AS "Engagement rate",
                mp.numinteractions AS "Interactions",
                mp.deshashtags AS "Hashtags",
                mp.nrohashtag AS "Number of Hashtags",
                mp.urlsounds AS "Sound URL",
                mp.codregionposteo AS "Region Code",
                mp.fecreacionregistro AS "Tracking date",
                mp.horacreacionregistro AS "Tracking time",
                mp.codusuarioauditoria AS "Logged-in User"
            FROM h_metricapublicacion mp
            INNER JOIN (
                SELECT 
                    codpublicacion, 
                    MAX(mpint.fecreacionregistro+mpint.horacreacionregistro) AS max_fecreacionregistro
                FROM h_metricapublicacion mpint
                WHERE 1=1
            """;
        
        List<Object> parametros = new ArrayList<>();
        if (!request.getPubStartDate().isEmpty() && !request.getPubFinishtDate().isEmpty()) {
            sql += " AND mpint.fecpublicacion BETWEEN ? AND ?";
            parametros.add(java.sql.Date.valueOf(request.getPubStartDate()));
            parametros.add(java.sql.Date.valueOf(request.getPubFinishtDate()));
        }

        if (!request.getTrackStartDate().isEmpty() && !request.getTrackFinishtDate().isEmpty()) {
            sql += " AND mpint.fecreacionregistro BETWEEN ? AND ?";
            parametros.add(java.sql.Date.valueOf(request.getTrackStartDate()));
            parametros.add(java.sql.Date.valueOf(request.getTrackFinishtDate()));
        }


        if (!request.getAuthorList().isEmpty()) {
            sql += " AND mpint.codautora IN (" 
            + String.join(",", Collections.nCopies(request.getAuthorList().size(), "?"))
            + ")";
            parametros.addAll(request.getAuthorList());
        }

        if (!request.getBookList().isEmpty()) {
            sql += " AND mpint.codlibro IN (" 
            + String.join(",", Collections.nCopies(request.getBookList().size(), "?"))
            + ")";
            parametros.addAll(request.getBookList());
        }

        if (!request.getPAList().isEmpty()) {
            sql += " AND mpint.codposteador IN (" 
            + String.join(",", Collections.nCopies(request.getPAList().size(), "?"))
            + ")";
            parametros.addAll(request.getPAList());
        }

        if (!request.getSceneList().isEmpty()) {
            sql += " AND mpint.codescena IN (" 
            + String.join(",", Collections.nCopies(request.getSceneList().size(), "?"))
            + ")";
            parametros.addAll(request.getSceneList());
        }

        if (!request.getTypePostList().isEmpty()) {
            sql += " AND mpint.tippublicacion IN (" 
            + String.join(",", Collections.nCopies(request.getTypePostList().size(), "?"))
            + ")";
            parametros.addAll(request.getTypePostList());
        }

        if (!request.getAccountList().isEmpty()) {
            sql += " AND mpint.nbrcuentatiktok IN (" 
            + String.join(",", Collections.nCopies(request.getAccountList().size(), "?"))
            + ")";
            parametros.addAll(request.getAccountList());
        }

        if (!request.getPostIDList().isEmpty()) {
            sql += " AND mpint.codpublicacion IN (" 
            + String.join(",", Collections.nCopies(request.getPostIDList().size(), "?"))
            + ")";
            parametros.addAll(request.getPostIDList());
        }

        if (!request.getRegionList().isEmpty()) {
            sql += " AND mpint.codregionposteo IN (" 
            + String.join(",", Collections.nCopies(request.getRegionList().size(), "?"))
            + ")";
            parametros.addAll(request.getRegionList());
        }

        sql += """
                    GROUP BY mpint.codpublicacion
                ) base
                ON mp.codpublicacion = base.codpublicacion 
                AND (mp.fecreacionregistro+mp.horacreacionregistro) = base.max_fecreacionregistro
                LEFT JOIN m_autora a ON mp.codautora = a.codautora
                LEFT JOIN m_escenalibro esc ON mp.codescena = esc.codescena
                LEFT JOIN m_libro lb ON mp.codlibro = lb.codlibro
                LEFT JOIN m_tipopost tp ON mp.tippublicacion = tp.tippublicacion
                LEFT JOIN m_posteadorasistente po ON mp.codposteador = po.codposteador
                WHERE 1=1
            """;


            if (!request.getLikesMin().isEmpty() || !request.getLikesMax().isEmpty()) { 
                sql += " AND mp.numlikes BETWEEN ? AND ?";
                parametros.add(request.getLikesMin().isEmpty() ? 0 : Integer.parseInt(request.getLikesMin()));
                parametros.add(request.getLikesMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getLikesMax()));
            }
            
            
            if (!request.getInteractionMin().isEmpty() || !request.getInteractionMax().isEmpty()) {
                sql += " AND mp.numinteractions BETWEEN ? AND ?";
                parametros.add(request.getInteractionMin().isEmpty() ? 0: Integer.parseInt(request.getInteractionMin()));
                parametros.add(request.getInteractionMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getInteractionMax()));
            }

            if (!request.getSavesMin().isEmpty() || !request.getSavesMax().isEmpty()) {
                sql += " AND mp.numsaves BETWEEN ? AND ?";
                parametros.add(request.getSavesMin().isEmpty() ? 0: Integer.parseInt(request.getSavesMin()));
                parametros.add(request.getSavesMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getSavesMax()));
            }
            

            if (!request.getViewsMin().isEmpty() || !request.getViewsMax().isEmpty()) {
                sql += " AND mp.numviews BETWEEN ? AND ?";
                parametros.add(request.getViewsMin().isEmpty() ? 0: Integer.parseInt(request.getViewsMin()));
                parametros.add(request.getViewsMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getViewsMax()));
            }

            
            if (!request.getEngagementMin().isEmpty() || !request.getEngagementMax().isEmpty()) {
                sql += " AND mp.numengagement BETWEEN ? AND ?";
                parametros.add(request.getEngagementMin().isEmpty() ? 0: Double.parseDouble(request.getEngagementMin()));
                parametros.add(request.getEngagementMax().isEmpty() ? 100 : Double.parseDouble(request.getEngagementMax()));
            }


        // Ejecutar la consulta y retornar los resultados
        //logger.info("🔍 Consulta SQL: " + sql);
        return jdbc.queryForList(sql.toString(),parametros.toArray());
    }
    catch (Exception e) {
        logger.error("Bad SQL grammar",e);
        return List.of();
    }
    }


    public List<Map<String,Object>> showDBRecordsConnection(Object tableName) {
        try {
            String sql = "SELECT * FROM " + tableName;
            return jdbc.queryForList(sql);
        }
        catch (Exception e) {
            logger.error("Bad SQL grammar",e);
            return List.of();
        }
    }
    
    public Map<String,Object> uploadRecordsExcelFileConnection(Map<String,Object> response, String userId) {
         // Obtener nombres de las columnas de forma dinámica
        try {
            logger.info("Iniciando guardado de datos del Excel importado.");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> records = (List<Map<String, Object>>) response.getOrDefault("records", Collections.emptyList());
        @SuppressWarnings("unchecked")
        List<String> conflictKeys = (List<String>) response.getOrDefault("conflict", Collections.emptyList());
        if (records.isEmpty()) {
            logger.error("No records to process");
            return Map.of("error", "No records to process");
        }
        
        for (Map<String, Object> record : records) {
            record.put("codusuarioauditoria", userId);
        }
        Set<String> columnNames = new HashSet<>(records.get(0).keySet());
        String tableName = (String) response.getOrDefault("table", "tabla_default");


        // Agregar las columnas obligatorias con timestamps de zona horaria fija
        columnNames.add("fecreacionregistro");
        columnNames.add("horacreacionregistro");
        columnNames.add("fecactualizacionregistro");
        columnNames.add("horaactualizacionregistro");
        String columns = String.join(", ", columnNames);
        String placeholders = columnNames.stream()
    .map(col -> {
        if (col.equals("fecreacionregistro") || col.equals("fecactualizacionregistro")) {
            return "CURRENT_DATE"; // Usa CURRENT_DATE para fechas
        } else if (col.equals("horacreacionregistro") || col.equals("horaactualizacionregistro")) {
            return "CURRENT_TIME AT TIME ZONE 'America/Lima'"; // Usa CURRENT_TIME AT TIME ZONE 'America/Lima'
        } else {
            return "?"; // Para las demás columnas usa valores dinámicos
        }
    }).collect(Collectors.joining(", "));



     // Construcción de `ON CONFLICT` dinámico
     String conflictClause = conflictKeys.isEmpty() ? "" :
                "ON CONFLICT (" + String.join(", ", conflictKeys) + ") DO UPDATE SET " +
                        columnNames.stream().filter(col -> !col.equals("fecreacionregistro") && !col.equals("horacreacionregistro")) // Excluir estos campos
                                .map(col -> {
                                    if (col.equals("fecactualizacionregistro")) {
                                        return col + " = CURRENT_DATE"; // Actualiza con la fecha actual
                                    } else if (col.equals("horaactualizacionregistro")) {
                                        return col + " = CURRENT_TIME AT TIME ZONE 'America/Lima'"; // Actualiza con la hora actual
                                    } else {
                                        return col + " = EXCLUDED." + col; // Asegura que las claves de conflicto también se actualicen
                                    }
                                })
                                .collect(Collectors.joining(", "));



    // Construcción de la consulta final
    String sql = String.format("""
        INSERT INTO %s (%s) VALUES (%s)
        %s;
    """, tableName, columns, placeholders, conflictClause);


    jdbc.batchUpdate(sql, records, records.size(), (ps, record) -> {
        int index = 1;
        for (String col : columnNames) {
            if (!col.equals("fecreacionregistro") && !col.equals("horacreacionregistro") &&
                !col.equals("fecactualizacionregistro") && !col.equals("horaactualizacionregistro")) {
                
                Object value = record.getOrDefault(col, null);

                try {
                    if (col.equalsIgnoreCase("fecfinperiodometa") || col.equalsIgnoreCase("fecinicioperiodometa")) {
                        if (value == null || value.toString().trim().isEmpty()) {
                            ps.setNull(index, java.sql.Types.DATE); // Asignar NULL si el valor es vacío
                        } 
                        else if (value instanceof String) { 
                            // Si es String, parsear al formato correcto
                            SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                            java.util.Date fecha = sdf.parse((String) value);
                            ps.setDate(index, new java.sql.Date(fecha.getTime()));
                        } 
                        else if (value instanceof java.util.Date) { 
                            // Si ya es Date, convertirlo a java.sql.Date
                            ps.setDate(index, new java.sql.Date(((java.util.Date) value).getTime()));
                        } 
                        else if (value instanceof LocalDate) { 
                            // Si es LocalDate, convertirlo directamente
                            ps.setDate(index, java.sql.Date.valueOf((LocalDate) value));
                        } 
                        else {
                            ps.setNull(index, java.sql.Types.DATE); // Si el formato es incorrecto, asignar NULL
                        }
                    } 
                    else {
                        ps.setObject(index, value); // Asignar valores normales
                    }
                } catch (ParseException e) {
                    logger.error("❌ Error al parsear la fecha: " + value);
                    e.printStackTrace();
                    ps.setNull(index, java.sql.Types.DATE); // En caso de error, asignar null
                }
                
                index++;
            }
        }
    });
    logger.info("Datos del Excel guardados correctamente en la BD");
    return Map.of("message", ((List<?>) response.get("records")).size());
    } catch (Exception e) {
        logger.error("Bad SQL grammar", e);
    return Map.of();
    }
    }


    public List<Map<String,Object>> GetDatosPaGraphConnectionDB(PaGraphsRenderer filtros) {
        try {
            String placeholders = filtros.getPAList().stream().map(p->"?").collect(Collectors.joining(", "));
            String sql =String.format("""
            SELECT
                T.codposteador, T.nbrPosteador, T.fecinicioperiodometa, T.fecfinperiodometa, T.numpostemeta,
                R.ctdPublicaciones as numpostereal, 

                CASE
                    WHEN T.numpostemeta = 0 THEN 0
                    ELSE ROUND((CAST(R.ctdPublicaciones AS NUMERIC)/CAST(T.numpostemeta AS NUMERIC))*100,0)
                END AS Eficacia,
                R.promNumviews, R.promNumlikes, R.promNumsaves, 
                R.promNumreposts, R.promNumcomments, R.promNumengagement, R.promInteraction
            FROM
            (
                SELECT po.codposteador, 
                COALESCE(po.nbposteador, '') || ' ' || COALESCE(po.apepatposteador, '') as nbrPosteador,
                pm.fecinicioperiodometa, pm.fecfinperiodometa, pm.numpostemeta
                FROM 
                m_posteadorasistente po 
                left join m_metaposteadorasistente pm 
                on po.codposteador = pm.codposteador
                and pm.fecinicioperiodometa = ?
                and pm.fecfinperiodometa = ?
                WHERE
                po.codposteador in (%s)
            ) T
            LEFT JOIN
            (
                select A.codposteador, count(a.codpublicacion) ctdPublicaciones, 
                        ROUND(avg(a.numviews),0) as promNumviews, ROUND(avg(a.numlikes),2) as promNumlikes, ROUND(avg(a.numsaves),2) as promNumsaves, 
                        ROUND(avg(a.numreposts),2) as promNumreposts, ROUND(avg(a.numcomments),2) as promNumcomments,
                        ROUND(avg(a.numengagement),2) as promNumengagement,
                        ROUND(avg(a.numinteractions),0) as promInteraction
                FROM
                    (
                    select 
                    mp.codpublicacion, mp.codposteador, mp.fecpublicacion, mp.horapublicacion,
                    mp.numviews, mp.numlikes, mp.numsaves, mp.numreposts, mp.numcomments, mp.numengagement, mp.numinteractions
		            from
		            h_metricapublicacion mp inner join
		            ( 	SELECT
				            mpint.codpublicacion, max(mpint.fecreacionregistro+mpint.horacreacionregistro) as max_fecreacionregistro
			            from
				            h_metricapublicacion mpint
			            WHERE
				            mpint.fecpublicacion BETWEEN ? and ? AND
				            mpint.codposteador in (%s)
			            GROUP BY
				            mpint.codpublicacion ) base
		            on mp.codpublicacion = base.codpublicacion and (mp.fecreacionregistro+mp.horacreacionregistro) = base.max_fecreacionregistro		
		            where mp.numviews >= 100
                    ) A
	            GROUP BY 
	            A.codposteador
            ) R
            ON T.codposteador = R.codposteador
        """, placeholders, placeholders);
        List<Object> params= new ArrayList<>() ;
        params.add(java.sql.Date.valueOf(filtros.getStartDate()));
        params.add(java.sql.Date.valueOf(filtros.getFinishDate()));
        params.addAll(filtros.getPAList());

        params.add(java.sql.Date.valueOf(filtros.getStartDate()));
        params.add(java.sql.Date.valueOf(filtros.getFinishDate()));
        params.addAll(filtros.getPAList());
        
        //logger.info("🔍 Consulta SQL: " + sql);
        //logger.info("📌 Parámetros: " + params);
        logger.info("Datos de los PA's obtenidos completadas con éxito");
        return jdbc.queryForList(sql,params.toArray());
        } catch (Exception e) {
            logger.error("Bad SQL Grammar", e);
            return List.of();
        }
    }




    public List<Map<String,Object>> GetDatosAuthorGraphsConnectionDBQuery1(AuthorGraphsRenderer filtros) {
        try {
            String placeholders_authors = filtros.getAuthorList().stream().map(p->"?").collect(Collectors.joining(", "));
            String sql =String.format("""
            SELECT
                T.codautora, T.nbrAutora,
                R.promNumviews, R.promNumlikes, R.promNumsaves, 
                R.promNumreposts, R.promNumcomments, R.promNumengagement, R.promInteraction
                FROM
            (   
                SELECT au.codautora,
                COALESCE(au.nbautora, '') || ' ' || COALESCE(au.apeautora, '') as nbrAutora
                FROM m_autora au 
                WHERE au.codautora in (%s)
            ) T
            LEFT JOIN  (
	            select A.codautora, count(A.codpublicacion) ctdPublicaciones, 
                        ROUND(avg(A.numviews),0) as promNumviews, ROUND(avg(A.numlikes),2) as promNumlikes, ROUND(avg(A.numsaves),2) as promNumsaves, 
                        ROUND(avg(A.numreposts),2) as promNumreposts, ROUND(avg(A.numcomments),2) as promNumcomments,
                        ROUND(avg(A.numengagement),0) as promNumengagement, ROUND(avg(a.numinteractions),0) as promInteraction
	            FROM  (
                        select 
                            mp.codpublicacion, mp.codautora, mp.fecpublicacion, mp.horapublicacion,
                            mp.numviews, mp.numlikes, mp.numsaves, mp.numreposts, mp.numcomments, mp.numengagement, mp.numinteractions
                        from h_metricapublicacion mp 
                        inner join  ( 	
                            SELECT
                                mpint.codpublicacion, max(mpint.fecreacionregistro+mpint.horacreacionregistro) as max_fecreacionregistro
                            from
                                h_metricapublicacion mpint
                            WHERE
                                mpint.fecpublicacion BETWEEN ? and ? AND
                                mpint.codautora in (%s)
                            GROUP BY
                                mpint.codpublicacion ) base
		                on mp.codpublicacion = base.codpublicacion and (mp.fecreacionregistro+mp.horacreacionregistro) = base.max_fecreacionregistro
		                where mp.numviews >= 100
		                ) A
	            GROUP BY 
	            A.codautora
            ) R
            ON T.codautora = R.codautora
        """, placeholders_authors, placeholders_authors);
        List<Object> params= new ArrayList<>() ;
        params.addAll(filtros.getAuthorList());
        params.add(java.sql.Date.valueOf(filtros.getStartDate()));
        params.add(java.sql.Date.valueOf(filtros.getFinishDate()));
        params.addAll(filtros.getAuthorList());
        //logger.info("🔍 Consulta SQL1: " + sql);
        //logger.info("📌 Parámetros: " + params);
        return jdbc.queryForList(sql,params.toArray());
        } catch (Exception e) {
            logger.error("Bad SQL Grammar", e);
            return List.of();
        }
    }



    public List<Map<String,Object>> GetDatosAuthorGraphsConnectionDBQuery2(AuthorGraphsRenderer filtros) {
        try {
            String placeholders_authors = filtros.getAuthorList().stream().map(p->"?").collect(Collectors.joining(", "));
            String sql =String.format("""
            SELECT
                T.codautora, T.nbrAutora,
                R.SumNumviews,R.fecpublicacion
            FROM
            (   
                SELECT au.codautora,
                COALESCE(au.nbautora, '') || ' ' || COALESCE(au.apeautora, '') as nbrAutora
                FROM m_autora au 
                WHERE au.codautora in (%s)
            ) T
            LEFT JOIN  (
	            select 
                    A.codautora, count(A.codpublicacion) ctdPublicaciones,
		    	    SUM(A.numviews) as SumNumviews, A.fecpublicacion
                FROM
                    (
                    select mp.codpublicacion, mp.codautora, mp.fecpublicacion, mp.horapublicacion,
		            mp.numviews
                        from h_metricapublicacion mp inner join  ( 	
                            SELECT
                                mpint.codpublicacion, max(mpint.fecreacionregistro+mpint.horacreacionregistro) as max_fecreacionregistro
                            from
                                h_metricapublicacion mpint
                            WHERE
                                mpint.fecpublicacion BETWEEN ? and ? AND
                                mpint.codautora in (%s)
                            GROUP BY
                                mpint.codpublicacion ) base
		                on mp.codpublicacion = base.codpublicacion and (mp.fecreacionregistro+mp.horacreacionregistro) = base.max_fecreacionregistro
		                where mp.numviews >= 100
		            ) A
	            GROUP BY 
	            A.fecpublicacion, A.codautora
            ) R
            ON T.codautora = R.codautora
        """, placeholders_authors, placeholders_authors);
        List<Object> params= new ArrayList<>() ;
        params.addAll(filtros.getAuthorList());
        params.add(java.sql.Date.valueOf(filtros.getStartDate()));
        params.add(java.sql.Date.valueOf(filtros.getFinishDate()));
        params.addAll(filtros.getAuthorList());
        //logger.info("🔍 Consulta SQL2: " + sql);
        //logger.info("📌 Parámetros: " + params);
        return jdbc.queryForList(sql,params.toArray());
        } catch (Exception e) {
            logger.error("Bad SQL Grammar", e);
            return List.of();
        }
    }






    public List<Map<String,Object>> getLastProcessedDataFromApifyConnection(String startDate, String finishDate, List<String> accountList) {
        String placeholders = accountList.stream().map(p->"?").collect(Collectors.joining(", "));
        String sql = String.format("""
            select 
                mp.codpublicacion AS "Post Code",
                mp.codautora AS "Author Code",
                COALESCE(a.nbautora, 'Not found: N/A') || ' ' || COALESCE(a.apeautora, '') AS "Author name",
                mp.codlibro AS "Book Code",
                COALESCE(lb.deslibro, 'Not found: N/A') AS "Book name",
                mp.codescena AS "Scene Code",
                COALESCE(esc.desscena, 'Not found: N/A') AS "Scene name",
                mp.numescena AS "Number of Scene",
                mp.tippublicacion AS "Code Post Type",
                COALESCE(tp.despost, 'Not found: N/A') AS "Post Type",
                mp.codposteador AS "PA Code",
                COALESCE(po.nbposteador, 'Not found: N/A') || ' ' || COALESCE(po.apepatposteador, '') || ' ' || COALESCE(po.apematposteador, '') AS "PA name",
                mp.fecpublicacion AS "Date posted",
                mp.horapublicacion AS "Time posted",
                mp.nbrcuentatiktok AS "TikTok Username",
                mp.urlpublicacion AS "Post URL",
                mp.numviews AS "Views",
                mp.numlikes AS "Likes",
                mp.numcomments AS "Comments",
                mp.numreposts AS "Reposted",
                mp.numsaves AS "Saves",
                mp.numengagement AS "Engagement rate",
                mp.numinteractions AS "Interactions",
                mp.deshashtags AS "Hashtags",
                mp.nrohashtag AS "Number of Hashtags",
                mp.urlsounds AS "Sound URL",
                mp.codregionposteo AS "Region Code",
                mp.fecreacionregistro AS "Tracking date",
                mp.horacreacionregistro AS "Tracking time",
                mp.codusuarioauditoria AS "Logged-in User"
        from
        h_metricapublicacion mp inner join ( 
            SELECT mpint.codpublicacion, max(mpint.fecreacionregistro+mpint.horacreacionregistro) as max_fecreacionregistro
            from h_metricapublicacion mpint
            WHERE
                mpint.fecpublicacion BETWEEN ? and ? AND 
                mpint.nbrcuentatiktok in (%s)
            GROUP BY
                mpint.codpublicacion ) base
        on mp.codpublicacion = base.codpublicacion and (mp.fecreacionregistro+mp.horacreacionregistro) = base.max_fecreacionregistro
        left join m_autora a on mp.codautora = a.codautora
        left join m_escenalibro esc on mp.codescena = esc.codescena
        left join m_libro lb on mp.codlibro = lb.codlibro
        left join m_tipopost tp on mp.tippublicacion = tp.tippublicacion
        left join m_posteadorasistente po on mp.codposteador = po.codposteador
                """,placeholders);
        List<Object> params= new ArrayList<>();
        params.add(java.sql.Date.valueOf(startDate));
        params.add(java.sql.Date.valueOf(finishDate));
        params.addAll(accountList);
        return jdbc.queryForList(sql,params.toArray());
    }











    public List<Map<String, Object>> scoreSceneConnectionV2(FiltrosRequestDB request) {
        try {
            StringBuilder sql = new StringBuilder("""
                WITH base AS (
                    SELECT
                        mpint.codpublicacion,
                        MAX(mpint.fecreacionregistro + mpint.horacreacionregistro) AS max_fecreacionregistro
                    FROM h_metricapublicacion mpint
                    WHERE 1=1
                """);

            List<Object> parametros = new ArrayList<>();

            //
            // 1️⃣  Filtros dinámicos para el CTE “base”
            //
            if (!request.getPubStartDate().isEmpty() && !request.getPubFinishtDate().isEmpty()) {
                sql.append(" AND mpint.fecpublicacion BETWEEN ? AND ?\n");
                parametros.add(java.sql.Date.valueOf(request.getPubStartDate()));
                parametros.add(java.sql.Date.valueOf(request.getPubFinishtDate()));
            }

            if (!request.getTrackStartDate().isEmpty() && !request.getTrackFinishtDate().isEmpty()) {
                sql.append(" AND mpint.fecreacionregistro BETWEEN ? AND ?\n");
                parametros.add(java.sql.Date.valueOf(request.getTrackStartDate()));
                parametros.add(java.sql.Date.valueOf(request.getTrackFinishtDate()));
            }

            if (!request.getAuthorList().isEmpty()) {
                sql.append(" AND mpint.codautora IN (")
                   .append(String.join(",", Collections.nCopies(request.getAuthorList().size(), "?")))
                   .append(")\n");
                parametros.addAll(request.getAuthorList());
            }

            if (!request.getBookList().isEmpty()) {
                sql.append(" AND mpint.codlibro IN (")
                   .append(String.join(",", Collections.nCopies(request.getBookList().size(), "?")))
                   .append(")\n");
                parametros.addAll(request.getBookList());
            }

            if (!request.getPAList().isEmpty()) {
                sql.append(" AND mpint.codposteador IN (")
                   .append(String.join(",", Collections.nCopies(request.getPAList().size(), "?")))
                   .append(")\n");
                parametros.addAll(request.getPAList());
            }

            if (!request.getSceneList().isEmpty()) {
                sql.append(" AND mpint.codescena IN (")
                   .append(String.join(",", Collections.nCopies(request.getSceneList().size(), "?")))
                   .append(")\n");
                parametros.addAll(request.getSceneList());
            }

            if (!request.getTypePostList().isEmpty()) {
                sql.append(" AND mpint.tippublicacion IN (")
                   .append(String.join(",", Collections.nCopies(request.getTypePostList().size(), "?")))
                   .append(")\n");
                parametros.addAll(request.getTypePostList());
            }

            if (!request.getAccountList().isEmpty()) {
                sql.append(" AND mpint.nbrcuentatiktok IN (")
                   .append(String.join(",", Collections.nCopies(request.getAccountList().size(), "?")))
                   .append(")\n");
                parametros.addAll(request.getAccountList());
            }

            if (!request.getPostIDList().isEmpty()) {
                sql.append(" AND mpint.codpublicacion IN (")
                   .append(String.join(",", Collections.nCopies(request.getPostIDList().size(), "?")))
                   .append(")\n");
                parametros.addAll(request.getPostIDList());
            }

            if (!request.getRegionList().isEmpty()) {
                sql.append(" AND mpint.codregionposteo IN (")
                   .append(String.join(",", Collections.nCopies(request.getRegionList().size(), "?")))
                   .append(")\n");
                parametros.addAll(request.getRegionList());
            }

            // Cierra el bloque del CTE “base”
            sql.append("""
                    GROUP BY mpint.codpublicacion
                ),
                filtradas AS (
                    SELECT
                        mp.codautora,
                        mp.codlibro,
                        mp.codescena,
                        mp.numviews,
                        mp.numinteractions,
                        mp.numlikes,
                        mp.numreposts,
                        mp.numsaves,
                        mp.numengagement
                    FROM h_metricapublicacion mp
                    INNER JOIN base
                      ON mp.codpublicacion = base.codpublicacion
                     AND (mp.fecreacionregistro + mp.horacreacionregistro) = base.max_fecreacionregistro
                    WHERE 1=1
                """);

            //
            // 2️⃣  Filtros dinámicos para el CTE “filtradas” (por métricas numéricas)
            //
            if (!request.getLikesMin().isEmpty() || !request.getLikesMax().isEmpty()) {
                sql.append(" AND mp.numlikes BETWEEN ? AND ?\n");
                parametros.add(request.getLikesMin().isEmpty() ? 0 : Integer.parseInt(request.getLikesMin()));
                parametros.add(request.getLikesMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getLikesMax()));
            }

            if (!request.getInteractionMin().isEmpty() || !request.getInteractionMax().isEmpty()) {
                sql.append(" AND mp.numinteractions BETWEEN ? AND ?\n");
                parametros.add(request.getInteractionMin().isEmpty() ? 0 : Integer.parseInt(request.getInteractionMin()));
                parametros.add(request.getInteractionMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getInteractionMax()));
            }

            if (!request.getSavesMin().isEmpty() || !request.getSavesMax().isEmpty()) {
                sql.append(" AND mp.numsaves BETWEEN ? AND ?\n");
                parametros.add(request.getSavesMin().isEmpty() ? 0 : Integer.parseInt(request.getSavesMin()));
                parametros.add(request.getSavesMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getSavesMax()));
            }

            if (!request.getViewsMin().isEmpty() || !request.getViewsMax().isEmpty()) {
                sql.append(" AND mp.numviews BETWEEN ? AND ?\n");
                parametros.add(request.getViewsMin().isEmpty() ? 0 : Integer.parseInt(request.getViewsMin()));
                parametros.add(request.getViewsMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getViewsMax()));
            }

            if (!request.getEngagementMin().isEmpty() || !request.getEngagementMax().isEmpty()) {
                sql.append(" AND mp.numengagement BETWEEN ? AND ?\n");
                parametros.add(request.getEngagementMin().isEmpty() ? 0.0 : Double.parseDouble(request.getEngagementMin()));
                parametros.add(request.getEngagementMax().isEmpty() ? 100.0 : Double.parseDouble(request.getEngagementMax()));
            }

            // Cierra el bloque del CTE “filtradas” y completa la consulta principal
            sql.append("""
                ),
                agrupado AS (
                    SELECT
                        codautora,
                        codlibro,
                        codescena,
                        AVG(numviews)           AS prom_numviews,
                        AVG(numinteractions)    AS prom_interacciones
                    FROM filtradas
                    GROUP BY codautora, codlibro, codescena
                ),
                maxmintotal AS (
                    SELECT
                        MIN(prom_numviews)          AS min_prom_numviews,
                        MAX(prom_numviews)          AS max_prom_numviews,
                        MIN(prom_interacciones)     AS min_prom_interacciones,
                        MAX(prom_interacciones)     AS max_prom_interacciones
                    FROM agrupado
                )
                SELECT
                    COALESCE(a.nbautora, '') || ' ' || COALESCE(a.apeautora, '') AS Author_name,
                    lb.deslibro                                 AS Book,
                    e.codescena                                 AS Scene_Code,
                    esc.desscena                                AS Scene,
                    ROUND(
                        0.5 * CASE 
                            WHEN (m.max_prom_numviews - m.min_prom_numviews) = 0 THEN 0
                            ELSE (e.prom_numviews - m.min_prom_numviews) / (m.max_prom_numviews - m.min_prom_numviews)
                        END
                        +
                        0.5 * CASE
                            WHEN (m.max_prom_interacciones - m.min_prom_interacciones) = 0 THEN 0
                            ELSE (e.prom_interacciones - m.min_prom_interacciones) / (m.max_prom_interacciones - m.min_prom_interacciones)
                        END
                    , 2) AS Score_Scene
                FROM agrupado e
                CROSS JOIN maxmintotal m
                LEFT JOIN m_autora a    ON e.codautora = a.codautora
                LEFT JOIN m_escenalibro esc ON e.codescena = esc.codescena
                LEFT JOIN m_libro lb    ON e.codlibro = lb.codlibro
            """);

            // Ejecuta la consulta y devuelve la lista de mapas (columnas → valores)
            return jdbc.queryForList(sql.toString(), parametros.toArray());
        }
        catch (Exception e) {
            logger.error("Error en ScoreSceneConnectionV2: ", e);
            return List.of();
        }
    }








    public List<Map<String,Object>> reporteConcisoConnectionV2(FiltrosRequestDB request) {
        try {
            String sql =  """ 
            SELECT 
                COALESCE(a.nbautora, 'Not found: N/A') || ' ' || COALESCE(a.apeautora, '') AS "Author name",
                COALESCE(lb.deslibro, 'Not found: N/A') AS "Book name",
                COALESCE(esc.desscena, 'Not found: N/A') AS "Scene name",
                COALESCE(tp.despost, 'Not found: N/A') AS "Post Type",
                mp.fecpublicacion AS "Date posted",
                mp.horapublicacion AS "Time posted",
                mp.nbrcuentatiktok AS "TikTok Username",
                mp.urlpublicacion AS "Post URL",
                mp.numviews AS "Views",
                mp.numlikes AS "Likes",
                mp.numcomments AS "Comments",
                mp.numreposts AS "Reposted",
                mp.numsaves AS "Saves",
                mp.numengagement AS "Engagement rate",
                mp.numinteractions AS "Interactions",
                mp.deshashtags AS "Hashtags",
                mp.nrohashtag AS "Number of Hashtags",
                mp.urlsounds AS "Sound URL"
            FROM h_metricapublicacion mp
            INNER JOIN (
                SELECT 
                    codpublicacion, 
                    MAX(mpint.fecreacionregistro+mpint.horacreacionregistro) AS max_fecreacionregistro
                FROM h_metricapublicacion mpint
                WHERE 1=1
            """;
        
        List<Object> parametros = new ArrayList<>();
        if (!request.getPubStartDate().isEmpty() && !request.getPubFinishtDate().isEmpty()) {
            sql += " AND mpint.fecpublicacion BETWEEN ? AND ?";
            parametros.add(java.sql.Date.valueOf(request.getPubStartDate()));
            parametros.add(java.sql.Date.valueOf(request.getPubFinishtDate()));
        }

        if (!request.getTrackStartDate().isEmpty() && !request.getTrackFinishtDate().isEmpty()) {
            sql += " AND mpint.fecreacionregistro BETWEEN ? AND ?";
            parametros.add(java.sql.Date.valueOf(request.getTrackStartDate()));
            parametros.add(java.sql.Date.valueOf(request.getTrackFinishtDate()));
        }


        if (!request.getAuthorList().isEmpty()) {
            sql += " AND mpint.codautora IN (" 
            + String.join(",", Collections.nCopies(request.getAuthorList().size(), "?"))
            + ")";
            parametros.addAll(request.getAuthorList());
        }

        if (!request.getBookList().isEmpty()) {
            sql += " AND mpint.codlibro IN (" 
            + String.join(",", Collections.nCopies(request.getBookList().size(), "?"))
            + ")";
            parametros.addAll(request.getBookList());
        }

        if (!request.getPAList().isEmpty()) {
            sql += " AND mpint.codposteador IN (" 
            + String.join(",", Collections.nCopies(request.getPAList().size(), "?"))
            + ")";
            parametros.addAll(request.getPAList());
        }

        if (!request.getSceneList().isEmpty()) {
            sql += " AND mpint.codescena IN (" 
            + String.join(",", Collections.nCopies(request.getSceneList().size(), "?"))
            + ")";
            parametros.addAll(request.getSceneList());
        }

        if (!request.getTypePostList().isEmpty()) {
            sql += " AND mpint.tippublicacion IN (" 
            + String.join(",", Collections.nCopies(request.getTypePostList().size(), "?"))
            + ")";
            parametros.addAll(request.getTypePostList());
        }

        if (!request.getAccountList().isEmpty()) {
            sql += " AND mpint.nbrcuentatiktok IN (" 
            + String.join(",", Collections.nCopies(request.getAccountList().size(), "?"))
            + ")";
            parametros.addAll(request.getAccountList());
        }

        if (!request.getPostIDList().isEmpty()) {
            sql += " AND mpint.codpublicacion IN (" 
            + String.join(",", Collections.nCopies(request.getPostIDList().size(), "?"))
            + ")";
            parametros.addAll(request.getPostIDList());
        }

        if (!request.getRegionList().isEmpty()) {
            sql += " AND mpint.codregionposteo IN (" 
            + String.join(",", Collections.nCopies(request.getRegionList().size(), "?"))
            + ")";
            parametros.addAll(request.getRegionList());
        }

        sql += """
                    GROUP BY mpint.codpublicacion
                ) base
                ON mp.codpublicacion = base.codpublicacion 
                AND (mp.fecreacionregistro+mp.horacreacionregistro) = base.max_fecreacionregistro
                LEFT JOIN m_autora a ON mp.codautora = a.codautora
                LEFT JOIN m_escenalibro esc ON mp.codescena = esc.codescena
                LEFT JOIN m_libro lb ON mp.codlibro = lb.codlibro
                LEFT JOIN m_tipopost tp ON mp.tippublicacion = tp.tippublicacion
                LEFT JOIN m_posteadorasistente po ON mp.codposteador = po.codposteador
                WHERE 1=1
            """;


            if (!request.getLikesMin().isEmpty() || !request.getLikesMax().isEmpty()) { 
                sql += " AND mp.numlikes BETWEEN ? AND ?";
                parametros.add(request.getLikesMin().isEmpty() ? 0 : Integer.parseInt(request.getLikesMin()));
                parametros.add(request.getLikesMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getLikesMax()));
            }
            
            
            if (!request.getInteractionMin().isEmpty() || !request.getInteractionMax().isEmpty()) {
                sql += " AND mp.numinteractions BETWEEN ? AND ?";
                parametros.add(request.getInteractionMin().isEmpty() ? 0: Integer.parseInt(request.getInteractionMin()));
                parametros.add(request.getInteractionMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getInteractionMax()));
            }

            if (!request.getSavesMin().isEmpty() || !request.getSavesMax().isEmpty()) {
                sql += " AND mp.numsaves BETWEEN ? AND ?";
                parametros.add(request.getSavesMin().isEmpty() ? 0: Integer.parseInt(request.getSavesMin()));
                parametros.add(request.getSavesMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getSavesMax()));
            }
            

            if (!request.getViewsMin().isEmpty() || !request.getViewsMax().isEmpty()) {
                sql += " AND mp.numviews BETWEEN ? AND ?";
                parametros.add(request.getViewsMin().isEmpty() ? 0: Integer.parseInt(request.getViewsMin()));
                parametros.add(request.getViewsMax().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(request.getViewsMax()));
            }

            
            if (!request.getEngagementMin().isEmpty() || !request.getEngagementMax().isEmpty()) {
                sql += " AND mp.numengagement BETWEEN ? AND ?";
                parametros.add(request.getEngagementMin().isEmpty() ? 0: Double.parseDouble(request.getEngagementMin()));
                parametros.add(request.getEngagementMax().isEmpty() ? 100 : Double.parseDouble(request.getEngagementMax()));
            }


        // Ejecutar la consulta y retornar los resultados
        //logger.info("🔍 Consulta SQL: " + sql);
        return jdbc.queryForList(sql.toString(),parametros.toArray());
    }
    catch (Exception e) {
        logger.error("Bad SQL grammar",e);
        return List.of();
    }
    }
    
}
