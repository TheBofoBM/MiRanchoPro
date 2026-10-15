# Operación del rancho, historiales y facilidad de uso

Fecha: 14 de octubre de 2026.

Este bloque implementa las ocho prioridades acordadas y las mejoras de uso que las acompañan. Trabaja sobre los datos locales del teléfono. La colaboración en línea, los cobros y la separación de varios ranchos siguen siendo etapas distintas.

## Las ocho prioridades

1. **Respaldo y restauración.** Configuración permite crear un archivo `.mrp` cifrado con contraseña, con cuentas, ganado, salud, tareas, lotes, inventario e historiales. Incluye las fotografías usando referencias portables. Se comprueba el archivo y se muestran sus cantidades antes de reemplazar datos; el dueño confirma con su contraseña actual. En una instalación vacía también se puede recuperar desde Login. La sustitución de registros es transaccional y los archivos se preparan antes de confirmar. Una contraseña incorrecta o una operación fallida no reemplaza los registros. El límite actual es 64 MB de archivo cifrado y 128 MB extraídos; no es sincronización automática.
2. **Ficha e historial del animal.** Ganado abre una ficha con fotografía, identificación, peso, lote, edad, relación con su madre y crías, pesajes, salud, gestaciones y eventos de estado/traslado. Se pueden registrar pesajes por fecha y notas. Un pesaje histórico no sustituye el peso más reciente; varios pesajes del mismo día conservan su orden de registro.
3. **Calendario y recordatorios.** El calendario reúne tareas con fecha, próximas aplicaciones y gestaciones activas, con filtros por tipo y horizonte. Respeta las tareas asignadas al empleado. Los avisos de salud se pueden marcar como atendidos sin inventar una aplicación realizada. En Configuración se activa un resumen diario, hora aproximada y anticipación. Se pide permiso de notificaciones cuando corresponde, se reprograma tras reinicio/cambio de hora y el aviso abre el calendario. Android puede retrasar las alarmas por ahorro de batería.
4. **Partos y crías.** Desde una hembra activa se registra un parto, con una a cuatro crías vivas o un resultado sin crías vivas. Se enlazan madre y crías y se cierra la gestación asociada en la misma transacción. Aretes duplicados o capacidad insuficiente revierten la operación completa. La ficha muestra la estimación existente de 283 días y una ventana orientativa de siete días a cada lado; no garantiza el día del nacimiento ni que todos los partos ocurran dentro de esa ventana. La [Universidad de Nebraska–Lincoln](https://beef.unl.edu/faq/pregnant-cows/) describe 283 días como referencia común y señala variación por raza y sexo de la cría; el margen de la aplicación es una ayuda de planificación. No se añadieron fórmulas lunares ni diagnósticos veterinarios.
5. **Movimientos de existencias.** Entradas, consumos, ajustes y retiros conservan cantidad, saldo, unidad, fecha, motivo y responsable. Se consultan también después de retirar un producto. Medicamentos e insumos permiten configurar mínimos y caducidad; los productos que requieren revisión aparecen antes del historial. Las aplicaciones de salud y el consumo de almacén son registros separados para evitar descuentos implícitos o duplicados.
6. **Estadísticas.** Reportes incluye periodo y lote actual, ganado activo, peso promedio actual, nacimientos conocidos por mes, partos, aplicaciones y frecuencia de enfermedades. El peso por animal incluye gráfica y valores accesibles. El consumo se suma por unidad, sin mezclar kg, frascos u otras unidades, y se identifica como consumo general del rancho. El filtro por lote utiliza la asignación actual, no reconstruye ubicaciones históricas.
7. **Borradores persistentes.** Altas y edición de animales, lotes y formularios compartidos conservan campos fuera del estado de la Activity. Los borradores se separan por cuenta, pantalla y registro cuando corresponde; se pueden descartar. Las contraseñas no se persisten como borradores. Las fotografías elegidas solicitan persistir el permiso de lectura cuando el proveedor lo permite. Los borradores se recuperan al volver a abrir el formulario correspondiente.
8. **Coherencia entre eventos.** Salud y pesajes comprueban el nacimiento; una edición del nacimiento respeta los eventos existentes. Se evita cambiar a macho una madre con crías o una gestación activa, y dar de baja con una gestación abierta. Un parto respeta su monta, su madre y los aretes de las crías; no se permite duplicar un parto de la misma madre en el día. La baja conserva fecha y motivo. Los movimientos de almacén se registran junto con el cambio de saldo, dentro de transacciones.

## Apariencia y facilidad de uso

- Inicio presenta pendientes y próximos eventos antes del clima, con acceso al calendario y alertas de productos.
- Ganado incluye búsqueda y filtros combinables de categoría, sexo, lote, estado, edad y enfermedad activa.
- Los nacimientos rápidos pendientes tienen un acceso visible en Inicio/Ganado. Se pueden completar o cancelar con confirmación; al terminar el último pendiente, el listado vuelve a los animales activos.
- La ficha separa información principal, acciones, reproducción y la línea de eventos.
- Hay primeros pasos cuando el rancho está vacío y una guía permanente desde Más/Configuración.
- Las pantallas nuevas utilizan superficies del tema, campos con etiquetas, encabezados accesibles, texto adaptable y navegación con descripciones. Los errores compartidos se anuncian mediante una región accesible. Las gráficas tienen descripción y valores textuales.
- Los iconos de las barras del sistema cambian de contraste con el tema. Las tarjetas nuevas usan los colores de superficie de la paleta verde.

## Conservación de datos

La base pasa de Room 12 a 13 con una migración aditiva. Los saldos previos se identifican como saldo inicial al actualizar. El peso previo se identifica como referencia disponible al actualizar, sin inventar pesajes anteriores. No se reconstruyen eventos que la versión anterior nunca guardó.

La copia automática de Android conserva únicamente la preferencia de apariencia. Para llevar registros y fotografías a otro teléfono se utiliza el archivo portable cifrado; las sesiones y borradores no se trasladan con ese archivo. Tras restaurar se exige volver a iniciar sesión.

## Verificación

Resultado final: **14 pruebas unitarias y 40 pruebas Android aprobadas**, compilación debug correcta y lint con **0 errores y 33 advertencias**. La batería Android completa terminó con `OK (40 tests)` en 136,81 segundos sobre el APK de entrega.

Las comprobaciones incluyen transacciones de partos, parentesco, cronología, pesajes, movimientos concurrentes, cifrado y restauración de fotografías, migraciones de las estructuras recientes, búsqueda, borradores sin contraseñas, calendario, estadísticas y acceso/cancelación de nacimientos rápidos pendientes. Las capturas se generaron desde los componentes reales durante las pruebas, con datos ficticios y texto al 130 % en una pantalla de 360 × 640 dp.

Registros locales, fuera de Git: `build/validaciones/avance03-pendientes-y-temas.log` y `build/validaciones/avance03-android-40.log`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

- [Ganado y búsqueda en oscuro](validaciones/capturas-avance03/ganado-busqueda-oscuro.png).
- [Ficha e historial en oscuro](validaciones/capturas-avance03/ficha-historial-oscuro.png).
- [Primeros pasos](validaciones/capturas-avance03/primeros-pasos-oscuro.png).
- [Calendario](validaciones/capturas-avance03/calendario-oscuro.png).
- [Estadísticas en oscuro](validaciones/capturas-avance03/estadisticas-oscuro.png).
- [Las mismas estadísticas en claro](validaciones/capturas-avance03/estadisticas-claro.png).

Las comprobaciones se realizan en un emulador aislado con datos ficticios. La validación en teléfonos físicos, versiones anteriores de Android, TalkBack completo y grandes volúmenes de fotografías sigue pendiente. El envío diario depende de los permisos y restricciones de batería del dispositivo.

Referencias de implementación: [alarmas de Android](https://developer.android.com/develop/background-work/services/alarms?hl=en), [accesibilidad en Compose](https://developer.android.com/develop/ui/compose/accessibility/api-defaults?hl=en).
