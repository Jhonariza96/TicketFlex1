document.addEventListener('DOMContentLoaded', function () {
    const toast = new bootstrap.Toast(document.getElementById('toastNotificacion'));
    const API_URL = {
        buscarCliente: '/admin/prediccion/buscar-cliente',
        predecirIndividual: '/admin/prediccion/predecir',
        predecirMasivo: '/admin/prediccion/predecir-masivo'
    };

    // Configurar eventos
    configurarValidacionEnTiempoReal();
    document.getElementById('btnBuscar').addEventListener('click', buscarClientePorID);
    document.getElementById('clienteForm').addEventListener('submit', manejarEnvioFormulario);

    // Función principal para manejar el envío del formulario
    function manejarEnvioFormulario(e) {
        e.preventDefault();
        const action = e.submitter?.value;

        if (action === 'predecir-masivo') {
            const nuevoGenero = document.getElementById('nuevoGeneroConcierto');
            const nuevoPrecio = document.getElementById('nuevoPrecio');
            const descuentoSeleccionado = document.querySelector('input[name="descuentoDisponible"]:checked');

            // Validación completa
            if (!nuevoGenero || !nuevoGenero.value) {
                mostrarNotificacion('Seleccione el género del evento', 'danger');
                return;
            }
            if (!nuevoPrecio || isNaN(nuevoPrecio.value) || nuevoPrecio.value <= 0) {
                mostrarNotificacion('Ingrese un precio válido (mayor a 0)', 'danger');
                return;
            }
            if (!descuentoSeleccionado) {
                mostrarNotificacion('Seleccione si habrá descuento', 'danger');
                return;
            }

            const eventoData = {
                genero: nuevoGenero.value,
                precio: parseFloat(nuevoPrecio.value),
                descuentoDisponible: descuentoSeleccionado.value === '1'
            };

            predecirAsistenciaMasiva(eventoData);
        } else {
            predecirAsistenciaIndividual(e);
        }
    }


    // Función para mostrar resultados en tabla
    function mostrarResultadosTabla(clientes) {
        const tabla = document.getElementById('tablaResultados');
        const infoResultados = document.getElementById('resultadosInfo');

        // Limpiar tabla de manera eficiente
        const tbody = tabla.tBodies[0];
        tbody.innerHTML = ''; // Limpiar todas las filas del cuerpo de la tabla

        if (!clientes || clientes.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        No se encontraron clientes con alta probabilidad de asistencia
                    </td>
                </tr>
            `;
            infoResultados.style.display = 'none';
            return;
        }

        // Actualizar contador
        document.getElementById('totalClientes').textContent = clientes.length;
        infoResultados.style.display = 'block';

        // Crear encabezados
        const headerRow = `
            <tr>
                <th>ID Cliente</th>
                <th>Género</th>
                <th>Edad</th>
                <th>Género Favorito</th>
                <th>Gasto Promedio</th>
                <th>Predicción</th>
                <th>Probabilidad</th>
            </tr>
        `;
        tabla.innerHTML = headerRow;

        // Llenar tabla con datos
        clientes.forEach(cliente => {
            const probabilidad = cliente.probabilidad?.toFixed(1) || '0';
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${cliente.idCliente}</td>
                <td>${cliente.genero}</td>
                <td>${cliente.edad}</td>
                <td>${cliente.generoFavorito}</td>
                <td>$${(cliente.gastoPromedio || 0).toFixed(2)}</td>
                <td class="${cliente.prediccion === '1' ? 'text-success' : 'text-danger'}">
                    ${cliente.prediccion === '1' ? 'Asistirá' : 'No asistirá'}
                </td>
                <td>
                    <div class="progress" style="height: 20px;">
                        <div class="progress-bar ${getClaseProbabilidad(cliente.probabilidad)}" 
                             role="progressbar" 
                             style="width: ${cliente.probabilidad}%"
                             aria-valuenow="${cliente.probabilidad}"
                             aria-valuemin="0"
                             aria-valuemax="100">
                            ${probabilidad}%
                        </div>
                    </div>
                </td>
            `;
            tbody.appendChild(row);
        });
    }


    // Función para predicción individual
    async function predecirAsistenciaIndividual(event) {
        const boton = event.submitter;
        const textoOriginal = boton.innerHTML;

        try {
            // Mostrar estado de carga
            boton.disabled = true;
            boton.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Procesando...';

            if (!validarFormulario()) {
                throw new Error('Complete todos los campos requeridos');
            }

            const formData = new FormData(event.target);
            const datos = {
                idCliente: formData.get('idCliente'),
                edad: parseInt(formData.get('edad')),
                genero: formData.get('genero'),
                totalEventosAsistidos: parseInt(formData.get('totalEventosAsistidos')),
                generoFavorito: formData.get('generoFavorito'),
                promedioGastoHistorico: parseFloat(formData.get('promedioGastoHistorico')),
                nuevoGeneroConcierto: formData.get('nuevoGeneroConcierto'),
                nuevoPrecio: parseFloat(formData.get('nuevoPrecio')),
                descuentoDisponible: formData.get('descuentoDisponible') === '1'
            };

            const response = await fetch(API_URL.predecirIndividual, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(datos)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Error en el servidor');
            }

            const resultado = await response.json();
            mostrarResultadoIndividual(resultado);

        } catch (error) {
            console.error('Error en predicción individual:', error);
            mostrarNotificacion(error.message, 'danger');
        } finally {
            // Restaurar botón
            boton.disabled = false;
            boton.innerHTML = textoOriginal;
        }
    }

    // Función para mostrar resultado individual
    function mostrarResultadoIndividual(resultado) {
        const asistira = resultado.prediccion === '1';
        const color = resultado.confianzaValue >= 85 ? '#28a745' :
            resultado.confianzaValue >= 60 ? '#ffc107' : '#dc3545';

        Swal.fire({
            title: asistira ? '¡Asistirá al evento! ✅' : 'No asistirá ❌',
            html: `
                <div class="text-left">
                    <p><strong>Confianza:</strong> ${resultado.confianza}</p>
                    <div class="progress mt-2 mb-3" style="height: 20px;">
                        <div class="progress-bar" 
                             style="width: ${resultado.confianzaValue}%; background-color: ${color};" 
                             aria-valuenow="${resultado.confianzaValue}" 
                             aria-valuemin="0" 
                             aria-valuemax="100">
                            ${resultado.confianzaValue}%
                        </div>
                    </div>
                    <p>${resultado.confianzaValue >= 85 ? '✅ Alta confianza' :
                    resultado.confianzaValue >= 60 ? '⚠ Confianza moderada' : '❌ Baja confianza'}</p>
                </div>
            `,
            icon: asistira ? 'success' : 'error',
            confirmButtonText: 'Entendido',
            width: '600px'
        });
    }

    // Función para predicción masiva
    async function predecirAsistenciaMasiva(eventoData) {
        const btnPredecirMasivo = document.getElementById('btnPredecirMasivo');
        const textoOriginal = btnPredecirMasivo.innerHTML;

        try {
            btnPredecirMasivo.disabled = true;
            btnPredecirMasivo.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Procesando...';

            const response = await fetch(API_URL.predecirMasivo, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                body: JSON.stringify(eventoData)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Error en el servidor');
            }

            const resultados = await response.json();
            mostrarResultadosTabla(resultados);
            mostrarNotificacion(`Predicción masiva completada: ${resultados.length} clientes analizados`, 'success');

        } catch (error) {
            console.error('Error en predicción masiva:', error);
            mostrarNotificacion(error.message, 'danger');
        } finally {
            btnPredecirMasivo.disabled = false;
            btnPredecirMasivo.innerHTML = textoOriginal;
        }
    }

    // Función para buscar cliente por ID
    async function buscarClientePorID() {
        const idInput = document.getElementById('buscarId');
        const id = idInput.value.trim();
        const errorElement = document.getElementById('idError');
        const btnBuscar = document.getElementById('btnBuscar');

        // Validación inicial
        errorElement.textContent = '';
        if (!id || isNaN(id) || id <= 0) {
            errorElement.textContent = 'Ingrese un ID numérico válido';
            return;
        }

        // Estado de carga
        const btnOriginalText = btnBuscar.innerHTML;
        btnBuscar.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Buscando...';
        btnBuscar.disabled = true;

        try {
            const response = await fetch(`${API_URL.buscarCliente}?id=${id}`);

            if (!response.ok) {
                throw new Error(await response.text());
            }

            const data = await response.json();
            autocompletarFormulario(data);
            mostrarNotificacion(`Datos del cliente #${id} cargados`, 'success');

        } catch (error) {
            console.error('Error al buscar cliente:', error);
            errorElement.textContent = error.message.includes('no encontrado') ?
                `Cliente con ID ${id} no encontrado` : 'Error al buscar cliente';
            mostrarNotificacion(error.message, 'danger');

            if (error.message.includes('no encontrado')) {
                limpiarFormulario();
            }
        } finally {
            // Restaurar estado del botón
            btnBuscar.innerHTML = btnOriginalText;
            btnBuscar.disabled = false;
        }
    }

    // Función para autocompletar formulario
    function autocompletarFormulario(cliente) {
        document.getElementById('idCliente').value = cliente.id || '';
        document.getElementById('edad').value = cliente.edad || '';

        // Seleccionar género
        const generoSelect = document.getElementById('genero');
        [...generoSelect.options].forEach(opt => {
            opt.selected = opt.value === cliente.genero?.trim();
        });

        document.getElementById('totalEventosAsistidos').value = cliente.totalEventosAsistidos || '';

        // Seleccionar género favorito
        const generoFavoritoSelect = document.getElementById('generoFavorito');
        [...generoFavoritoSelect.options].forEach(opt => {
            opt.selected = opt.value === cliente.generoFavorito?.trim();
        });

        document.getElementById('promedioGastoHistorico').value = cliente.promedioGastoHistorico || '';
        document.getElementById('nuevoGeneroConcierto').value = cliente.nuevoGeneroConcierto?.trim() || '';
        document.getElementById('nuevoPrecio').value = cliente.nuevoPrecio || '';

        if (cliente.descuentoDisponible !== undefined) {
            const radioId = cliente.descuentoDisponible ? 'descuentoSi' : 'descuentoNo';
            document.getElementById(radioId).checked = true;
        }
    }

    // Función para limpiar formulario
    function limpiarFormulario() {
        const campos = [
            'idCliente', 'edad', 'totalEventosAsistidos',
            'promedioGastoHistorico', 'nuevoGeneroConcierto', 'nuevoPrecio'
        ];

        campos.forEach(id => {
            const input = document.getElementById(id);
            if (input) input.value = '';
        });

        document.querySelectorAll('input[name="descuentoDisponible"]').forEach(radio => {
            radio.checked = false;
        });
    }

    // Función para validar formulario
    function validarFormulario() {
        let valido = true;
        const camposRequeridos = [
            'idCliente', 'edad', 'genero', 'totalEventosAsistidos',
            'generoFavorito', 'promedioGastoHistorico',
            'nuevoGeneroConcierto', 'nuevoPrecio'
        ];

        camposRequeridos.forEach(campo => {
            const elemento = document.querySelector(`[name="${campo}"]`);
            if (!elemento || !elemento.value) {
                valido = false;
                const errorElement = document.getElementById(`${campo}Error`);
                if (errorElement) errorElement.textContent = 'Campo requerido';
            }
        });

        if (!document.querySelector('input[name="descuentoDisponible"]:checked')) {
            valido = false;
            document.getElementById('descuentoError').textContent = 'Seleccione una opción';
        }

        return valido;
    }


    // Función para configurar validación en tiempo real
    function configurarValidacionEnTiempoReal() {
        const campos = [
            'edad', 'totalEventosAsistidos',
            'promedioGastoHistorico', 'nuevoPrecio'
        ];

        campos.forEach(id => {
            const element = document.getElementById(id);
            if (element) {
                element.addEventListener('blur', validarCampoNumerico);
            }
        });

        document.querySelectorAll('input[type="radio"][name="descuentoDisponible"]').forEach(radio => {
            radio.addEventListener('change', function () {
                document.getElementById('descuentoError').textContent = '';
            });
        });
    }

    // Función para validar campo numérico
    function validarCampoNumerico(e) {
        const campo = e.target;
        const id = campo.id;
        const value = campo.value;
        let mensaje = '';

        if (!value) {
            mensaje = 'Campo requerido';
        } else if (isNaN(value)) {
            mensaje = 'Debe ser un número';
        } else if (value < 0) {
            mensaje = 'Debe ser positivo';
        } else if (id === 'edad' && (value < 18 || value > 100)) {
            mensaje = 'Edad entre 18-100';
        }

        // Mostrar el mensaje de error si existe
        const errorElement = document.getElementById(`${id}Error`);
        if (errorElement) {
            errorElement.textContent = mensaje;
        }

        // Si no hay mensaje de error, limpiar el texto
        if (!mensaje && errorElement) {
            errorElement.textContent = '';
        }
    }

    // Función para mostrar notificación
    function mostrarNotificacion(mensaje, tipo = 'success') {
        const toastEl = document.getElementById('toastNotificacion');
        const toastMensaje = document.getElementById('toastMensaje');

        if (!toastEl || !toastMensaje) return;

        toastEl.className = `toast align-items-center text-white bg-${tipo} border-0 show`;
        toastMensaje.textContent = mensaje;

        const toast = bootstrap.Toast.getOrCreateInstance(toastEl);
        toast.show();
    }

    // Función auxiliar para clase de probabilidad
    function getClaseProbabilidad(probabilidad) {
        if (probabilidad >= 80) return 'bg-success';
        if (probabilidad >= 60) return 'bg-warning';
        return 'bg-danger';
    }

    // Función para exportar a Excel
    $('#exportExcelBtn').click(function () {
        // Obtener datos de la tabla
        const rows = [];
        $('#resultsTableBody tr').each(function () {
            const row = [];
            $(this).find('td').each(function () {
                // Excluir la barra de progreso (solo tomar el texto)
                let text = $(this).find('.progress').length > 0
                    ? $(this).find('.progress-bar').text().trim()
                    : $(this).text().trim();
                row.push(text);
            });
            rows.push(row.join('\t')); // Separador de columnas (tabulación)
        });

        // Crear contenido CSV (compatible con Excel)
        const headers = [
            'ID Cliente', 'Género', 'Edad', 'Género Favorito',
            'Gasto Promedio', 'Probabilidad', 'Predicción', 'Razón Principal'
        ].join('\t');

        const csvContent = [headers, ...rows].join('\n');

        // Descargar archivo
        const blob = new Blob(["\uFEFF" + csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `prediccion_asistencia_${new Date().toLocaleDateString()}.xls`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    });
});