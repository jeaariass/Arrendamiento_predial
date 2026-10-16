package com.ctglobal.arriendo.config;

import com.ctglobal.arriendo.model.*;
import com.ctglobal.arriendo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga unos pocos datos de ejemplo (bancos, ciudades, propietarios e
 * inmuebles) para que la pagina principal se vea con contenido real la
 * primera vez que se levanta la aplicacion. Solo se ejecuta si la tabla
 * de inmuebles esta vacia.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UbicacionRepository ubicacionRepository;
    private final BancoRepository bancoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final InmuebleRepository inmuebleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (inmuebleRepository.count() > 0) {
            return;
        }

        Ubicacion bogota = ubicacionRepository.save(ubicacion("Bogota", "Colombia"));
        Ubicacion medellin = ubicacionRepository.save(ubicacion("Medellin", "Colombia"));
        Ubicacion cartagena = ubicacionRepository.save(ubicacion("Cartagena", "Colombia"));
        Ubicacion santaMarta = ubicacionRepository.save(ubicacion("Santa Marta", "Colombia"));

        bancoRepository.save(banco("Bancolombia", "BCOL"));
        bancoRepository.save(banco("Davivienda", "DAVI"));
        bancoRepository.save(banco("BBVA Colombia", "BBVA"));

        Propietario propietario = crearPropietarioDemo();

        inmuebleRepository.save(inmueble(propietario, bogota, "Apartamento moderno en Chapinero",
                TipoInmueble.APARTAMENTO, "Calle 60 # 9-30", new BigDecimal("180000"), 4,
                "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800"));

        inmuebleRepository.save(inmueble(propietario, medellin, "Loft con vista a la ciudad",
                TipoInmueble.LOFT, "Carrera 35 # 8-15, El Poblado", new BigDecimal("150000"), 2,
                "https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800"));

        inmuebleRepository.save(inmueble(propietario, cartagena, "Casa colonial en el centro historico",
                TipoInmueble.CASA, "Calle del Arsenal # 4-22", new BigDecimal("320000"), 6,
                "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800"));

        inmuebleRepository.save(inmueble(propietario, santaMarta, "Cabana frente al mar",
                TipoInmueble.CABANA, "Via a Taganga km 2", new BigDecimal("220000"), 5,
                "https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?w=800"));

        inmuebleRepository.save(inmueble(propietario, bogota, "Habitacion privada cerca a la 93",
                TipoInmueble.HABITACION, "Calle 93 # 13-20", new BigDecimal("90000"), 1,
                "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=800"));

        inmuebleRepository.save(inmueble(propietario, medellin, "Finca campestre en el oriente antioqueno",
                TipoInmueble.FINCA, "Vereda La Union, Rionegro", new BigDecimal("450000"), 10,
                "https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=800"));
    }

    private Propietario crearPropietarioDemo() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Carlos");
        usuario.setApellido("Ramirez");
        usuario.setEmail("propietario.demo@arriendo.co");
        usuario.setTelefono("3001234567");
        usuario.setPasswordHash(passwordEncoder.encode("demo1234"));
        usuario = usuarioRepository.save(usuario);
        return propietarioRepository.save(new Propietario(usuario));
    }

    private Ubicacion ubicacion(String ciudad, String pais) {
        Ubicacion u = new Ubicacion();
        u.setCiudad(ciudad);
        u.setPais(pais);
        return u;
    }

    private Banco banco(String nombre, String codigo) {
        Banco b = new Banco();
        b.setNombre(nombre);
        b.setCodigo(codigo);
        return b;
    }

    private Inmueble inmueble(Propietario propietario, Ubicacion ubicacion, String titulo,
                               TipoInmueble tipo, String direccion, BigDecimal precioNoche,
                               Integer capacidad, String imagenUrl) {
        Inmueble i = new Inmueble();
        i.setPropietario(propietario);
        i.setUbicacion(ubicacion);
        i.setTitulo(titulo);
        i.setDescripcion("Espacio comodo y bien ubicado, ideal para estadias cortas.");
        i.setTipo(tipo);
        i.setDireccion(direccion);
        i.setPrecioNoche(precioNoche);
        i.setCapacidad(capacidad);
        i.setImagenUrl(imagenUrl);
        return i;
    }
}
