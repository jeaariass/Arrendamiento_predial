package com.arriendaya.app.config;

import com.arriendaya.app.model.Inmueble;
import com.arriendaya.app.model.Propietario;
import com.arriendaya.app.model.TipoInmueble;
import com.arriendaya.app.model.Ubicacion;
import com.arriendaya.app.model.Usuario;
import com.arriendaya.app.repository.InmuebleRepository;
import com.arriendaya.app.repository.UbicacionRepository;
import com.arriendaya.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Pobla 14 ciudades grandes de Colombia (Bogota ya tiene 500 reales de Inside
 * Airbnb via ImportadorAirbnbSeeder, asi que no se repite aqui) con 500
 * alojamientos de ejemplo cada una, para que el buscador por ciudad tenga
 * contenido real en todo el pais y no solo en Bogota. No son datos reales:
 * se generan con plantillas y coordenadas con un poco de ruido alrededor del
 * centro de cada ciudad, y la descripcion lo dice explicitamente.
 */
@Component
@Order(3)
@RequiredArgsConstructor
public class CiudadesSinteticasSeeder implements CommandLineRunner {

    private static final String EMAIL_MARCADOR = "ciudades.sinteticas@arriendaya.local";
    private static final int INMUEBLES_POR_CIUDAD = 500;

    private record Ciudad(String nombre, double lat, double lon) {
    }

    private static final List<Ciudad> CIUDADES = List.of(
            new Ciudad("Medellin", 6.2442, -75.5812),
            new Ciudad("Cali", 3.4516, -76.5320),
            new Ciudad("Barranquilla", 10.9685, -74.7813),
            new Ciudad("Cartagena", 10.3910, -75.4794),
            new Ciudad("Bucaramanga", 7.1193, -73.1227),
            new Ciudad("Pereira", 4.8143, -75.6946),
            new Ciudad("Santa Marta", 11.2408, -74.1990),
            new Ciudad("Ibague", 4.4389, -75.2322),
            new Ciudad("Cucuta", 7.8939, -72.5078),
            new Ciudad("Manizales", 5.0689, -75.5174),
            new Ciudad("Villavicencio", 4.1420, -73.6266),
            new Ciudad("Armenia", 4.5339, -75.6811),
            new Ciudad("Monteria", 8.7479, -75.8814),
            new Ciudad("Pasto", 1.2136, -77.2811)
    );

    private static final List<String> ZONAS = List.of(
            "Zona Centro", "Zona Norte", "Zona Sur", "Zona Occidente", "Zona Rosa",
            "Via Principal", "Cerca al Aeropuerto", "Barrio Historico", "Zona Universitaria", "Zona Residencial"
    );

    private static final List<String> IMAGENES = List.of(
            "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800",
            "https://images.unsplash.com/photo-1484154218962-a197022b5858?w=800",
            "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?w=800",
            "https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?w=800",
            "https://images.unsplash.com/photo-1540518614846-7eded433c457?w=800",
            "https://images.unsplash.com/photo-1518780664697-55e3ad937233?w=800"
    );

    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final InmuebleRepository inmuebleRepository;
    private final PropietariosPoolSeeder propietariosPoolSeeder;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.existsByEmail(EMAIL_MARCADOR)) {
            return;
        }

        List<Propietario> pool = propietariosPoolSeeder.cargarPool();
        if (pool.isEmpty()) {
            throw new IllegalStateException("El pool de propietarios esta vacio: no se pueden generar las ciudades");
        }

        Random random = new Random();
        for (Ciudad ciudad : CIUDADES) {
            Ubicacion ubicacion = ubicacionRepository.findByCiudadIgnoreCaseAndPaisIgnoreCase(ciudad.nombre(), "Colombia")
                    .orElseGet(() -> {
                        Ubicacion u = new Ubicacion();
                        u.setCiudad(ciudad.nombre());
                        u.setPais("Colombia");
                        return ubicacionRepository.save(u);
                    });

            for (int n = 0; n < INMUEBLES_POR_CIUDAD; n++) {
                inmuebleRepository.save(generarInmueble(ciudad, ubicacion, pool, random));
            }
        }

        Usuario marcador = new Usuario();
        marcador.setNombre("Ciudades");
        marcador.setApellido("Sinteticas");
        marcador.setEmail(EMAIL_MARCADOR);
        marcador.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        usuarioRepository.save(marcador);

        org.slf4j.LoggerFactory.getLogger(CiudadesSinteticasSeeder.class)
                .info("Generados {} alojamientos de ejemplo en {} ciudades", CIUDADES.size() * INMUEBLES_POR_CIUDAD, CIUDADES.size());
    }

    private Inmueble generarInmueble(Ciudad ciudad, Ubicacion ubicacion, List<Propietario> pool, Random random) {
        TipoInmueble tipo = TipoInmueble.values()[random.nextInt(TipoInmueble.values().length)];
        String zona = ZONAS.get(random.nextInt(ZONAS.size()));

        Inmueble i = new Inmueble();
        i.setPropietario(pool.get(random.nextInt(pool.size())));
        i.setUbicacion(ubicacion);
        i.setTitulo(nombreTipo(tipo) + " en " + zona + ", " + ciudad.nombre());
        i.setDescripcion("Alojamiento de ejemplo generado para la demo (no es un dato real de Inside Airbnb).");
        i.setTipo(tipo);
        i.setDireccion(zona + ", " + ciudad.nombre());
        i.setPrecioNoche(precioAleatorio(tipo, random));
        i.setCapacidad(capacidadAleatoria(tipo, random));
        i.setImagenUrl(IMAGENES.get(random.nextInt(IMAGENES.size())));
        i.setLatitud(ciudad.lat() + (random.nextDouble() - 0.5) * 0.12);
        i.setLongitud(ciudad.lon() + (random.nextDouble() - 0.5) * 0.12);
        return i;
    }

    private String nombreTipo(TipoInmueble tipo) {
        return switch (tipo) {
            case APARTAMENTO -> "Apartamento";
            case CASA -> "Casa";
            case HABITACION -> "Habitacion";
            case FINCA -> "Finca";
            case CABANA -> "Cabana";
            case LOFT -> "Loft";
        };
    }

    private BigDecimal precioAleatorio(TipoInmueble tipo, Random random) {
        int min, max;
        switch (tipo) {
            case HABITACION -> { min = 50000; max = 120000; }
            case APARTAMENTO -> { min = 100000; max = 280000; }
            case LOFT -> { min = 120000; max = 300000; }
            case CABANA -> { min = 150000; max = 350000; }
            case CASA -> { min = 200000; max = 450000; }
            case FINCA -> { min = 300000; max = 600000; }
            default -> { min = 100000; max = 300000; }
        }
        int valor = min + random.nextInt((max - min) / 5000 + 1) * 5000;
        return BigDecimal.valueOf(valor);
    }

    private Integer capacidadAleatoria(TipoInmueble tipo, Random random) {
        int min, max;
        switch (tipo) {
            case HABITACION -> { min = 1; max = 2; }
            case APARTAMENTO -> { min = 2; max = 6; }
            case LOFT -> { min = 2; max = 4; }
            case CABANA -> { min = 2; max = 8; }
            case CASA -> { min = 4; max = 10; }
            case FINCA -> { min = 6; max = 15; }
            default -> { min = 1; max = 4; }
        }
        return min + random.nextInt(max - min + 1);
    }
}
