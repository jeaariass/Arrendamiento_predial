package com.arriendaya.app.dto;

import com.arriendaya.app.model.Usuario;

/** Un usuario encontrado en la busqueda del admin, con que roles tiene. */
public record PersonaResumen(Usuario usuario, boolean esCliente, boolean esPropietario) {
}
