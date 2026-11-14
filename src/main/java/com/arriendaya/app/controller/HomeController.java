package com.arriendaya.app.controller;

import com.arriendaya.app.dto.PuntoMapa;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.TipoInmueble;
import com.arriendaya.app.service.InmuebleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final InmuebleService inmuebleService;

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("destacados", inmuebleService.destacados());
        model.addAttribute("ciudadesTop", inmuebleService.ciudadesTop());
        return "index";
    }

    @GetMapping("/ciudades/sugerencias")
    @ResponseBody
    public List<String> sugerenciasCiudades(@RequestParam(required = false, defaultValue = "") String q) {
        return inmuebleService.sugerirCiudades(q);
    }

    @GetMapping("/mapa/puntos")
    @ResponseBody
    public List<PuntoMapa> puntosEnMapa(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLon,
            @RequestParam double maxLon,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate llegada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida) {
        return inmuebleService.puntosEnMapa(minLat, maxLat, minLon, maxLon, ciudad, llegada, salida);
    }

    @GetMapping("/buscar")
    public String buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Integer huespedes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate llegada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
            @RequestParam(required = false) TipoInmueble tipo,
            @RequestParam(required = false) String orden,
            Model model) {

        List<Inmueble> resultados = inmuebleService.buscar(ciudad, huespedes, llegada, salida, tipo, orden);

        model.addAttribute("resultados", resultados);
        model.addAttribute("ciudadesTop", inmuebleService.ciudadesTop());
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("huespedes", huespedes);
        model.addAttribute("llegada", llegada);
        model.addAttribute("salida", salida);
        model.addAttribute("tipo", tipo);
        model.addAttribute("tipos", TipoInmueble.values());
        model.addAttribute("orden", orden);
        return "resultados";
    }
}
