package com.arriendaya.app.config;

import com.arriendaya.app.model.*;
import com.arriendaya.app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Carga alojamientos reales de Bogota tomados de Inside Airbnb
 * (https://insideairbnb.com, dataset abierto para uso academico/investigacion)
 * para que la pagina tenga datos reales ademas de los de ejemplo. El CSV ya
 * viene filtrado y limpio en src/main/resources/data/bogota_airbnb.csv.
 * Solo corre una vez: se detecta con un Usuario marcador.
 */
@Component
@Order(2)
@RequiredArgsConstructor
public class ImportadorAirbnbSeeder implements CommandLineRunner {

    private static final String EMAIL_MARCADOR = "importacion.airbnb@arriendaya.local";

    private final UsuarioRepository usuarioRepository;
    private final PropietarioRepository propietarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final InmuebleRepository inmuebleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (usuarioRepository.existsByEmail(EMAIL_MARCADOR)) {
            return;
        }

        Ubicacion bogota = ubicacionRepository.findByCiudadIgnoreCaseAndPaisIgnoreCase("Bogota", "Colombia")
                .orElseGet(() -> {
                    Ubicacion u = new Ubicacion();
                    u.setCiudad("Bogota");
                    u.setPais("Colombia");
                    return ubicacionRepository.save(u);
                });

        Map<String, Propietario> propietariosPorHost = new HashMap<>();
        int importados = 0;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new ClassPathResource("data/bogota_airbnb.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String linea = br.readLine(); // encabezado
            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) {
                    continue;
                }
                String[] campos = linea.split("\\|", -1);
                if (campos.length < 10) {
                    continue;
                }
                String hostId = campos[0];
                String hostName = campos[1];
                String nombreInmueble = campos[2];
                String roomType = campos[3];
                String barrio = campos[4];
                BigDecimal precio = new BigDecimal(campos[5]);
                Integer capacidad = Integer.valueOf(campos[6]);
                String imagenUrl = campos[7];
                Double latitud = Double.valueOf(campos[8]);
                Double longitud = Double.valueOf(campos[9]);

                Propietario propietario = propietariosPorHost.computeIfAbsent(hostId,
                        id -> crearPropietarioHost(id, hostName));

                Inmueble inmueble = new Inmueble();
                inmueble.setPropietario(propietario);
                inmueble.setUbicacion(bogota);
                inmueble.setTitulo(nombreInmueble);
                inmueble.setDescripcion("Alojamiento real tomado de Inside Airbnb (dataset abierto de Bogota, uso academico).");
                inmueble.setTipo(mapearTipo(roomType));
                inmueble.setDireccion(barrio + ", Bogota");
                inmueble.setPrecioNoche(precio);
                inmueble.setCapacidad(capacidad);
                inmueble.setImagenUrl(imagenUrl);
                inmueble.setLatitud(latitud);
                inmueble.setLongitud(longitud);
                inmuebleRepository.save(inmueble);
                importados++;
            }
        }

        Usuario marcador = new Usuario();
        marcador.setNombre("Importacion");
        marcador.setApellido("Inside Airbnb");
        marcador.setEmail(EMAIL_MARCADOR);
        marcador.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        usuarioRepository.save(marcador);

        org.slf4j.LoggerFactory.getLogger(ImportadorAirbnbSeeder.class)
                .info("Importados {} alojamientos reales de Bogota desde Inside Airbnb", importados);
    }

    private Propietario crearPropietarioHost(String hostId, String hostName) {
        String email = "airbnb-host-" + hostId + "@arriendaya-import.co";
        return usuarioRepository.findByEmail(email)
                .flatMap(u -> propietarioRepository.findById(u.getId()))
                .orElseGet(() -> {
                    Usuario usuario = new Usuario();
                    String[] partes = hostName.trim().split("\\s+", 2);
                    usuario.setNombre(partes[0]);
                    usuario.setApellido(partes.length > 1 ? partes[1] : "(anfitrion Airbnb)");
                    usuario.setEmail(email);
                    usuario.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    usuario = usuarioRepository.save(usuario);
                    return propietarioRepository.save(new Propietario(usuario));
                });
    }

    private TipoInmueble mapearTipo(String roomType) {
        if (roomType == null) {
            return TipoInmueble.APARTAMENTO;
        }
        return switch (roomType) {
            case "Private room", "Shared room", "Hotel room" -> TipoInmueble.HABITACION;
            default -> TipoInmueble.APARTAMENTO;
        };
    }
}
