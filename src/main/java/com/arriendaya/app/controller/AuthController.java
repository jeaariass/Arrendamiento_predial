package com.arriendaya.app.controller;

import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/registro")
    public String formularioRegistro(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String nombre,
                             @RequestParam String apellido,
                             @RequestParam String email,
                             @RequestParam(required = false) String telefono,
                             @RequestParam String password,
                             @RequestParam(required = false, defaultValue = "false") boolean serPropietario,
                             @RequestParam(required = false, defaultValue = "false") boolean serCliente,
                             @RequestParam(required = false) String redirect,
                             Model model,
                             HttpSession session) {
        try {
            if (!serPropietario && !serCliente) {
                serCliente = true;
            }
            Usuario usuario = usuarioService.registrar(nombre, apellido, email, telefono, password,
                    serPropietario, serCliente);
            session.setAttribute("usuarioId", usuario.getId());
            session.setAttribute("usuarioNombre", usuario.getNombre());
            session.setAttribute("esAdmin", false);
            session.setAttribute("esPropietario", serPropietario);
            return "redirect:" + destinoSeguro(redirect);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("redirect", redirect);
            return "registro";
        }
    }

    @GetMapping("/login")
    public String formularioLogin(@RequestParam(required = false) String redirect, Model model) {
        model.addAttribute("redirect", redirect);
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                         @RequestParam(required = false) String redirect,
                         Model model, HttpSession session) {
        var usuarioOpt = usuarioService.porEmail(email);
        if (usuarioOpt.isEmpty() || !passwordEncoder.matches(password, usuarioOpt.get().getPasswordHash())) {
            model.addAttribute("error", "Correo o contrasena incorrectos");
            model.addAttribute("redirect", redirect);
            return "login";
        }
        Usuario usuario = usuarioOpt.get();
        session.setAttribute("usuarioId", usuario.getId());
        session.setAttribute("usuarioNombre", usuario.getNombre());
        session.setAttribute("esAdmin", usuario.isEsAdmin());
        session.setAttribute("esPropietario", usuarioService.comoPropietario(usuario.getId()).isPresent());
        return "redirect:" + destinoSeguro(redirect);
    }

    /** Evita open-redirect: solo rutas relativas propias ("/algo"), nunca "//host" ni URLs con esquema. */
    private String destinoSeguro(String redirect) {
        if (redirect != null && redirect.startsWith("/") && !redirect.startsWith("//")) {
            return redirect;
        }
        return "/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
