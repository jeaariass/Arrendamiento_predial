package com.arriendaya.app.controller;

import com.arriendaya.app.dto.InmuebleConReserva;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Reserva;
import com.arriendaya.app.repository.ReservaRepository;
import com.arriendaya.app.service.InmuebleService;
import com.arriendaya.app.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class PropietarioController {

    private final UsuarioService usuarioService;
    private final InmuebleService inmuebleService;
    private final ReservaRepository reservaRepository;

    @GetMapping("/propietarios/{id}")
    public String perfil(@PathVariable Long id, Model model) {
        var propietario = usuarioService.comoPropietario(id)
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado: " + id));

        LocalDate hoy = LocalDate.now();
        List<InmuebleConReserva> inmuebles = inmuebleService.deMisPropiedades(id).stream()
                .map(i -> new InmuebleConReserva(i, reservaMasCercana(i, hoy)))
                .toList();

        model.addAttribute("propietario", propietario);
        model.addAttribute("inmuebles", inmuebles);
        return "perfil_propietario";
    }

    private Reserva reservaMasCercana(Inmueble inmueble, LocalDate hoy) {
        List<Reserva> futuras = reservaRepository.futurasPorInmueble(inmueble.getId(), hoy);
        return futuras.isEmpty() ? null : futuras.get(0);
    }
}
