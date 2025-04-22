document.addEventListener("DOMContentLoaded", function () {
    // Eventos predefinidos
    const eventos = [
      {
        nombre: "Concierto de Rock",
        fecha: "2024-12-20",
        descripcion: "Un concierto de rock en vivo con bandas internacionales.",
        imagen: "img/rock.jpg",
        precioGeneral: 100,
        precioVIP: 200,
        precioPlatinum: 300,
      },
      {
        nombre: "Concierto de Pop",
        fecha: "2024-12-25",
        descripcion: "Disfruta de los mejores éxitos pop del momento.",
        imagen: "img/pop.jpg",
        precioGeneral: 50,
        precioVIP: 100,
        precioPlatinum: 150,
      },
    ];
  
    // Mostrar eventos en la página principal
    const eventosContainer = document.getElementById("eventos");
    eventos.forEach((evento) => {
      const card = document.createElement("div");
      card.classList.add("col-md-4", "evento-card");
      card.innerHTML = `
        <img src="${evento.imagen}" alt="${evento.nombre}">
        <h5>${evento.nombre}</h5>
        <p>${evento.descripcion}</p>
        <button class="btn btn-primary" onclick="comprarEntrada(${JSON.stringify(evento)})">Comprar Entradas</button>
      `;
      eventosContainer.appendChild(card);
    });
  
    // Función para abrir el modal de compra
    window.comprarEntrada = function (evento) {
      const cuotasSelect = document.getElementById("cuotas");
      const localidadSelect = document.getElementById("localidad");
      const fechaEvento = new Date(evento.fecha);
      const fechaHoy = new Date();
      const mesesRestantes = Math.max(1, Math.floor((fechaEvento - fechaHoy) / (1000 * 60 * 60 * 24 * 30))); // Calcular los meses restantes
  
      // Rellenar las cuotas disponibles
      cuotasSelect.innerHTML = "";
      for (let i = 1; i <= Math.min(mesesRestantes, 5); i++) {
        const option = document.createElement("option");
        option.value = i;
        option.textContent = `${i} cuota(s)`;
        cuotasSelect.appendChild(option);
      }
  
      // Actualizar los precios según la localidad seleccionada
      localidadSelect.addEventListener("change", function () {
        let precio = 0;
        switch (localidadSelect.value) {
          case "VIP":
            precio = evento.precioVIP;
            break;
          case "Platinum":
            precio = evento.precioPlatinum;
            break;
          default:
            precio = evento.precioGeneral;
        }
        console.log(`Precio: $${precio}`);
      });
    };
  });
  