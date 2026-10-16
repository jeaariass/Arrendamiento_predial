package com.arriendaya.app.controller;

import com.arriendaya.app.dto.EstadoOcupacion;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.service.AdminService;
import com.arriendaya.app.service.InmuebleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final InmuebleService inmuebleService;

    @GetMapping("/admin")
    public String panel(@RequestParam(required = false) String ciudad,
                         @RequestParam(defaultValue = "0") int pagina,
                         HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("esAdmin"))) {
            return "redirect:/login";
        }

        Page<Inmueble> paginaInmuebles = adminService.listarPagina(ciudad, pagina);
        List<EstadoOcupacion> filas = paginaInmuebles.getContent().stream()
                .map(adminService::calcularOcupacion)
                .toList();

        model.addAttribute("filas", filas);
        model.addAttribute("ciudadesTop", inmuebleService.ciudadesTop());
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("pagina", pagina);
        model.addAttribute("totalPaginas", paginaInmuebles.getTotalPages());
        model.addAttribute("totalInmuebles", paginaInmuebles.getTotalElements());
        return "admin/panel";
    }
}
