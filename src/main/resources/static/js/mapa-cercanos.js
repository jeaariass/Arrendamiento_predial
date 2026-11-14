(function () {
    function formatearPrecio(precioNoche) {
        return precioNoche
            ? "$" + Math.round(precioNoche).toLocaleString("es-CO") + " COP / noche"
            : "";
    }

    function iconoCluster(cluster) {
        var cantidad = cluster.getChildCount();
        var talla = cantidad < 10 ? 36 : cantidad < 50 ? 44 : 52;
        return L.divIcon({
            className: "icono-cluster",
            html:
                '<div style="width:' + talla + "px;height:" + talla + 'px;">' +
                '<svg width="100%" height="100%" viewBox="0 0 48 48" xmlns="http://www.w3.org/2000/svg">' +
                '<circle cx="24" cy="24" r="22" fill="#febb02" stroke="#003580" stroke-width="3"/>' +
                "</svg>" +
                "<span>" + cantidad + "</span>" +
                "</div>",
            iconSize: [talla, talla]
        });
    }

    /**
     * Mapa chico centrado en (lat, lon) con los 50 inmuebles mas cercanos por distancia real
     * (sin importar la ciudad), agrupados en clusters cuando estan muy pegados. Idempotente.
     */
    window.iniciarMapaCercano = function (mapaId, listaId, lat, lon, excluirId, popupCentro) {
        var contenedor = document.getElementById(mapaId);
        if (!contenedor || contenedor.dataset.iniciado === "1" || typeof L === "undefined") {
            return;
        }
        contenedor.dataset.iniciado = "1";

        var mapa = L.map(mapaId).setView([lat, lon], 12);
        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "&copy; OpenStreetMap"
        }).addTo(mapa);
        L.marker([lat, lon]).addTo(mapa).bindPopup(popupCentro || "Este alojamiento").openPopup();

        var clusters = (typeof L.markerClusterGroup === "function")
            ? L.markerClusterGroup({ iconCreateFunction: iconoCluster })
            : L.layerGroup();
        mapa.addLayer(clusters);

        var params = new URLSearchParams({
            lat: lat,
            lon: lon,
            excluir: excluirId || -1,
            limite: 50
        });

        fetch("/mapa/cercanos?" + params.toString())
            .then(function (r) { return r.json(); })
            .then(function (puntos) {
                var lista = document.getElementById(listaId);
                if (lista) {
                    lista.innerHTML = "";
                }
                var bounds = [[lat, lon]];
                puntos.forEach(function (p) {
                    var marcador = L.marker([p.lat, p.lon]);
                    marcador.bindPopup('<strong><a href="/inmuebles/' + p.id + '">' + p.titulo + "</a></strong><br>" + formatearPrecio(p.precioNoche));
                    clusters.addLayer(marcador);
                    bounds.push([p.lat, p.lon]);
                    if (!lista) {
                        return;
                    }
                    var item = document.createElement("div");
                    item.className = "item-foto";
                    item.innerHTML =
                        '<img src="' + (p.imagenUrl || "/img/sin-imagen.svg") + '" alt="' + p.titulo + '">' +
                        '<div class="info"><h4>' + p.titulo + "</h4>" +
                        '<div class="precio">' + formatearPrecio(p.precioNoche) + "</div></div>";
                    item.addEventListener("click", function () {
                        window.location.href = "/inmuebles/" + p.id;
                    });
                    lista.appendChild(item);
                });
                if (bounds.length > 1) {
                    mapa.fitBounds(bounds, { padding: [30, 30], maxZoom: 14 });
                }
                if (lista && puntos.length === 0) {
                    lista.innerHTML = '<div class="vacio-lista">No hay otros inmuebles cerca por ahora.</div>';
                }
            })
            .catch(function () {});
    };
})();
