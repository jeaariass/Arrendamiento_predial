package com.ctglobal.arriendo.controller;

import com.ctglobal.arriendo.model.Inmueble;
import com.ctglobal.arriendo.service.InmuebleService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final InmuebleService inmuebleService;

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("destacados", inmuebleService.destacados());
        return "index";
    }

    @GetMapping("/buscar")
    public String buscar(
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) Integer huespedes,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate llegada,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
            Model model) {

        List<Inmueble> resultados = inmuebleService.buscar(ciudad, huespedes, llegada, salida);

        model.addAttribute("resultados", resultados);
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("huespedes", huespedes);
        model.addAttribute("llegada", llegada);
        model.addAttribute("salida", salida);
        return "resultados";
    }
}
