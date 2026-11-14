-- Numero de tarjeta (u otra referencia) cuando el metodo de pago es TARJETA. Transferencia y
-- PSE no la necesitan (se asume que el pago queda hecho igual, es simulado).
ALTER TABLE public.pago ADD COLUMN referencia character varying(100);
