document.addEventListener('DOMContentLoaded', function () {

    const toast = new bootstrap.Toast(document.getElementById('toastNotificacion'));

    configurarValidacionEnTiempoReal();

    document.getElementById('btnBuscar').addEventListener('click', buscarClientePorID);

    async function buscarClientePorID() {
        const idInput = document.getElementById('buscarId');
        const id = idInput.value.trim();
        const errorElement = document.getElementById('idError');
        const btnBuscar = document.getElementById('btnBuscar');

        errorElement.textContent = '';

        if (!id || isNaN(id) || id <= 0) {
            errorElement.textContent = 'Ingrese un ID numérico válido (ej: 1, 3, 8, 9)';
            idInput.focus();
            return;
        }

        const btnOriginalText = btnBuscar.innerHTML;
        btnBuscar.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Buscando...';
        btnBuscar.disabled = true;

        try {
            const response = await fetch(`/admin/prediccion/buscar?id=${id}`);

            if (!response.ok) {
                throw new Error(response.status === 404 ?
                    `El ID ${id} no existe en la base de datos` :
                    'Error del servidor al buscar');
            }

            const cliente = await response.json();

            if (!cliente.hasOwnProperty('edad') || !cliente.hasOwnProperty('genero')) {
                throw new Error('Datos del cliente incompletos');
            }

            autocompletarFormulario(cliente);

            mostrarNotificacion(`Datos del ID ${id} cargados`, 'success');

        } catch (error) {
            errorElement.textContent = error.message;
            mostrarNotificacion(error.message, 'danger');
            console.error('Error:', error);

            if (error.message.includes('no existe')) {
                limpiarFormulario();
            }
        } finally {
            btnBuscar.innerHTML = btnOriginalText;
            btnBuscar.disabled = false;
        }
    }

    function autocompletarFormulario(cliente) {
        console.log("📦 Datos recibidos desde la API:", cliente);
        console.log(`Genero recibido: "${cliente.genero}"`);
        console.log(`Genero favorito: "${cliente.generoFavorito}"`);
        console.log(`Nuevo genero concierto: "${cliente.nuevoGeneroConcierto}"`);
        console.log(`Interés en el evento: "${cliente.interesEvento}"`);
    
        document.getElementById('idCliente').value = cliente.id || '';
        document.getElementById('edad').value = cliente.edad || '';
    
        // ✅ Seleccionar opción en el <select> de género
        const generoSelect = document.getElementById('genero');
        [...generoSelect.options].forEach(opt => {
            opt.selected = opt.value === cliente.genero?.trim();
        });
    
        document.getElementById('totalEventosAsistidos').value = cliente.totalEventosAsistidos || '';
    
        // ✅ Seleccionar opción en el <select> de género favorito
        const generoFavoritoSelect = document.getElementById('generoFavorito');
        [...generoFavoritoSelect.options].forEach(opt => {
            opt.selected = opt.value === cliente.generoFavorito?.trim();
        });
    
        document.getElementById('promedioGastoHistorico').value = cliente.promedioGastoHistorico || '';
        document.getElementById('nuevoGeneroConcierto').value = cliente.nuevoGeneroConcierto?.trim() || '';
        document.getElementById('nuevoPrecio').value = cliente.nuevoPrecio || '';
    
        if (cliente.descuentoDisponible !== undefined && cliente.descuentoDisponible !== null) {
            const radioId = cliente.descuentoDisponible ? 'descuentoSi' : 'descuentoNo';
            document.getElementById(radioId).checked = true;
        }
    }

    function limpiarFormulario() {
        const campos = [
            'edad', 'genero', 'totalEventosAsistidos', 'generoFavorito',
            'promedioGastoHistorico', 'nuevoGeneroConcierto', 'nuevoArtista',
            'nuevoPrecio', 'idCliente'
        ];

        campos.forEach(id => {
            const input = document.getElementById(id);
            if (input) input.value = '';
        });

        document.querySelectorAll('input[name="descuentoDisponible"]').forEach(radio => {
            radio.checked = false;
        });
    }


    function validarYEnviarFormulario(event) {
        let isValid = true;
        const errores = {};

        document.querySelectorAll('.error-message').forEach(el => el.textContent = '');

        const validaciones = [
            { id: 'edad', condicion: v => !v || v < 18 || v > 100, mensaje: 'La edad debe estar entre 18 y 100 años' },
            { id: 'genero', condicion: v => !v, mensaje: 'Por favor seleccione un género' },
            { id: 'totalEventosAsistidos', condicion: v => v === '' || v < 0, mensaje: 'Debe ingresar un número válido de eventos asistidos' },
            { id: 'generoFavorito', condicion: v => !v, mensaje: 'Por favor ingrese el género favorito' },
            { id: 'promedioGastoHistorico', condicion: v => !v || v <= 0, mensaje: 'Ingrese un gasto promedio válido' },
            { id: 'nuevoGeneroConcierto', condicion: v => !v, mensaje: 'Ingrese el género del nuevo concierto' },
            { id: 'nuevoPrecio', condicion: v => !v || v <= 0, mensaje: 'Ingrese un precio válido' },
            {
                name: 'descuentoDisponible',
                condicion: () => !document.querySelector('input[name="descuentoDisponible"]:checked'),
                mensaje: 'Seleccione si hay descuento disponible'
            }
        ];

        for (const campo of validaciones) {
            let valor;
            if (campo.id) {
                const input = document.getElementById(campo.id);
                valor = input?.value;
            } else if (campo.name) {
                valor = document.querySelector(`input[name="${campo.name}"]:checked`)?.value;
            }

            if (campo.condicion(valor)) {
                isValid = false;
                const errorEl = document.getElementById((campo.id || campo.name) + 'Error');
                if (errorEl) errorEl.textContent = campo.mensaje;
            }
        }

        if (!isValid) {
            event.preventDefault();
            mostrarNotificacion('Por favor corrija los errores del formulario', 'danger');
        }
    }

    function configurarValidacionEnTiempoReal() {
        const campos = [
            'edad', 'genero', 'totalEventosAsistidos', 'generoFavorito',
            'promedioGastoHistorico', 'nuevoGeneroConcierto',
            'nuevoPrecio'
        ];

        campos.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                element.addEventListener('blur', function () {
                    validarCampo(this);
                });
            }
        });

        document.querySelectorAll('input[type="radio"][name="descuentoDisponible"]').forEach(radio => {
            radio.addEventListener('change', function () {
                const errorElement = document.getElementById('descuentoDisponibleError');
                if (errorElement) errorElement.textContent = '';
            });
        });
    }

    function validarCampo(campo) {
        const id = campo.id;
        const value = campo.value;
        let esValido = true;
        let mensaje = '';

        switch (id) {
            case 'edad':
                esValido = value && value >= 18 && value <= 100;
                mensaje = 'La edad debe estar entre 18 y 100 años';
                break;
            case 'totalEventosAsistidos':
                esValido = value !== '' && value >= 0;
                mensaje = 'Debe ingresar un número válido de eventos asistidos';
                break;
            case 'promedioGastoHistorico':
                esValido = value && value > 0;
                mensaje = 'Ingrese un gasto promedio válido';
                break;
            case 'nuevoPrecio':
                esValido = value && value > 0;
                mensaje = 'Ingrese un precio válido';
                break;
            default:
                esValido = !!value;
                mensaje = 'Este campo es requerido';
        }

        const errorElement = document.getElementById(`${id}Error`);
        if (errorElement) {
            errorElement.textContent = esValido ? '' : mensaje;
        }

        return esValido;
    }

    function mostrarNotificacion(mensaje, tipo = 'success') {
        const toastEl = document.getElementById('toastNotificacion');
        const toastMensaje = document.getElementById('toastMensaje');

        if (!toastEl || !toastMensaje) {
            console.warn('Toast no encontrado en el DOM');
            return;
        }

        toastEl.className = `toast align-items-center text-white bg-${tipo} border-0 show`;
        toastMensaje.textContent = mensaje;

        const toast = bootstrap.Toast.getOrCreateInstance(toastEl);
        toast.show();
    }
});
