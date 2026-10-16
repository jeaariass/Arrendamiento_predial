-- Bandera de acceso para el panel de administracion. No es un objeto nuevo
-- del modelo conceptual (el profesor pidio no tener "Administrador" como
-- entidad propia, tipo Propietario/Cliente): es solo un campo de control de
-- acceso sobre Usuario, igual de "implementacion" que password_hash.
ALTER TABLE public.usuario ADD COLUMN es_admin boolean NOT NULL DEFAULT false;
