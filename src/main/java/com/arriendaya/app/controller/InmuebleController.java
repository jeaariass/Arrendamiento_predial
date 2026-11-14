package com.arriendaya.app.controller;

import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.TipoInmueble;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.service.InmuebleService;
import com.arriendaya.app.service.ReservaService;
import com.arriendaya.app.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @GetMapping("/nuevo")
    public String formularioNuevo(HttpSession session, Model model) {
        if (session.getAttribute("usuarioId") == null) {
            return "redirect:/login";
        }
        model.addAttribute("inmueble", new Inmueble());
        model.addAttribute("tipos", TipoInmueble.values());
        model.addAttribute("accion", "/inmuebles/nuevo");
        model.addAttribute("titulo", "Publicar inmueble");
        return "formulario_inmueble";
    }

    @PostMapping("/nuevo")
    public String crear(@RequestParam String titulo, @RequestParam(required = false) String descripcion,
                         @RequestParam TipoInmueble tipo, @RequestParam String direccion,
                         @RequestParam String ciudad, @RequestParam(defaultValue = "Colombia") String pais,
                         @RequestParam BigDecimal precioNoche, @RequestParam Integer capacidad,
                         @RequestParam(required = false) String imagenUrl,
                         HttpSession session) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            return "redirect:/login";
        }
        Usuario usuario = usuarioService.porId(usuarioId)
                .orElseThrow(() -> new IllegalStateException("Usuario invalido"));
        var propietario = usuarioService.convertirEnPropietario(usuario);
        session.setAttribute("esPropietario", true);

        Inmueble inmueble = new Inmueble();
        inmueble.setPropietario(propietario);
        inmueble.setUbicacion(inmuebleService.obtenerOCrearUbicacion(ciudad, pais));
        inmueble.setTitulo(titulo);
        inmueble.setDescripcion(descripcion);
        inmueble.setTipo(tipo);
        inmueble.setDireccion(direccion);
        inmueble.setPrecioNoche(precioNoche);
        inmueble.setCapacidad(capacidad);
        inmueble.setImagenUrl(imagenUrl);
        Inmueble guardado = inmuebleService.guardar(inmueble);
        return "redirect:/propietarios/" + usuarioId + "?mensaje=" + codificar("Inmueble \"" + guardado.getTitulo() + "\" publicado");
    }

    @GetMapping("/{id}/editar")
    public String formularioEditar(@PathVariable Long id, HttpSession session, Model model) {
        Inmueble inmueble = inmuebleService.obtener(id);
        if (!esDuenoDe(inmueble, session)) {
            return "redirect:/propietarios/" + inmueble.getPropietario().getId();
        }
        model.addAttribute("inmueble", inmueble);
        model.addAttribute("tipos", TipoInmueble.values());
        model.addAttribute("accion", "/inmuebles/" + id + "/editar");
        model.addAttribute("titulo", "Editar inmueble");
        return "formulario_inmueble";
    }

    @PostMapping("/{id}/editar")
    public String editar(@PathVariable Long id, @RequestParam String titulo,
                          @RequestParam(required = false) String descripcion, @RequestParam TipoInmueble tipo,
                          @RequestParam String direccion, @RequestParam String ciudad,
                          @RequestParam(defaultValue = "Colombia") String pais,
                          @RequestParam BigDecimal precioNoche, @RequestParam Integer capacidad,
                          @RequestParam(required = false) String imagenUrl,
                          HttpSession session) {
        Inmueble inmueble = inmuebleService.obtener(id);
        if (!esDuenoDe(inmueble, session)) {
            return "redirect:/propietarios/" + inmueble.getPropietario().getId();
        }
        inmueble.setUbicacion(inmuebleService.obtenerOCrearUbicacion(ciudad, pais));
        inmueble.setTitulo(titulo);
        inmueble.setDescripcion(descripcion);
        inmueble.setTipo(tipo);
        inmueble.setDireccion(direccion);
        inmueble.setPrecioNoche(precioNoche);
        inmueble.setCapacidad(capacidad);
        inmueble.setImagenUrl(imagenUrl);
        inmuebleService.guardar(inmueble);
        return "redirect:/propietarios/" + inmueble.getPropietario().getId() + "?mensaje=" + codificar("Inmueble actualizado");
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, HttpSession session) {
        Inmueble inmueble = inmuebleService.obtener(id);
        Long propietarioId = inmueble.getPropietario().getId();
        if (!esDuenoDe(inmueble, session)) {
            return "redirect:/propietarios/" + propietarioId;
        }
        if (reservaService.existeReservaDe(id)) {
            return "redirect:/propietarios/" + propietarioId + "?error=" + codificar("No se puede eliminar: tiene reservas asociadas");
        }
        inmuebleService.eliminar(id);
        return "redirect:/propietarios/" + propietarioId + "?mensaje=" + codificar("Inmueble eliminado");
    }

    private boolean esDuenoDe(Inmueble inmueble, HttpSession session) {
        return inmueble.getPropietario().getId().equals(session.getAttribute("usuarioId"));
    }

    private String codificar(String texto) {
        return java.net.URLEncoder.encode(texto, java.nio.charset.StandardCharsets.UTF_8);
    }

    @GetMapping("/{id}/reservar/preview")
    public String previsualizarReserva(@PathVariable Long id,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate llegada,
                                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
                                        HttpSession session,
                                        Model model) {
        var inmueble = inmuebleService.obtener(id);
        try {
            var valorTotal = reservaService.calcularValor(id, llegada, salida);
            model.addAttribute("inmueble", inmueble);
            model.addAttribute("llegada", llegada);
            model.addAttribute("salida", salida);
            model.addAttribute("noches", java.time.temporal.ChronoUnit.DAYS.between(llegada, salida));
            model.addAttribute("valorTotal", valorTotal);
            model.addAttribute("autenticado", session.getAttribute("usuarioId") != null);
            model.addAttribute("urlPreview", "/inmuebles/" + id + "/reservar/preview?llegada=" + llegada + "&salida=" + salida);
            return "reserva_confirmar";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("inmueble", inmueble);
            model.addAttribute("error", ex.getMessage());
            return "detalle";
        }
    }

    @PostMapping("/{id}/reservar")
    public String reservar(@PathVariable Long id,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate llegada,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate salida,
                            HttpSession session,
                            Model model) {
        Long usuarioId = (Long) session.getAttribute("usuarioId");
        if (usuarioId == null) {
            String destino = "/inmuebles/" + id + "/reservar/preview?llegada=" + llegada + "&salida=" + salida;
            return "redirect:/login?redirect=" + codificar(destino);
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
