/*package com.example.sbazureappdemo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sbazureappdemo.APICALL.JsonProcessor;

import org.springframework.web.bind.annotation.CrossOrigin;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

// Controlador REST que maneja solicitudes relacionadas con la generación y descarga de archivos Excel
@RestController
@CrossOrigin(origins = "*")
//@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})

// Define la ruta base "/api/excel" para todos los endpoints de este controlador
@RequestMapping("/api/excel")
public class copiaExcelController {
    // Dependencia de JsonProcessor, que se encarga de procesar y almacenar los datos
    // jsonProcessor Instancia de JsonProcessor inyectada automáticamente por Spring.
    @Autowired
    private final JsonProcessor jsonProcessor;
    public copiaExcelController(JsonProcessor jsonProcessor) {
        this.jsonProcessor = jsonProcessor;
    }
    
    // Endpoint que permite descargar un archivo Excel con las métricas de TikTok
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExcel() {
        // Obtiene los datos procesados por JsonProcessor
        List<Map<String, Object>> data = jsonProcessor.getLastProcessedData();
        if (data == null || data.isEmpty()) {
            return ResponseEntity.badRequest().body("⚠️ No hay datos para exportar a Excel.".getBytes());
        }

        // Usa Apache POI para crear y llenar un archivo Excel
        try (XSSFWorkbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Crear una hoja llamada "Tiktok Métricas"
            XSSFSheet sheet = workbook.createSheet("Tiktok Métricas");
            // Obtiene los nombres de las columnas a partir del primer elemento
            List<String> columns = new ArrayList<>(data.get(0).keySet());

                // === Estilo de cabecera ===
            CellStyle cabeceraStyle = workbook.createCellStyle();
            cabeceraStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            cabeceraStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Font cabeceraFont = workbook.createFont();
            cabeceraFont.setBold(true);
            cabeceraStyle.setFont(cabeceraFont);

            cabeceraStyle.setAlignment(HorizontalAlignment.CENTER);
            cabeceraStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            cabeceraStyle.setWrapText(true);

            // Borde grueso en cabeceras
            cabeceraStyle.setBorderTop(BorderStyle.THICK);
            cabeceraStyle.setBorderBottom(BorderStyle.THICK);
            cabeceraStyle.setBorderLeft(BorderStyle.THICK);
            cabeceraStyle.setBorderRight(BorderStyle.THICK);

            // === Estilo para celdas de contenido ===
            CellStyle contenidoStyle = workbook.createCellStyle();
            contenidoStyle.setAlignment(HorizontalAlignment.CENTER);
            contenidoStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            contenidoStyle.setWrapText(true);

            // === Estilo para celdas de fecha ===
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setAlignment(HorizontalAlignment.CENTER);
            dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            // Formato de fecha "dd/MM/yyyy"
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));

            // Agregar cabeceras
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.size(); col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columns.get(col)); //Establece el nombre de la columna
                cell.setCellStyle(cabeceraStyle);  // Aplica estilo a la cabecera
            }

            // Añadir los datos al Excel
            int rowIndex = 1; // La fila 0 es para cabeceras
            for (Map<String, Object> rowData : data) {
                Row row = sheet.createRow(rowIndex++);
                for (int col = 0; col < columns.size(); col++) {
                    Cell cell = row.createCell(col);
                    String columnName = columns.get(col);
                    Object value = rowData.get(columns.get(col));
                    if (value != null) {
                        // Si la columna es "Date posted" o "Tracking date"
                        if ("Date posted".equalsIgnoreCase(columnName) || "Tracking date".equalsIgnoreCase(columnName)) {
                            // Intentar parsear la fecha (dd/MM/yyyy)
                            try {
                                Date parsedDate = new SimpleDateFormat("dd/MM/yyyy").parse(value.toString());
                                cell.setCellValue(parsedDate);
                                cell.setCellStyle(dateStyle); // Celda tipo fecha
                            } catch (Exception e) {
                                // Si falla el parseo, lo ponemos como texto normal
                                cell.setCellValue(value.toString());
                                cell.setCellStyle(contenidoStyle);
                            }
                        } else {
                            // De lo contrario, lo tratamos como texto normal
                            cell.setCellValue(value.toString());
                            cell.setCellStyle(contenidoStyle);
                        }
                    } else {
                        // Valor nulo
                        cell.setCellValue("");
                        cell.setCellStyle(contenidoStyle);
                    }
                }
            }
            
            // Ajustar automáticamente el ancho de las columnas
            final int MAX_COLUMN_WIDTH = 65280; 
            for (int col = 0; col < columns.size(); col++) {
                sheet.autoSizeColumn(col);

                // Obtenemos el ancho calculado
                int currentWidth = sheet.getColumnWidth(col);
                // Sumamos un poco para que se vea más espacioso
                int extraWidth = 2000;  // Ajusta según tu preferencia
                int newWidth = currentWidth + extraWidth;
                // Evitamos superar el límite máximo
                if (newWidth > MAX_COLUMN_WIDTH) {
                    newWidth = MAX_COLUMN_WIDTH;
                }
                // Establecemos el nuevo ancho
                sheet.setColumnWidth(col, newWidth);
            }

            // Ajustar el alto de las filas (por defecto, desde la 1 hasta la última)
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    // Ajusta la altura a 50 puntos (puedes cambiarlo)
                    row.setHeightInPoints(45);
                }
            }

            // Obtener timestamp para generar nombre único del archivo
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = "backup_tiktok_videos_" + timestamp + ".xlsx";

            // Configurar los encabezados HTTP para la descarga
            workbook.write(outputStream);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", fileName);
            System.out.println("Excel generado: " + fileName);
            return ResponseEntity.ok().headers(headers).body(outputStream.toByteArray());

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("❌ Error al generar el archivo Excel.".getBytes());
        }
    }
}
*/