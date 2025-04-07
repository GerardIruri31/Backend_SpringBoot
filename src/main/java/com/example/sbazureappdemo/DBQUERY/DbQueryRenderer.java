package com.example.sbazureappdemo.DBQUERY;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.sbazureappdemo.service.TiktokMetricasService;


@Component
public class DbQueryRenderer {
    Logger logger = LoggerFactory.getLogger(DbQueryRenderer.class);

    private final TiktokMetricasService tiktokMetricasService; 
    private List<Map<String, Object>> lastProcessedData = new ArrayList<>();
    @Autowired
    public DbQueryRenderer(TiktokMetricasService tiktokMetricasService) {
        this.tiktokMetricasService = tiktokMetricasService;
    }
    public List<Map<String,Object>> filtrarDatos (FiltrosRequestDB request) {  
        lastProcessedData.clear(); // Evita acumulación de datos de ejecuciones anteriores  
        lastProcessedData = tiktokMetricasService.filterRegistries(request);  
        return lastProcessedData;
    }

    // para usarlo en procesar excel
    public List<Map<String, Object>> getLastProcessedData() {
        if (lastProcessedData.isEmpty()) {
            logger.info("No hay datos procesados aún.");
        }
        return lastProcessedData;
    }
}
