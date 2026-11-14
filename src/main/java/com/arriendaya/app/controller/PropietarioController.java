package com.arriendaya.app.controller;

import com.arriendaya.app.dto.InmuebleConReserva;
import com.arriendaya.app.service.InmuebleService;
import com.arriendaya.app.service.ReservaService;
import com.arriendaya.app.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PropietarioController {

    private final UsuarioService usuarioService;
    private final InmuebleService inmuebleService;
    private final ReservaService reservaService;

    @GetMapping("/propietarios/{id}")
    public String perfil(@PathVariable Long id,
                          @RequestParam(required = false) String mensaje,
                          @RequestParam(required = false) String error,
                          HttpSession session, Model model) {
        var propietario = usuarioService.comoPropietario(id)
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado: " + id));

        List<InmuebleConReserva> inmuebles = inmuebleService.deMisPropiedades(id).stream()
                .map(i -> new InmuebleConReserva(i, reservaService.proximaReservaPorInmueble(i.getId())))
                .toList();

        boolean esPropio = id.equals(session.getAttribute("usuarioId"));
        model.addAttribute("propietario", propietario);
        model.addAttribute("inmuebles", inmuebles);
        model.addAttribute("esPropio", esPropio);
        model.addAttribute("mensaje", mensaje);
        model.addAttribute("error", error);
        if (esPropio) {
            model.addAttribute("historialReservas", reservaService.deInmueblesDePropietario(id));
        }
        return "perfil_propietario";
    }
}
