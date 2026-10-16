(function () {
    function render(box, input, ciudades) {
        box.innerHTML = "";
        if (!ciudades || ciudades.length === 0) {
            box.classList.remove("visible");
            return;
        }
        ciudades.forEach(function (c) {
            var item = document.createElement("div");
            item.className = "sugerencia";
            item.textContent = c;
            item.addEventListener("mousedown", function (e) {
                e.preventDefault();
                input.value = c;
                box.classList.remove("visible");
            });
            box.appendChild(item);
        });
        box.classList.add("visible");
    }

    function buscarSugerencias(box, input, q) {
        fetch("/ciudades/sugerencias?q=" + encodeURIComponent(q))
            .then(function (r) { return r.json(); })
            .then(function (data) { render(box, input, data); })
            .catch(function () {});
    }

    function setup(inputId, boxId) {
        var input = document.getElementById(inputId);
        var box = document.getElementById(boxId);
        if (!input || !box) {
            return;
        }
        var timer;

        function mostrar(q) {
            if (q === "") {
                render(box, input, window.CIUDADES_TOP || []);
            } else {
                buscarSugerencias(box, input, q);
            }
        }

        input.addEventListener("focus", function () {
            mostrar(input.value.trim());
        });

        input.addEventListener("input", function () {
            var q = input.value.trim();
            clearTimeout(timer);
            timer = setTimeout(function () { mostrar(q); }, 200);
        });

        document.addEventListener("click", function (e) {
            if (e.target !== input && !box.contains(e.target)) {
                box.classList.remove("visible");
            }
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        setup("campoCiudad", "sugerenciasCiudad");
    });
})();
