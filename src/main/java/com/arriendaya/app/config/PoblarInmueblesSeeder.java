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
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Puebla todos los inmuebles de la aplicacion: primero Bogota con datos reales
 * de Inside Airbnb (https://insideairbnb.com, dataset abierto para uso
 * academico/investigacion, CSV en src/main/resources/data/bogota_airbnb.csv),
 * y luego las otras 14 ciudades grandes de Colombia con alojamientos de
 * ejemplo generados por plantilla. Son los dos pasos de un mismo proceso de
 * carga (Bogota real, resto sintetico), por eso van en un solo seeder.
 *
 * Esta es la unica fuente de inmuebles: ya no existe un DataSeeder aparte con
 * un puñado de inmuebles de ejemplo "a mano", para no tener dos lugares con
 * datos de arranque.
 *
 * Los inmuebles no quedan cada uno con su propio "dueno de un solo
 * alojamiento": se reparten al azar entre el pool de PropietariosPoolSeeder,
 * asi cada dueno termina con varias propiedades.
 */
@Component
@Order(1)
@RequiredArgsConstructor
public class PoblarInmueblesSeeder implements CommandLineRunner {

    private static final String EMAIL_MARCADOR_BOGOTA = "importacion.airbnb@arriendaya.local";
    private static final String EMAIL_MARCADOR_CIUDADES = "ciudades.sinteticas@arriendaya.local";
    private static final int INMUEBLES_POR_CIUDAD = 500;

    private record Ciudad(String nombre, double lat, double lon) {
    }

    private static final List<Ciudad> CIUDADES_SINTETICAS = List.of(
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
    public void run(String... args) throws Exception {
        List<Propietario> pool = propietariosPoolSeeder.cargarPool();
        if (pool.isEmpty()) {
            throw new IllegalStateException("El pool de propietarios esta vacio: no se pueden poblar inmuebles");
        }

        importarBogotaDesdeAirbnb(pool);
        generarCiudadesSinteticas(pool);
    }

    private void importarBogotaDesdeAirbnb(List<Propietario> pool) throws Exception {
        if (usuarioRepository.existsByEmail(EMAIL_MARCADOR_BOGOTA)) {
            return;
        }

        Ubicacion bogota = ubicacionRepository.findByCiudadIgnoreCaseAndPaisIgnoreCase("Bogota", "Colombia")
                .orElseGet(() -> {
                    Ubicacion u = new Ubicacion();
                    u.setCiudad("Bogota");
                    u.setPais("Colombia");
                    return ubicacionRepository.save(u);
                });

        Random random = new Random();
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
                String nombreInmueble = campos[2];
                String roomType = campos[3];
                String barrio = campos[4];
                BigDecimal precio = new BigDecimal(campos[5]);
                Integer capacidad = Integer.valueOf(campos[6]);
                String imagenUrl = campos[7];
                Double latitud = Double.valueOf(campos[8]);
                Double longitud = Double.valueOf(campos[9]);

                Propietario propietario = pool.get(random.nextInt(pool.size()));

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

        usuarioRepository.save(marcador("Importacion", "Inside Airbnb", EMAIL_MARCADOR_BOGOTA));

        org.slf4j.LoggerFactory.getLogger(PoblarInmueblesSeeder.class)
                .info("Importados {} alojamientos reales de Bogota desde Inside Airbnb", importados);
    }

    private void generarCiudadesSinteticas(List<Propietario> pool) {
        if (usuarioRepository.existsByEmail(EMAIL_MARCADOR_CIUDADES)) {
            return;
        }

        Random random = new Random();
        for (Ciudad ciudad : CIUDADES_SINTETICAS) {
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

        usuarioRepository.save(marcador("Ciudades", "Sinteticas", EMAIL_MARCADOR_CIUDADES));

        org.slf4j.LoggerFactory.getLogger(PoblarInmueblesSeeder.class)
                .info("Generados {} alojamientos de ejemplo en {} ciudades", CIUDADES_SINTETICAS.size() * INMUEBLES_POR_CIUDAD, CIUDADES_SINTETICAS.size());
    }

    private Usuario marcador(String nombre, String apellido, String email) {
        Usuario marcador = new Usuario();
        marcador.setNombre(nombre);
        marcador.setApellido(apellido);
        marcador.setEmail(email);
        marcador.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        return marcador;
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
