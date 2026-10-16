package com.arriendaya.app.controller;

import com.arriendaya.app.service.InmuebleService;
import com.arriendaya.app.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class PropietarioController {

    private final UsuarioService usuarioService;
    private final InmuebleService inmuebleService;

    @GetMapping("/propietarios/{id}")
    public String perfil(@PathVariable Long id, Model model) {
        var propietario = usuarioService.comoPropietario(id)
                .orElseThrow(() -> new IllegalArgumentException("Propietario no encontrado: " + id));
        model.addAttribute("propietario", propietario);
        model.addAttribute("inmuebles", inmuebleService.deMisPropiedades(id));
        return "perfil_propietario";
    }
}
