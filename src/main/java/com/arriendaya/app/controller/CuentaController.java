package com.arriendaya.app.controller;

import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.EstadoReserva;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.ClienteRepository;
import com.arriendaya.app.service.ReservaService;
import com.arriendaya.app.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public String verCuenta(HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        cargarModelo(cliente, model);
        return "mi_cuenta";
    }

    @PostMapping("/banco")
    public String actualizarBanco(@RequestParam(required = false) String cuentaBancaria, HttpSession session, Model model) {
        Cliente cliente = clienteDeLaSesion(session);
        if (cliente == null) {
            return "redirect:/login";
        }
        cliente.setCuentaBancaria(cuentaBancaria);
        clienteRepository.save(cliente);
        cargarModelo(cliente, model);
        model.addAttribute("mensaje", "Informacion bancaria actualizada");
        return "mi_cuenta";
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
        model.addAttribute("cliente", cliente);
        model.addAttribute("reservas", reservas);
        model.addAttribute("deuda", reservaService.deudaPendiente(reservas));
    }
}
