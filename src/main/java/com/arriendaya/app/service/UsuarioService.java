package com.arriendaya.app.service;

import com.arriendaya.app.model.Cliente;
import com.arriendaya.app.model.Propietario;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.ClienteRepository;
import com.arriendaya.app.repository.PropietarioRepository;
import com.arriendaya.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Un Usuario es la persona (objeto real). Puede, ademas, "ser" Propietario
 * y/o Cliente al mismo tiempo (especializacion solapada), representado como
 * un registro 1 a 1 opcional en cada tabla, con la misma llave primaria.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario registrar(String nombre, String apellido, String email, String telefono,
                              String password, boolean serPropietario, boolean serCliente) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ya existe una cuenta con ese correo");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setApellido(apellido);
        usuario.setEmail(email);
        usuario.setTelefono(telefono);
        usuario.setPasswordHash(passwordEncoder.encode(password));
        usuario = usuarioRepository.save(usuario);

        if (serPropietario) {
            propietarioRepository.save(new Propietario(usuario));
        }
        if (serCliente) {
            clienteRepository.save(new Cliente(usuario));
        }
        return usuario;
    }

    public Optional<Usuario> porEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> porId(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Propietario> comoPropietario(Long usuarioId) {
        return propietarioRepository.findById(usuarioId);
    }

    public Optional<Cliente> comoCliente(Long usuarioId) {
        return clienteRepository.findById(usuarioId);
    }

    @Transactional
    public Propietario convertirEnPropietario(Usuario usuario) {
        return propietarioRepository.findById(usuario.getId())
                .orElseGet(() -> propietarioRepository.save(new Propietario(usuario)));
    }

    @Transactional
    public Cliente convertirEnCliente(Usuario usuario) {
        return clienteRepository.findById(usuario.getId())
                .orElseGet(() -> clienteRepository.save(new Cliente(usuario)));
    }
}
