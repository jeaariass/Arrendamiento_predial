package com.arriendaya.app.config;

import com.arriendaya.app.model.Banco;
import com.arriendaya.app.repository.BancoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Catalogo de bancos para poder pagar una reserva. A diferencia de los demas
 * seeders (datos de ejemplo), este siempre corre: un banco no es un dato de
 * demo, es informacion de referencia que la app necesita para funcionar.
 */
@Component
@Order(-1)
@RequiredArgsConstructor
public class BancoSeeder implements CommandLineRunner {

    private final BancoRepository bancoRepository;

    @Override
    public void run(String... args) {
        if (bancoRepository.count() > 0) {
            return;
        }
        bancoRepository.save(banco("Bancolombia", "BCOL"));
        bancoRepository.save(banco("Davivienda", "DAVI"));
        bancoRepository.save(banco("BBVA Colombia", "BBVA"));
        bancoRepository.save(banco("Banco de Bogota", "BBOG"));
    }

    private Banco banco(String nombre, String codigo) {
        Banco b = new Banco();
        b.setNombre(nombre);
        b.setCodigo(codigo);
        return b;
    }
}
