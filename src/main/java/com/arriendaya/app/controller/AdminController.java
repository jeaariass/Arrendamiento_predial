package com.arriendaya.app.controller;

import com.arriendaya.app.dto.EstadoOcupacion;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.service.AdminService;
import com.arriendaya.app.service.InmuebleService;
import com.arriendaya.app.service.ReorganizarReservasService;
import com.arriendaya.app.service.ReservaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final InmuebleService inmuebleService;
    private final ReservaService reservaService;
    private final ReorganizarReservasService reorganizarReservasService;

    @GetMapping
    public String panel(@RequestParam(required = false) String ciudad,
                         @RequestParam(required = false) String estado,
                         @RequestParam(defaultValue = "0") int pagina,
                         @RequestParam(required = false) String mensaje,
                         @RequestParam(required = false) String error,
                         HttpSession session, Model model) {
        if (!Boolean.TRUE.equals(session.getAttribute("esAdmin"))) {
            return "redirect:/login";
        }

        Page<Inmueble> paginaInmuebles = adminService.listarPagina(ciudad, estado, pagina);
        List<EstadoOcupacion> filas = paginaInmuebles.getContent().stream()
                .map(adminService::calcularOcupacion)
                .toList();

        model.addAttribute("filas", filas);
        model.addAttribute("ciudadesTop", inmuebleService.ciudadesTop());
        model.addAttribute("ciudad", ciudad);
        model.addAttribute("estado", estado);
        model.addAttribute("pagina", pagina);
        model.addAttribute("totalPaginas", paginaInmuebles.getTotalPages());
        model.addAttribute("totalInmuebles", paginaInmuebles.getTotalElements());
        model.addAttribute("arrendadores", adminService.listaArrendadores());
        model.addAttribute("mensaje", mensaje);
        model.addAttribute("error", error);
        return "admin/panel";
    }

    @PostMapping("/reorganizar")
    public String reorganizar(HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("esAdmin"))) {
            return "redirect:/login";
        }
        var resultado = reorganizarReservasService.reorganizar();
        String mensaje = "Se crearon " + resultado.reservasCreadas() + " reservas nuevas en "
                + resultado.inmueblesConReserva() + " inmuebles (horizonte -90/+90 dias).";
        return "redirect:/admin?mensaje=" + java.net.URLEncoder.encode(mensaje, java.nio.charset.StandardCharsets.UTF_8);
    }

    @PostMapping("/asignar")
    public String asignar(@RequestParam Long inmuebleId,
                           @RequestParam String emailCliente,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                           @RequestParam BigDecimal valorTotal,
                           HttpSession session) {
        if (!Boolean.TRUE.equals(session.getAttribute("esAdmin"))) {
            return "redirect:/login";
        }
        try {
            reservaService.asignarPorAdmin(emailCliente, inmuebleId, fechaInicio, fechaFin, valorTotal);
            return "redirect:/admin?mensaje=" + java.net.URLEncoder.encode(
                    "Inmueble asignado a " + emailCliente, java.nio.charset.StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return "redirect:/admin?error=" + java.net.URLEncoder.encode(ex.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
