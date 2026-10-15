package com.ctglobal.arriendo.controller;

import com.ctglobal.arriendo.model.Cliente;
import com.ctglobal.arriendo.service.InmuebleService;
import com.ctglobal.arriendo.service.ReservaService;
import com.ctglobal.arriendo.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/inmuebles")
@RequiredArgsConstructor
public class InmuebleController {

    private final InmuebleService inmuebleService;
    private final ReservaService reservaService;
    private final UsuarioService usuarioService;

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("inmueble", inmuebleService.obtener(id));
        return "detalle";
    }

    @PostMapping("/{id}/reservar")
    public String reservar(@PathVariable Long id,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate llegada,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
                            HttpSession session,
                            Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }
        var usuarioOpt = usuarioService.comoCliente(usuarioId);
        Cliente cliente;
        if (usuarioOpt.isPresent()) {
            cliente = usuarioOpt.get();
        } else {
            var u = usuarioService.comoPropietario(usuarioId)
                    .map(p -> p.getUsuario())
                    .orElseThrow(() -> new IllegalStateException("Usuario invalido"));
            cliente = usuarioService.convertirEnCliente(u);
        }

        try {
            var reserva = reservaService.reservar(cliente, id, llegada, salida);
            model.addAttribute("reserva", reserva);
            return "reserva_confirmada";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("inmueble", inmuebleService.obtener(id));
            model.addAttribute("error", ex.getMessage());
            return "detalle";
        }
    }
}
