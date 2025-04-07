package com.example.sbazureappdemo.AUTHORGRAPHS;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.example.sbazureappdemo.service.TiktokMetricasService;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/authorsgraphs")
@CrossOrigin(origins = "*")
public class AuthorGraphsController {
    Logger logger = LoggerFactory.getLogger(AuthorGraphsController.class);
    private final TiktokMetricasService tiktokMetricasService;
    @Autowired
    public AuthorGraphsController(TiktokMetricasService tiktokMetricasService){
        this.tiktokMetricasService = tiktokMetricasService;
    }

    @PostMapping("/getdata")
    public CompletableFuture<List<List<Map<String,Object>>>> RecibirFiltros(@RequestBody AuthorGraphsRenderer filtros) {
        try {
            logger.info("📥 Filtros recibidos en el backend: " + filtros.getStartDate() + ", " + filtros.getFinishDate() + ", " + filtros.getAuthorList());
            return tiktokMetricasService.GetDatosAuthorGraphs(filtros);
            }
        catch (Exception e) {
            logger.error("Error al filtrar datos",e);
            return CompletableFuture.completedFuture(List.of());
        }
    }
}