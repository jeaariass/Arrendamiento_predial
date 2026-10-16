(function () {
    function iniciar() {
        var el = document.getElementById("mapa");
        if (!el || typeof L === "undefined") {
            return;
        }
        var filtros = window.MAPA_FILTROS || {};
        var mapa = L.map("mapa").setView([4.6097, -74.0817], 12);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap contributors",
            maxZoom: 19
        }).addTo(mapa);

        var iconoInmueble = L.divIcon({
            className: "icono-inmueble",
            html:
                '<svg width="30" height="38" viewBox="0 0 30 38" xmlns="http://www.w3.org/2000/svg">' +
                '<path d="M15 0C6.7 0 0 6.7 0 15c0 11.2 15 23 15 23s15-11.8 15-23C30 6.7 23.3 0 15 0z" fill="#0057b8"/>' +
                '<circle cx="15" cy="15" r="10" fill="#febb02"/>' +
                '<path d="M15 8.5l8 6.4V24h-5.3v-6H12.3v6H7v-9.1l8-6.4z" fill="#003580"/>' +
                "</svg>",
            iconSize: [30, 38],
            iconAnchor: [15, 38],
            popupAnchor: [0, -34]
        });

        var clusters = L.markerClusterGroup({
            iconCreateFunction: function (cluster) {
                var cantidad = cluster.getChildCount();
                var talla = cantidad < 10 ? 36 : cantidad < 50 ? 44 : 52;
                return L.divIcon({
                    className: "icono-cluster",
                    html:
                        '<div style="width:' + talla + "px;height:" + talla + 'px;">' +
                        '<svg width="100%" height="100%" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">' +
                        '<circle cx="24" cy="24" r="22" fill="#febb02" stroke="#003580" stroke-width="3"/>' +
                        "</svg>" +
                        '<span>' + cantidad + "</span>" +
                        "</div>",
                    iconSize: [talla, talla]
                });
            }
        });
        mapa.addLayer(clusters);

        var lista = document.getElementById("listaMapa");
        var primerCarga = true;
        var timer;

        function formatearPrecio(precioNoche) {
            return precioNoche
                ? "$" + Math.round(precioNoche).toLocaleString("es-CO") + " COP / noche"
                : "";
        }

        function renderLista(puntos) {
            if (!lista) {
                return;
            }
            lista.innerHTML = "";
            if (puntos.length === 0) {
                lista.innerHTML = '<div class="vacio-lista">No hay alojamientos en esta zona del mapa.</div>';
                return;
            }
            puntos.forEach(function (p) {
                var item = document.createElement("div");
                item.className = "item-foto";
                item.innerHTML =
                    '<img src="' + (p.imagenUrl || "") + '" alt="' + p.titulo + '">' +
                    '<div class="info"><h4>' + p.titulo + "</h4>" +
                    '<div class="precio">' + formatearPrecio(p.precioNoche) + "</div></div>";
                item.addEventListener("click", function () {
                    window.location.href = "/inmuebles/" + p.id;
                });
                lista.appendChild(item);
            });
        }

        function cargar() {
            var b = mapa.getBounds();
            var params = new URLSearchParams({
                minLat: b.getSouth(),
                maxLat: b.getNorth(),
                minLon: b.getWest(),
                maxLon: b.getEast()
            });
            if (filtros.ciudad) {
                params.set("ciudad", filtros.ciudad);
            }
            if (filtros.llegada) {
                params.set("llegada", filtros.llegada);
            }
            if (filtros.salida) {
                params.set("salida", filtros.salida);
            }

            fetch("/mapa/puntos?" + params.toString())
                .then(function (r) { return r.json(); })
                .then(function (puntos) {
                    clusters.clearLayers();
                    var bounds = [];
                    puntos.forEach(function (p) {
                        var marcador = L.marker([p.lat, p.lon], { icon: iconoInmueble });
                        marcador.bindPopup(
                            '<strong><a href="/inmuebles/' + p.id + '">' + p.titulo + "</a></strong><br>" +
                            formatearPrecio(p.precioNoche)
                        );
                        clusters.addLayer(marcador);
                        bounds.push([p.lat, p.lon]);
                    });
                    renderLista(puntos);
                    if (primerCarga && bounds.length > 0) {
                        primerCarga = false;
                        mapa.fitBounds(bounds, { padding: [30, 30] });
                    }
                })
                .catch(function () {});
        }

        mapa.on("moveend", function () {
            clearTimeout(timer);
            timer = setTimeout(cargar, 300);
        });

        cargar();
    }

    document.addEventListener("DOMContentLoaded", iniciar);
})();
