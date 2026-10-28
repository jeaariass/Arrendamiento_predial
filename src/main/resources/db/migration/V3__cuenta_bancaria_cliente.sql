-- El cliente tambien puede guardar una cuenta bancaria (para que la plataforma
-- le devuelva dinero si aplica), igual que ya tenia el propietario.
ALTER TABLE public.cliente ADD COLUMN cuenta_bancaria character varying(40);
