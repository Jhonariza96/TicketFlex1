// Estado de autenticación
let isAuthenticated = false; // Variable para verificar si el usuario está autenticado
let userId = null; // Variable para almacenar el ID del usuario logueado

// Funcion para el DOM
document.addEventListener('DOMContentLoaded', function () {
    cargarEventos(); // Cargar eventos al inicio
    agregarEventosLogin();
    agregarEventosFiltros();
});


// ========================================================
// Manejo de eventos relacionados con la autenticación

function agregarEventosLogin() {
    // Mostrar el modal de inicio de sesión
    document.getElementById('showLoginButton').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'block';
    });

    // Mostrar el modal de registro
    document.getElementById('showRegisterButton').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'block';
    });

    // Cerrar el modal de inicio de sesión
    document.getElementById('closeLoginModal').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'none';
    });

    // Cerrar el modal de registro
    document.getElementById('closeRegisterModal').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'none';
    });

    // Manejo del formulario de inicio de sesión
    document.getElementById('loginForm').addEventListener('submit', login);
    document.getElementById('registroForm').addEventListener('submit', registro);
}

// Manejo del formulario de inicio de sesión
function login(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData);

    fetch('http://localhost:8080/api/usuarios/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(data),
        credentials: 'include'
    })
        .then(response => response.json())
        .then(data => {
            isAuthenticated = true;
            userId = data.id;
            document.getElementById('userName').innerText = data.nombre;
            document.getElementById('loginButtonSection').classList.add('hidden');
            document.getElementById('userSection').classList.remove('hidden');
            alert('¡Bienvenido, ' + data.nombre + '!');
            document.getElementById('loginModal').style.display = 'none';
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al iniciar sesión: ' + error.message);
        });
}

// Manejo del formulario de registro de un usuario
function registro(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData);

    console.log("Datos a enviar:", data); // Para depuración

    fetch('http://localhost:8080/api/usuarios/registrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
        .then(async response => {
            const result = await response.json();
            if (!response.ok) {
                throw new Error(result.error || 'Error al registrar');
            }
            alert('Registro exitoso. Puedes iniciar sesión ahora.');
            document.getElementById('registerModal').style.display = 'none';
            event.target.reset();
        })
        .catch(error => {
            console.error('Error completo:', error);
            alert('Error al registrar: ' + error.message);
        });
}

// Luego asigna los event listeners
function agregarEventosLogin() {
    // Mostrar modales
    document.getElementById('showLoginButton').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'block';
    });

    document.getElementById('showRegisterButton').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'block';
    });

    // Cerrar modales
    document.getElementById('closeLoginModal').addEventListener('click', () => {
        document.getElementById('loginModal').style.display = 'none';
    });

    document.getElementById('closeRegisterModal').addEventListener('click', () => {
        document.getElementById('registerModal').style.display = 'none';
    });

    // Asignar manejadores de formularios
    document.getElementById('loginForm').addEventListener('submit', login);
    document.getElementById('registroForm').addEventListener('submit', registro);
}

// Inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function () {
    cargarEventos();
    agregarEventosLogin();
    agregarEventosFiltros();
});

// Cerrar el modal al hacer clic fuera de él
window.onclick = function (event) {
    const loginModal = document.getElementById('loginModal');
    const registerModal = document.getElementById('registerModal');
    if (event.target === loginModal) {
        loginModal.style.display = 'none';
    }
    if (event.target === registerModal) {
        registerModal.style.display = 'none';
    }
};

// Manejo del cierre de sesión
document.getElementById('logoutButton').addEventListener('click', function () {
    fetch('http://localhost:8080/api/usuarios/logout', {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error al cerrar sesión');
            }
            alert('Sesión cerrada exitosamente');
            isAuthenticated = false;
            userId = null;
            document.getElementById('loginButtonSection').classList.remove('hidden');
            document.getElementById('userSection').classList.add('hidden');
            document.getElementById('userName').innerText = '';
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al cerrar sesión: ' + error.message);
        });
});

// ========================================================
/// Función para cargar y filtrar eventos
function agregarEventosFiltros() {
    // Manejadores de eventos para aplicar y resetear filtros
    document.querySelector('#resetFilters').addEventListener('click', resetearFiltros);
    document.querySelector('#filtrar').addEventListener('click', aplicarFiltros);
}

// Resetear filtros y recargar todos los eventos
function resetearFiltros() {
    document.querySelector('#lugar').value = '';
    document.querySelector('#fecha').value = '';
    document.querySelector('#categoria').value = '';
    document.querySelector('#artista').value = '';
    cargarEventos(); // Recargar todos los eventos sin aplicar filtros
}

// Función para aplicar los filtros
function aplicarFiltros() {
    const filters = {
        lugar: document.querySelector('#lugar').value.trim(),
        fecha: document.querySelector('#fecha').value.trim(),
        categoria: document.querySelector('#categoria').value.trim(),
        artista: document.querySelector('#artista').value.trim()
    };

    fetch('http://localhost:8080/api/eventos/filtrar', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(filters),
        credentials: 'include'
    })
        .then(async response => {
            if (response.redirected) {
                document.getElementById('loginModal').style.display = 'block';
                throw new Error('Por favor inicie sesión');
            }
            if (!response.ok) {
                const error = await response.text();
                throw new Error(error || 'Error al filtrar');
            }
            return response.json();
        })
        .then(mostrarEventos) // Usamos la misma función para mostrar
        .catch(error => {
            console.error('Error:', error);
            alert(error.message);
        });
}
// Mostrar los eventos filtrados en la interfaz
function mostrarEventos(eventos) {
    const contenedor = document.querySelector('#eventosList');
    contenedor.innerHTML = '';

    if (!eventos || eventos.length === 0) {
        contenedor.innerHTML = '<p class="no-events-message">No se encontraron eventos.</p>';
        return;
    }

    eventos.forEach(evento => {
        const eventoDiv = document.createElement('div');
        eventoDiv.classList.add('evento-card');

        eventoDiv.innerHTML = `
            <div class="evento-imagen">
                <img src="http://localhost:8080/images/${evento.imagen || 'default-event.jpg'}" 
                     alt="${evento.nombreEvento || 'Evento'}">
            </div>
            <div class="evento-info">
                <h3>${evento.nombreEvento || 'Evento sin nombre'}</h3>
                <p class="evento-artista"><i class="fas fa-user"></i> ${evento.artista || 'Varios artistas'}</p>
                <p class="evento-fecha"><i class="far fa-calendar-alt"></i> ${formatearFecha(evento.fecha) || 'Fecha por definir'}</p>
                <p class="evento-lugar"><i class="fas fa-map-marker-alt"></i> ${evento.lugar || 'Lugar por confirmar'}</p>
                ${evento.descripcion ? `<p class="evento-descripcion">${evento.descripcion}</p>` : ''}
                <p class="evento-precio"><strong>Precio:</strong> $${(evento.precioBase || 0).toLocaleString('es-CO')}</p>
                <button class="btn-comprar" onclick="comprarBoleto(${evento.idEvento})">
                    <i class="fas fa-ticket-alt"></i> Comprar
                </button>
            </div>
        `;
        contenedor.appendChild(eventoDiv);
    });
}


function cargarEventos() {
    fetch('http://localhost:8080/api/eventos/listar', {
        credentials: 'include'
    })
        .then(response => {
            if (response.redirected) {
                document.getElementById('loginModal').style.display = 'block';
                throw new Error('Debes iniciar sesión');
            }
            return response.json();
        })
        .then(mostrarEventos) // Usamos la misma función para mostrar
        .catch(error => {
            console.error('Error:', error);
            document.querySelector('#eventosList').innerHTML = `
            <p class="error-message">Error al cargar eventos: ${error.message}</p>
        `;
        });
}

// Función auxiliar para formatear la fecha (opcional)
function formatearFecha(fechaString) {
    if (!fechaString) return '';

    try {
        const fecha = new Date(fechaString);
        if (isNaN(fecha.getTime())) return fechaString;

        return fecha.toLocaleDateString('es-CO', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    } catch (e) {
        console.error('Error formateando fecha:', e);
        return fechaString;
    }
}


// VARIABLES PARA LA COMPRA DEL BOLETO
const buyTicketModal = document.getElementById("buyTicketModal");
const closeModalButton = document.getElementById("closeModal");
const buyButton = document.getElementById("buyButton");
const ticketCountInput = document.getElementById("ticketCount");
const graderiaInput = document.getElementById("graderia");
let idEventoToBuy = null;

// Función para abrir el modal y manejar la compra
function comprarBoleto(eventoId) {
    if (!isAuthenticated) {
        alert('Por favor, inicie sesión para comprar boletos.');
        return;
    }

    // Guardamos el ID del evento para hacer la compra posteriormente
    idEventoToBuy = eventoId;

    // Mostrar el modal
    buyTicketModal.style.display = "block";
}

// Función para cerrar el modal
closeModalButton.addEventListener("click", function () {
    buyTicketModal.style.display = "none";
});

// Función para procesar la compra
buyButton.addEventListener("click", function () {
    let cantidad = parseInt(ticketCountInput.value, 10); // Obtenemos la cantidad de boletos

    // Validación de la cantidad
    if (isNaN(cantidad) || cantidad < 1 || cantidad > 5) {
        alert('Cantidad no válida. Debe ser un número entre 1 y 5.');
        return;
    }

    if (userId === null || userId === undefined) {
        alert('Error: ID de usuario no definido.');
        return;
    }

    // Realizamos la solicitud de compra a la API
    fetch(`http://localhost:8080/api/eventos/${idEventoToBuy}/comprar?idUsuario=${userId}&cantidad=${cantidad}`, {
        method: 'POST'
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error en la compra: ' + response.statusText);
            }
            return response.text();
        })
        .then(data => {
            alert(data); // Mensaje de éxito
            buyTicketModal.style.display = "none"; // Cerrar el modal después de la compra
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al realizar la compra: ' + error.message);
        });
});

// Cierra el modal si el usuario hace clic fuera de él
window.addEventListener("click", function (event) {
    if (event.target === buyTicketModal) {
        buyTicketModal.style.display = "none";
    }
});



// Función para dirigirse hacia la parte superior
document.getElementById("scrollToTopButton").onclick = function () {
    window.scrollTo({ top: 0, behavior: 'smooth' });
};

// Función para mostrar u ocultar los campos de tarjeta
function toggleCardFields() {
    const bankSelect = document.getElementById('bank');
    const cardDetails = document.getElementById('cardDetails');

    // Si se selecciona un banco, mostrar los campos de tarjeta
    if (bankSelect.value) {
        cardDetails.style.display = 'block';
    } else {
        cardDetails.style.display = 'none';
    }
}
// Mostrar/ocultar pestañas
function openPaymentTab(tabName) {
    document.querySelectorAll('.payment-tab-content').forEach(tab => {
        tab.style.display = 'none';
    });
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    document.getElementById(`${tabName}-tab`).style.display = 'block';
    event.currentTarget.classList.add('active');
}

// Procesar pago según método seleccionado
function processPayment(method) {
    const eventoId = idEventoToBuy;
    const cantidad = (method === 'ticketflex') ?
        parseInt(document.getElementById('tf-ticketCount').value) :
        parseInt(document.getElementById('ticketCount').value);

    if (!isAuthenticated) {
        alert("Debes iniciar sesión para comprar boletos");
        return;
    }

    if (method === 'ticketflex') {
        // Lógica para TicketFlex
        fetch(`/api/boletas/comprar?idEvento=${eventoId}&idUsuario=${userId}&cantidad=${cantidad}&metodoPago=TICKETFLEX`, {
            method: 'POST'
        })
            .then(response => response.json())
            .then(data => {
                showTicketFlexConfirmation(data);
                buyTicketModal.style.display = "none";
            });
    } else {
        // Lógica para pago tradicional (existente)
        fetch(`http://localhost:8080/api/eventos/${eventoId}/comprar?idUsuario=${userId}&cantidad=${cantidad}`, {
            method: 'POST'
        })
            .then(response => response.text())
            .then(data => {
                alert("Compra exitosa: " + data);
                buyTicketModal.style.display = "none";
            });
    }
}

// Mostrar confirmación de TicketFlex
function showTicketFlexConfirmation(data) {
    const confirmationHTML = `
                    <div class="ticketflex-confirmation">
                        <h3><i class="fas fa-check-circle text-success"></i> ¡Reserva Exitosa!</h3>
                        <div class="qr-placeholder">
                            <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=TEMP-${data.idBoleto}" alt="QR Temporal">
                            <p class="text-muted">QR temporal - Se activará al completar el pago</p>
                        </div>
                        <div class="payment-details">
                            <p><strong>Total a pagar:</strong> $${data.precioTotal}</p>
                            <p><strong>Fecha límite:</strong> ${data.fechaLimitePago}</p>
                            <button onclick="location.href='/completar-pago?id=${data.idBoleto}'" class="btn-pay-now">
                                Completar Pago Ahora
                            </button>
                        </div>
                    </div>
                `;

    // Crear un nuevo modal o reemplazar contenido
    document.body.insertAdjacentHTML('beforeend', confirmationHTML);
}



// Mostrar el botón de "Scroll to top"
window.onscroll = function () {
    var footer = document.getElementById("footer");
    var scrollToTopButton = document.getElementById("scrollToTopButton");

    // Comprobar si el usuario está cerca del footer
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - footer.offsetHeight) {
        scrollToTopButton.style.display = "block";  // Mostrar el botón cuando esté cerca del footer
    } else {
        scrollToTopButton.style.display = "none";  // Ocultar el botón cuando no esté cerca
    }
};

