# Plan de mejoras de MiRanchoPro

Preparado el 14 de octubre de 2026 a partir de la revisión del código y de los objetivos del propietario del proyecto.

Objetivo: publicar una aplicación Android útil y estable en Google Play, con una modalidad Gratis y una modalidad Pro para colaboración entre dispositivos, estadísticas y seguimiento del ganado.

Este documento conserva la planificación original. El usuario autorizó comenzar la implementación el 14 de octubre de 2026. Las casillas no reflejan todos los avances posteriores: consulta [validaciones y tema](docs/AVANCE_02_VALIDACIONES_Y_TEMA.md) y [operación del rancho, respaldos e historiales](docs/AVANCE_03_OPERACION_DEL_RANCHO.md) para conocer el estado actual. Los límites comerciales aún son propuestas. No es necesario completar todas las ampliaciones antes de la primera publicación.

## Avance del primer bloque — 14 de octubre de 2026

- Compilación debug y APK de pruebas correctos, con Room 2.8.5, Kotlin 2.2.10 y KSP 2.2.10-2.0.2.
- Esquemas 11 y 12 exportados y migraciones verificadas desde las estructuras recientes de las versiones 9, 10 y 11. Las versiones anteriores sin migración se rechazan conservando sus datos; todavía requieren una migración específica para abrirse con esta app.
- Nacimientos pendientes completados mediante transacción; un arete duplicado, un pendiente inexistente o un segundo intento no elimina ni duplica registros.
- Identificadores de nacimientos rápidos basados en UUID.
- Fotografías nuevas del formulario de registro guardadas en archivos independientes y fuera del hilo de interfaz. Los fallos conservan el registro pendiente y la fotografía anterior. La edición de fotografías todavía requiere adaptar su flujo al mismo almacenamiento.
- 16 pruebas Android aprobadas en un emulador aislado Android 16/API 36: 15 pruebas nuevas y la prueba de contexto existente. También pasó la prueba unitaria existente.
- Respaldo/restauración manual, autenticación, lotes, otros formularios y las siguientes etapas continúan pendientes.
- Detalles y comandos: [avance del primer bloque](docs/AVANCE_01_DATOS.md).

## 0. Decisiones del producto

- [ ] D01. Confirmar el alcance de la primera publicación: funciones individuales completas y, si Pro se ofrece desde el lanzamiento, colaboración y cobro completamente funcionales.
- [ ] D02. Confirmar los límites de Gratis y Pro. Propuesta inicial, todavía sin aprobar: Gratis para un dueño en un teléfono; Pro para un dueño y hasta cuatro empleados, con hasta dos dispositivos por persona.
- [ ] D03. Confirmar qué funciones pertenecen a cada plan: registro básico, reproducción, recordatorios, estadísticas, exportación, respaldo y colaboración.
- [ ] D04. Definir precio, periodicidad y posible prueba de Pro después de estimar costos de almacenamiento, sincronización, soporte y comisiones aplicables.
- [ ] D05. Decidir si Gratis podrá utilizarse completamente sin cuenta en línea y cómo se trasladarán sus datos al activar Pro.
- [ ] D06. Definir los permisos del dueño y los empleados, y si se necesita también un administrador delegado.
- [ ] D07. Definir conservación, consulta, exportación y límites de almacenamiento cuando termine Pro; incluir el procedimiento para reemplazar un teléfono.

## 1. Estabilidad y organización del proyecto

- [x] B01. Compilar el estado actual y registrar errores reales antes de modificarlo; preservar los cambios existentes de animales y embarazos.
- [x] B02. Revisar las dependencias y usar versiones estables compatibles; sustituida la versión alpha de Room y alineados Kotlin, KSP y el destino JVM.
- [ ] B03. Unificar progresivamente la estructura de pantallas, temas y ViewModels; retirar archivos vacíos o implementaciones antiguas solo después de verificar sus referencias.
- [ ] B04. Centralizar validación, tratamiento de fechas, errores y resultados de operaciones.
- [ ] B05. Manejar fallos de base de datos y archivos; asegurar que los indicadores de carga se restablezcan y que guardar repetidamente no duplique operaciones.
- [ ] B06. Separar las herramientas de simulación de la versión pública y actualizar la documentación para que describa lo realmente implementado.

## 2. Protección y estructura de los datos

- [x] DB01. Sustituir el borrado automático como solución de actualización por migraciones que conserven registros; implementadas para las estructuras recientes 9, 10 y 11.
- [x] DB02. Exportar y versionar los esquemas de Room y probar las migraciones desde las versiones que se decida soportar; guardados los esquemas 11/12 y verificadas las migraciones recientes.
- [ ] DB03. Crear respaldo manual y restauración verificable de registros y fotografías, con versión del formato y validación de integridad.
- [ ] DB04. Ajustar las reglas de copia de Android para incluir lo necesario y excluir credenciales y sesiones; comprobar la restauración en otro dispositivo.
- [ ] DB05. Introducir identificadores internos estables para sincronización; mantener el arete como identificador de negocio único dentro del rancho.
- [ ] DB06. Relacionar animales, lotes, registros de salud y responsables mediante identificadores, con reglas explícitas al eliminar o archivar entidades.
- [ ] DB07. Normalizar fechas y estados; calcular la edad desde la fecha de nacimiento y distinguir fecha desconocida de fecha de registro.
- [ ] DB08. Usar operaciones atómicas cuando un cambio afecte varios registros: completar nacimientos, trasladar ganado y modificar existencias.

## 3. Autenticación y sesión

- [ ] A01. Eliminar el almacenamiento de contraseñas en texto claro y su comparación directa mediante SQL; elegir una solución adecuada al acceso local y al acceso en línea.
- [ ] A02. Establecer un procedimiento seguro para pasar de los usuarios locales actuales a cuentas en línea, sin reutilizar sus contraseñas almacenadas como credenciales remotas.
- [ ] A03. Conectar el botón de recuperación de contraseña y reemplazar el envío simulado y la exposición del código por el mecanismo real elegido.
- [ ] A04. Normalizar correos y comprobar duplicados, errores y estados de registro e inicio de sesión.
- [ ] A05. Corregir el flujo posterior al registro: actualmente abre Inicio sin establecer usuarioActual.
- [ ] A06. Gestionar la sesión ante recreación de pantallas, cierre del proceso, vencimiento y cierre de sesión; evitar que las pantallas protegidas funcionen con usuarioActual nulo.
- [ ] A07. Limpiar la sesión y los datos de pantalla al salir, y verificar que Atrás o el estado restaurado no permitan acceder a una sesión anterior.
- [ ] A08. Aplicar permisos también en las operaciones de datos y en el servidor cuando exista; ocultar botones por sí solo no será el control de acceso.

## 4. Rancho, empleados y permisos

- [ ] R01. Crear la entidad Rancho y la relación entre rancho, dueño y miembros.
- [ ] R02. Asignar los datos existentes al rancho correspondiente mediante un procedimiento que conserve la información.
- [ ] R03. Aislar todos los registros por rancho; actualmente las consultas generales no separan los animales por cuenta o rancho.
- [ ] R04. Permitir invitar, aceptar, retirar y administrar empleados, cada uno con cuenta propia.
- [ ] R05. Sustituir la edición de contraseñas de empleados por gestión de acceso y recuperación individual.
- [ ] R06. Relacionar tareas y registros con el identificador del empleado, y conservar historial cuando deje el equipo.
- [ ] R07. Adaptar rutas y acciones al rol; revisar especialmente el acceso a Equipo, que actualmente aparece en la navegación general.
- [ ] R08. Registrar quién creó o modificó la información compartida y permitir retirar dispositivos autorizados.

## 5. Inventario y ficha del animal

- [ ] I01. Validar y normalizar aretes; detectar duplicados antes de escribir registros o fotografías.
- [ ] I02. Rechazar pesos inválidos, negativos o no finitos; corregir la edición que actualmente convierte un texto inválido en cero.
- [ ] I03. Validar fechas estrictamente, evitar fechas futuras incompatibles y conservar la opción de nacimiento desconocido.
- [ ] I04. Separar sexo y categoría del animal; corregir la inconsistencia entre Becerro/Becerra y las categorías que habilitan reproducción.
- [ ] I05. Guardar pesajes con fecha como historial, manteniendo también el peso actual.
- [ ] I06. Reunir identificación, lote, fotos, pesajes, salud y reproducción en la ficha del animal.
- [ ] I07. Guardar bajas y sus motivos como eventos propios, sin anexarlos al campo de marcas; definir qué registros se archivan o eliminan.
- [x] I08. Completar un nacimiento pendiente sin perderlo si falla el registro definitivo; implementación atómica verificada con fallos, duplicados y reintentos.
- [x] I09. Reemplazar los identificadores temporales basados en un fragmento de la hora por identificadores sin colisiones prácticas.
- [ ] I10. Mejorar búsqueda y filtros por arete, nombre, sexo, categoría, lote, salud y estado, usando criterios de conteo coherentes.

## 6. Fotografías

- [ ] F01. Guardar y reemplazar imágenes de forma segura, evitando sobrescribir la foto de otro registro antes de confirmar la operación.
- [ ] F02. Generar nombres internos de archivo independientes del texto del arete y limitar las rutas al directorio previsto.
- [ ] F03. Tratar archivos ilegibles, permisos denegados, cancelación de cámara y falta de espacio; conservar la foto previa si falla el reemplazo.
- [ ] F04. Mover copias y procesamiento fuera del hilo de interfaz, reducir tamaño y normalizar orientación.
- [ ] F05. Limpiar archivos temporales y huérfanos de forma controlada, e incluir imágenes en respaldo y restauración.
- [ ] F06. Preparar identificadores y almacenamiento privado de fotografías para la sincronización Pro; no depender de rutas absolutas del teléfono.

## 7. Lotes y movimientos

- [ ] L01. Vincular los animales al identificador del lote en vez de depender de su nombre escrito.
- [ ] L02. Calcular o mantener atómicamente la ocupación a partir de los animales asignados y de los estados acordados.
- [ ] L03. Corregir el traslado que actualmente cambia contadores sin actualizar los animales.
- [ ] L04. Validar capacidad, cantidad, destino distinto al origen y permisos dentro de la operación de traslado.
- [ ] L05. Actualizar correctamente la ocupación al registrar, cambiar de lote, dar de baja o archivar animales.
- [ ] L06. Evitar eliminar lotes ocupados usando los datos reales y manejar renombres sin perder asociaciones.
- [ ] L07. Conservar historial de movimientos y una opción consistente de Sin lote.

## 8. Salud y medicamentos

- [ ] S01. Mantener vacunas y enfermedades vinculadas a animales reales y mostrar su historial en la ficha.
- [ ] S02. Completar las alertas rápidas: permitir asignar animal y registrar el seguimiento; actualmente se crea un texto provisional como arete.
- [ ] S03. Completar edición y seguimiento de vacunaciones y enfermedades, con fechas y estados validados.
- [ ] S04. Registrar movimientos de medicamentos, cantidades, unidades, caducidad y umbral de existencias bajas cuando se incluyan esos datos.
- [ ] S05. Conectar las salidas de inventario de medicamentos con los eventos correspondientes cuando proceda, evitando descontar dos veces.
- [ ] S06. Permitir programar y consultar próximas aplicaciones a partir de los datos registrados y generar recordatorios.
- [ ] S07. Revisar datos de demostración y contenido clínico; evitar presentar dosis de ejemplo como recomendaciones generales.

## 9. Reproducción y partos

- [ ] G01. Mantener registro de servicio por inseminación o monta con fecha conocida, fecha estimada o intervalo de exposición al toro.
- [ ] G02. Admitir la estimación registrada por el veterinario como información diferenciada.
- [ ] G03. Mostrar método, datos utilizados y fecha o ventana probable; validar entradas y evitar convertir fechas inválidas en la fecha actual.
- [ ] G04. Evitar embarazos activos duplicados y comprobar que el animal reúna las condiciones definidas para registrar reproducción.
- [ ] G05. Completar cambios de estado, historial, corrección de registros y cierre del embarazo al registrar el parto.
- [ ] G06. Relacionar madre y cría, permitir registrar varias crías y conservar fecha real y resultado del parto.
- [ ] G07. Crear calendario y recordatorios configurables de los registros reproductivos.
- [ ] G08. Comparar estimación y fecha real con el historial disponible, sin prometer precisión individual no validada.
- [ ] G09. Si se incluye el calendario lunar, identificarlo como referencia tradicional y explicar sus límites; confirmar el método que se desea mostrar antes de implementarlo.

## 10. Insumos y alimentación

- [ ] IN01. Validar cantidades y unidades en alta, abastecimiento y consumo; rechazar valores negativos y no finitos.
- [ ] IN02. Comprobar y descontar existencias de manera atómica, con un resultado visible cuando no haya suficiente stock.
- [ ] IN03. Guardar entradas, salidas y ajustes con fecha y responsable, en vez de conservar solamente el saldo actual.
- [ ] IN04. Asociar consumos al lote o animales cuando sea necesario para calcular estadísticas.
- [ ] IN05. Añadir umbrales configurables y avisos de existencias bajas.
- [ ] IN06. Incluir costos como ampliación opcional si se aprueban reportes económicos; definir unidades y monedas antes de calcularlos.

## 11. Tareas, recordatorios y sensor

- [ ] T01. Relacionar tareas con empleados del rancho mediante identificadores y mostrar solo lo autorizado para cada rol.
- [ ] T02. Validar fecha, responsable y prioridades; conservar autor y cambios de estado.
- [ ] T03. Añadir tareas recurrentes e historial de cumplimiento cuando se apruebe ese alcance.
- [ ] T04. Crear recordatorios persistentes y manejar el permiso de notificaciones y las preferencias del usuario.
- [ ] T05. Hacer configurable el acceso al registro rápido mediante movimiento, limitar repeticiones y suspenderlo cuando la app no esté activa.
- [ ] T06. Mantener acceso manual al registro rápido y aclarar que el movimiento abre una acción del usuario; no detecta por sí mismo un parto o una enfermedad.

## 12. Perfil, configuración y experiencia de uso

- [ ] UX01. Sustituir correo, teléfono y rol de ejemplo del perfil por datos reales y completar su edición.
- [ ] UX02. Guardar preferencias y hacer funcionales el modo oscuro y los controles de notificaciones.
- [ ] UX03. Completar Seguridad y Acerca de; ofrecer Idioma solo si existe el soporte correspondiente.
- [ ] UX04. Unificar tema, componentes, mensajes, formatos y recursos de texto; comprobar accesibilidad y contraste.
- [ ] UX05. Simplificar la navegación según el rol y verificar tamaño de texto, teclado, pantallas pequeñas y barras del sistema.
- [ ] UX06. Conservar formularios ante recreación de pantalla y advertir sobre cambios sin guardar cuando corresponda.
- [ ] UX07. Mostrar estados vacíos, carga, error, reintento y confirmaciones claras; permitir deshacer o confirmar acciones destructivas según el caso.
- [ ] UX08. Añadir una introducción breve para crear el rancho y registrar el primer animal, y un medio de ayuda y contacto.

## 13. Clima

- [ ] C01. Permitir configurar la localidad del rancho y mostrar la fecha de actualización y unidades.
- [ ] C02. Conservar la última consulta útil y distinguir datos anteriores cuando no haya conexión.
- [ ] C03. Manejar tiempos de espera, límites del proveedor y errores, con reintentos razonables y sin consultas repetidas innecesarias.
- [ ] C04. Revisar la clave del servicio integrada en el código y adoptar controles compatibles con el proveedor; cambiarla de archivo no la vuelve secreta dentro de un APK.

## 14. Estadísticas y reportes

- [ ] E01. Definir conteos consistentes de activos, bajas y pendientes entre Inicio, Inventario, Lotes y Reportes.
- [ ] E02. Añadir filtros por período, lote y categoría, usando datos del rancho autorizado.
- [ ] E03. Mostrar evolución de peso a partir de pesajes con fecha.
- [ ] E04. Mostrar nacimientos, bajas y registros reproductivos por período, indicando datos faltantes.
- [ ] E05. Mostrar cumplimiento de tareas y consumos registrados por lote cuando exista información suficiente.
- [ ] E06. Exportar reportes a PDF y CSV; decidir si se necesita además Excel nativo. Actualmente los reportes consultados muestran estadísticas dentro de la app.
- [ ] E07. Corregir errores que actualmente se ignoran al cargar reportes y permitir reintentar.
- [ ] E08. Reservar comparaciones históricas y reportes avanzados para el plan acordado, evitando confundir falta de datos con cero actividad.

## 15. Consejos para el manejo del ganado

- [ ] CO01. Definir los temas de contenido educativo y sus fuentes; incluir autor o revisión, fecha y actualización.
- [ ] CO02. Revisar contenido veterinario con un profesional antes de presentarlo como consejo de salud.
- [ ] CO03. Basar avisos personalizados en registros comprobables y explicar qué dato motivó cada aviso.
- [ ] CO04. Si se aprueba incorporar IA, definir alcance, costo, datos enviados y revisión de respuestas antes de elegir e integrar un servicio.

## 16. Colaboración y sincronización Pro

- [ ] SY01. Elegir servicio remoto y almacenamiento después de comparar costos, permisos, respaldo y necesidades de operación.
- [ ] SY02. Conservar Room para trabajar sin conexión y crear una cola persistente de cambios pendientes.
- [ ] SY03. Sincronizar por rancho registros, archivos, altas, modificaciones y eliminaciones con identificadores estables.
- [ ] SY04. Resolver cambios simultáneos, evitando pérdida silenciosa de información, duplicados y descuentos repetidos de stock.
- [ ] SY05. Reintentar operaciones sin ejecutarlas dos veces; manejar pérdida de conexión, cierre de la app y sesiones vencidas.
- [ ] SY06. Mostrar estado de sincronización y ofrecer resolución o reintento de fallos.
- [ ] SY07. Validar permisos, pertenencia al rancho y límites de plan en el servidor.
- [ ] SY08. Controlar tamaño y cantidad de fotografías, almacenamiento y consumo del servicio; definir retención y restauración.
- [ ] SY09. Permitir instalar en otro teléfono, recuperar datos autorizados y revocar acceso de dispositivos o empleados.

## 17. Gratis, Pro y facturación

- [ ] P01. Mantener una app con funciones desbloqueadas según el plan aprobado y explicar claramente sus límites.
- [ ] P02. Vincular la suscripción al rancho del dueño y definir cómo se recupera en otros dispositivos y cómo se cambia su administración.
- [ ] P03. Integrar Google Play Billing para la compra del acceso digital, conforme a las políticas aplicables.
- [ ] P04. Verificar compras y administrar el estado de la suscripción en el servidor antes de conceder acceso.
- [ ] P05. Manejar compra pendiente, renovación, cancelación, vencimiento, reembolso y períodos de gracia aplicables.
- [ ] P06. Permitir restaurar compras, consultar el plan y administrar la suscripción.
- [ ] P07. Aplicar límites de usuarios y dispositivos con mensajes claros y un mecanismo para reemplazarlos.
- [ ] P08. Aplicar el comportamiento acordado al terminar Pro, preservando los datos y el acceso a consulta/exportación según la política definida.
- [ ] P09. Medir costos y uso para ajustar precios y límites; no publicar la venta de funciones que todavía no funcionen.

## 18. Privacidad y documentación

- [ ] PR01. Redactar la política de privacidad según los datos reales, permisos, proveedores y tratamiento local o remoto, y hacerla accesible en la app y Play Console.
- [ ] PR02. Completar Seguridad de los datos según el funcionamiento efectivo y los SDK integrados.
- [ ] PR03. Añadir eliminación de cuentas en línea y datos asociados, con acceso desde la app y recurso web cuando aplique; las cuentas exclusivamente sin conexión tienen una excepción específica.
- [ ] PR04. Definir qué ocurre con los datos compartidos del rancho al retirar un empleado, eliminar una cuenta o eliminar el rancho.
- [ ] PR05. Documentar condiciones de la suscripción, límites, retención y canales de soporte; solicitar solo permisos necesarios.
- [ ] PR06. Revisar copias, registros técnicos y servicios de diagnóstico para evitar exposición de contraseñas, sesiones o datos privados.

## 19. Pruebas y criterios de aceptación

- [ ] QA01. Reemplazar las pruebas de ejemplo por pruebas de comportamientos críticos y corregir el identificador antiguo esperado por la prueba instrumentada.
- [ ] QA02. Probar migraciones y restauración conservando relaciones y fotografías.
- [ ] QA03. Probar aretes duplicados, pesos y cantidades inválidos, fechas incorrectas y fallo al completar un nacimiento pendiente.
- [ ] QA04. Probar registro, acceso, recuperación, cierre de sesión y permisos de cada rol y rancho.
- [ ] QA05. Probar traslados, ocupación de lotes y movimientos de existencias, incluyendo fallos a mitad de operación.
- [ ] QA06. Probar cámara, galería, permisos denegados, falta de espacio, rotación y cierre del proceso.
- [ ] QA07. Probar sincronización entre celulares, ediciones simultáneas, trabajo sin conexión y reconexión.
- [ ] QA08. Probar compras y todos los cambios relevantes de suscripción con las herramientas de prueba de Google Play.
- [ ] QA09. Probar en dispositivos y versiones Android representativos, revisar rendimiento y comprobar compatibilidad de bibliotecas nativas con páginas de memoria de 16 KB cuando corresponda.
- [ ] QA10. Realizar pruebas con usuarios de ranchos, registrar problemas y validar los límites propuestos de Gratis y Pro.

## 20. Preparación y publicación en Google Play

- [ ] GP01. Confirmar y completar la configuración y verificación de la cuenta de desarrollador.
- [ ] GP02. Elegir identificador definitivo de la aplicación y organizar versionName/versionCode para actualizaciones.
- [ ] GP03. Generar y probar una compilación release; configurar reducción y ofuscación cuando corresponda y comprobar que no rompan la app.
- [ ] GP04. Preparar el Android App Bundle firmado, configurar Play App Signing y proteger la clave de subida fuera del repositorio.
- [ ] GP05. Preparar icono, capturas reales, descripción, categoría, datos de contacto y clasificación de contenido, indicando funciones de pago.
- [ ] GP06. Confirmar requisitos vigentes de API de destino y compatibilidad al enviar; el proyecto actualmente declara targetSdk 36.
- [ ] GP07. Facilitar instrucciones y acceso funcional a los revisores cuando se requiera autenticación.
- [ ] GP08. Ejecutar pruebas internas y cerradas y revisar el informe previo al lanzamiento. Si la cuenta personal se creó después del 13 de noviembre de 2023, comprobar el requisito de 12 participantes durante 14 días continuos antes de solicitar acceso a producción.
- [ ] GP09. Publicar por etapas una versión que cumpla los criterios de aceptación y preparar atención a fallos y actualizaciones.

## Orden propuesto

1. Acordar el alcance y los límites provisionales del producto (D).
2. Compilar y asegurar datos: estabilidad, migraciones, respaldos y operaciones sin pérdida (B, DB, I08, L, IN).
3. Preparar la estructura de rancho, identidad y permisos (A, R).
4. Cerrar las funciones individuales y experiencia de uso que se incluyan en la primera versión (I, F, S, G básico, T, UX, C, E básico).
5. Probar y preparar la publicación de Gratis si se decide lanzarla primero (QA, PR, GP).
6. Construir colaboración Pro y facturación antes de ofrecerlas al público (SY, P, QA, PR).
7. Ampliar estadísticas, reproducción, contenido y funciones opcionales según resultados de usuarios (G avanzado, E avanzado, CO).

Criterio de avance: cada bloque debe demostrar su comportamiento con verificaciones pertinentes antes de comenzar el siguiente bloque dependiente. Las pruebas continuarán durante todas las etapas.

## Fuentes de requisitos y referencias consultadas

- [Google Play: política de privacidad y Seguridad de los datos](https://support.google.com/googleplay/android-developer/answer/10144311?hl=es).
- [Google Play: eliminación de cuentas y excepción para cuentas sin conexión](https://support.google.com/googleplay/android-developer/answer/13327111?hl=es).
- [Google Play: pruebas para nuevas cuentas personales](https://support.google.com/googleplay/android-developer/answer/14151465?hl=es).
- [Google Play: nivel de API de destino](https://support.google.com/googleplay/android-developer/answer/11926878?hl=es-419).
- [Google Play: política de pagos](https://support.google.com/googleplay/android-developer/answer/9858738?hl=en).
- [Android Developers: verificación de compras](https://developer.android.com/google/play/billing/security).
- [Android Developers: publicación del paquete firmado](https://developer.android.com/studio/publish/upload-bundle).
- [Android Developers: compatibilidad con páginas de memoria de 16 KB](https://developer.android.com/guide/practices/page-sizes).
- [Mississippi State University Extension: estimación de parto a partir del servicio](https://extension.msstate.edu/publications/calving-season-selection-considerations).
- [Investigación de ciclo lunar en 428 partos](https://pubmed.ncbi.nlm.nih.gov/27580019/).
- [Investigación de ciclo lunar y clima en más de dos millones de partos](https://pubmed.ncbi.nlm.nih.gov/26848114/).
