-- Una reserva PENDIENTE (sin pagar) ya no debe bloquearle las fechas a otro cliente: solo una
-- CONFIRMADA (pagada) ocupa de verdad el inmueble. El constraint original bloqueaba tambien
-- PENDIENTE, lo que haca que /buscar y /inmuebles/{id}/reservar tiraran 500 (exclusion
-- constraint) tan pronto dos reservas PENDIENTE distintas se cruzaban en fechas.
ALTER TABLE public.reserva DROP CONSTRAINT ex_reserva_sin_solapamiento;

ALTER TABLE public.reserva
    ADD CONSTRAINT ex_reserva_sin_solapamiento
    EXCLUDE USING gist (
        inmueble_id WITH =,
        daterange(fecha_inicio, fecha_fin, '[)') WITH &&
    )
    WHERE (estado = 'CONFIRMADA');
