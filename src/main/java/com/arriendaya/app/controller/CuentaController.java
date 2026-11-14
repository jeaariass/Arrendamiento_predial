package com.arriendaya.app.controller;

import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Pago;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.BancoRepository;
import com.arriendaya.app.repository.ClienteRepository;
import com.arriendaya.app.repository.PagoRepository;
import com.arriendaya.app.service.PagoService;
import com.arriendaya.app.service.ReservaService;
import com.arriendaya.app.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Cuenta del cliente: informacion bancaria propia (no la del propietario del
 * inmueble) y cuanto debe ahora por reservas pendientes/confirmadas. Cualquier
 * usuario logueado puede entrar aqui: si todavia no tiene rol de Cliente, se
 * le crea en el momento (igual que ya pasaba al reservar desde el detalle).
 */
@Controller
@RequestMapping("/mi-cuenta")
@RequiredArgsConstructor
public class CuentaController {

    private final UsuarioService usuarioService;
    private final ClienteRepository clienteRepository;
    private final ReservaService reservaService;
    private final PagoService pagoService;
    private final BancoRepository bancoRepository;
    private final PagoRepository pagoRepository;

    @GetMapping
    public String verCuenta(HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        cargarModelo(cliente, model);
        return "mi_cuenta";
    }

    @GetMapping("/ajustes")
    public String verAjustes(HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", cliente.getUsuario());
        model.addAttribute("cliente", cliente);
        return "mi_cuenta_ajustes";
    }

    @PostMapping("/ajustes")
    public String actualizarPerfil(@RequestParam String nombre, @RequestParam String apellido,
                                    @RequestParam(required = false) String telefono,
                                    HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        Usuario usuario = usuarioService.actualizarPerfil(cliente.getUsuario(), nombre, apellido, telefono);
        session.setAttribute("usuarioNombre", usuario.getNombre());
        model.addAttribute("usuario", usuario);
        model.addAttribute("cliente", cliente);
        model.addAttribute("mensaje", "Datos actualizados");
        return "mi_cuenta_ajustes";
    }

    @PostMapping("/ajustes/clave")
    public String cambiarClave(@RequestParam String actual, @RequestParam String nueva,
                                @RequestParam String confirmar, HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", cliente.getUsuario());
        model.addAttribute("cliente", cliente);
        if (!nueva.equals(confirmar)) {
            model.addAttribute("error", "La nueva contrasena no coincide con la confirmacion");
            return "mi_cuenta_ajustes";
        }
        try {
            usuarioService.cambiarPassword(cliente.getUsuario(), actual, nueva);
            model.addAttribute("mensaje", "Contrasena actualizada");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "mi_cuenta_ajustes";
    }

    @PostMapping("/ajustes/banco")
    public String actualizarBanco(@RequestParam(required = false) String cuentaBancaria, HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        cliente.setCuentaBancaria(cuentaBancaria);
        clienteRepository.save(cliente);
        model.addAttribute("usuario", cliente.getUsuario());
        model.addAttribute("cliente", cliente);
        model.addAttribute("mensaje", "Informacion bancaria actualizada");
        return "mi_cuenta_ajustes";
    }

    @PostMapping("/reservas/{reservaId}/cancelar")
    public String cancelarReserva(@PathVariable Long reservaId, HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        boolean esSuya = reservaService.deCliente(cliente.getId()).stream()
                .anyMatch(r -> r.getId().equals(reservaId));
        if (esSuya) {
            reservaService.cambiarEstado(reservaId, EstadoReserva.CANCELADA);
        }
        cargarModelo(cliente, model);
        model.addAttribute("mensaje", esSuya ? "Reserva cancelada, ya no debes ese dinero" : "Esa reserva no es tuya");
        return "mi_cuenta";
    }

    @PostMapping("/reservas/{reservaId}/pagar")
    public String pagarReserva(@PathVariable Long reservaId,
                                @RequestParam Long bancoId,
                                @RequestParam String metodoPago,
                                @RequestParam(required = false) String referencia,
                                HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        cargarModelo(cliente, model);
        try {
            pagoService.pagar(cliente, reservaId, bancoId, metodoPago, referencia);
            cargarModelo(cliente, model);
            model.addAttribute("mensaje", "Pago registrado, reserva confirmada");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
        return "mi_cuenta";
    }

    private Cliente clienteDeLaSesion(HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return null;
        }
        return usuarioService.comoCliente(usuarioId).orElseGet(() -> {
            Usuario usuario = usuarioService.porId(usuarioId)
                    .orElseThrow(() -> new IllegalStateException("Usuario invalido"));
            return usuarioService.convertirEnCliente(usuario);
        });
    }

    private void cargarModelo(Cliente cliente, Model model) {
        var reservas = reservaService.deCliente(cliente.getId());
        Map<Long, Pago> pagoPorReserva = new HashMap<>();
        for (var reserva : reservas) {
            pagoRepository.findByReservaId(reserva.getId()).ifPresent(p -> pagoPorReserva.put(reserva.getId(), p));
        }
        model.addAttribute("cliente", cliente);
        model.addAttribute("reservas", reservas);
        model.addAttribute("deuda", reservaService.deudaPendiente(reservas));
        model.addAttribute("bancos", bancoRepository.findAll());
        model.addAttribute("pagoPorReserva", pagoPorReserva);
    }
}
