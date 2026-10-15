# Secciones de ganado y presentación del registro

Fecha: 14 de octubre de 2026.

El propietario pidió recuperar la presentación anterior de las categorías, los nacimientos pendientes y los detalles visuales de los formularios. Esta etapa conserva las validaciones y el funcionamiento local. Gratis/Pro y la colaboración en línea se retomarán después, según lo anotado en el [plan del proyecto](../PLAN_DE_MEJORAS.md).

## Cambios

- Ganado vuelve a abrir con tarjetas de **Vacas, Toros, Becerros, Becerras, Novillos y Vaquillas**, con sus cantidades de animales activos. Pendientes y bajas no aumentan esos conteos.
- Se conserva la búsqueda por arete, nombre o raza, los filtros combinables, el acceso al listado completo y el histórico de bajas. La flecha y el botón Atrás permiten volver a las secciones.
- La tarjeta destacada de **Nacimientos pendientes** recupera su icono y acento ámbar. Cada registro rápido muestra fecha y hora disponible y permite completar o cancelar con confirmación.
- El botón de registro vuelve a ser circular para que ocupe menos espacio sobre las tarjetas. Desde una categoría envía ese tipo al formulario.
- El registro vuelve a agruparse en tarjetas de fotografía, información principal, nacimiento/ubicación y características físicas. Los encabezados tienen iconos en círculos y los campos incluyen iconos de calendario, arete, peso, origen y ubicación.
- Los formularios compartidos admiten iconos decorativos sin sustituir las etiquetas, errores, enfoque y borradores existentes. La edición de animales también incorpora iconos en nacimiento y peso, y utiliza el fondo del tema.
- Las superficies, textos y acentos respetan los temas claro y oscuro. Los títulos de sección tienen semántica de encabezado; las etiquetas se adaptan a letra ampliada.

## Capturas

Capturas de los componentes reales con datos ficticios, en un emulador de 360 × 640 dp y letra al 130 %:

- [Secciones en oscuro](validaciones/capturas-avance04/ganado-secciones-oscuro.png).
- [Secciones en claro](validaciones/capturas-avance04/ganado-secciones-claro.png).
- [Registro pendiente](validaciones/capturas-avance04/registro-pendiente-oscuro.png).
- [Fotografía y tarjetas del formulario](validaciones/capturas-avance04/registro-foto-oscuro.png).
- [Fecha y ubicación en oscuro](validaciones/capturas-avance04/registro-fecha-oscuro.png).
- [Fecha y ubicación en claro](validaciones/capturas-avance04/registro-fecha-claro.png).

## Alcance

No cambia el esquema de la base de datos ni requiere migraciones. Las comprobaciones cubren navegación por categorías, separación de pendientes/bajas, búsqueda, cancelación de un pendiente, borradores, guardado con decimal de coma y nacimiento desconocido, y conservación de fotografías al fallar o completar un registro. La revisión con teléfonos físicos y TalkBack completo sigue pendiente.

## Verificación

- Compilación debug y APK de pruebas correctos. APK actualizado: `app/build/outputs/apk/debug/app-debug.apk`.
- **14 pruebas unitarias aprobadas**. La revisión de lint realizada durante esta etapa terminó con **0 errores y 33 advertencias**.
- **10 pruebas Android aprobadas** de `FlujosPrioridadesTest` y `RegistroAnimalFotoTest`, con `OK (10 tests)` en 61,211 segundos sobre el APK actualizado. Se usan una base en memoria y datos ficticios; los borradores de prueba se limpian antes y después de cada caso. La navegación de las listas se comprueba desplazando el contenedor hasta los elementos que todavía están fuera de pantalla.
- Capturas revisadas en los temas claro y oscuro, con letra ampliada al 130 %.
- Registros: `build/validaciones/avance04-verificacion.log`, `avance04-boton.log`, `avance04-prueba-final.log` y `avance04-android-aprobado.log`.
