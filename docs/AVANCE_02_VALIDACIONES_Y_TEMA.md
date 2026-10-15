# Validaciones y apariencia de MiRanchoPro

Fecha: 14 de octubre de 2026.

Continuación: [historiales, respaldos, calendario y mejoras de uso](AVANCE_03_OPERACION_DEL_RANCHO.md). Los pendientes de este informe describen el estado anterior a ese bloque.

Se implementaron las correcciones de formularios y consistencia descritas abajo, junto con la revisión del contraste en modo oscuro. El informe original se conserva como diagnóstico histórico en [REVISION_VALIDACIONES_Y_DISENO.md](REVISION_VALIDACIONES_Y_DISENO.md).

## Apariencia

- Las tarjetas compartidas de Inventario y Salud usan superficies del tema, bordes y colores de texto compatibles. Se retiró el fondo blanco fijo que hacía ilegibles sus títulos al activar el modo oscuro.
- Las tarjetas de pendientes conservan su acento de advertencia mediante un fondo oscuro matizado; sus textos mantienen el contraste de la paleta.
- Se adaptaron búsquedas, campos, listas, ventanas de selección, iconos y divisores. La barra inferior usa colores distintos para selección, contenido y fondo en cada tema.
- Los temas claro, oscuro y del teléfono se pueden elegir en Configuración; la elección se conserva al cerrar y abrir la aplicación.
- Se unificó la marca Mi Rancho PRO y la navegación en Inicio, Ganado, Salud, Tareas y Más.
- Los formularios largos usan un área desplazable que descuenta barras del sistema y teclado. Se revisó también texto al 130 % en una pantalla de 360 × 640 dp.
- El alta de animales comienza con identificación y datos; la fotografía opcional es más compacta. Se muestran ejemplos, unidades y errores de los campos.
- Perfil utiliza la cuenta real y permite cambiar su contraseña. Se retiraron los datos ficticios y los interruptores de ajustes que no hacían nada.
- Reportes muestra un mensaje y una acción cuando no hay animales activos. El clima permite elegir una ciudad y reintentar ante un fallo.

## Validaciones y operaciones

- Validación compartida de correos, pesos, decimales con coma o punto y fechas completas. Se rechazan negativos, valores no finitos y fechas imposibles; el peso inválido no se sustituye por cero.
- Un nacimiento desconocido permanece desconocido. La edad mostrada se calcula desde el nacimiento, respetando el día del mes. El registro ofrece sexo y categorías compatibles, incluido Becerra.
- Los errores aparecen en los formularios. Se bloquea el envío mientras se guarda y los diálogos se cierran después de confirmar el guardado, conservando sus datos ante un fallo.
- Consumo y abastecimiento modifican existencias de forma atómica; no admiten cantidades negativas ni consumos superiores al saldo.
- La ocupación de lotes se obtiene de los animales activos. Traslados, cambios de nombre, comprobación de capacidad y eliminación se realizan mediante transacciones. Los diálogos de editar y mover ya están conectados.
- La gestación valida animal, categoría, estado y fecha, e impide registrar otra activa en la misma operación. Se puede marcar como finalizada o interrumpida conservando su historial.
- Salud selecciona animales existentes y valida fechas, dosis aplicadas y orden de próximas aplicaciones. Medicamentos comprueba existencias enteras, unidades y duplicados.
- Tareas permite seleccionar al dueño como responsable y conserva la fecha elegida en el calendario sin desplazarla por la zona horaria. Al eliminar un empleado, sus tareas se reasignan al dueño.
- Reportes e Inicio cuentan animales activos; el promedio del reporte excluye pesos inválidos y registros pendientes o de baja.
- La edición de fotografías guarda un archivo nuevo y conserva el anterior si falla la actualización.

## Cuenta y permisos locales

- La sesión se restaura por cuenta y se limpia al salir. El registro exitoso asigna el usuario antes de abrir Inicio.
- La administración de empleados y lotes exige el rol de dueño; las operaciones de tareas verifican permisos y responsables.
- Las contraseñas nuevas se guardan como derivaciones con sal aleatoria. Las credenciales antiguas se convierten al abrir la base, conservando el acceso. Se usa PBKDF2-HMAC-SHA256 con 600 000 iteraciones cuando está disponible y un respaldo compatible para Android 24/25. Las decisiones se contrastaron con la [guía de almacenamiento de contraseñas de OWASP](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html) y la [disponibilidad de algoritmos en Android](https://developer.android.com/reference/javax/crypto/SecretKeyFactory).
- La edición de empleados nunca carga su contraseña en el formulario. Un cambio de contraseña propia exige verificar la anterior.

## Verificación

- 10 pruebas unitarias aprobadas: 9 de validación/contraseñas y la prueba existente.
- 24 pruebas Android aprobadas en el emulador aislado Android 16/API 36: migraciones recientes, nacimientos, fotografías, existencias, traslados, capacidad, duplicados y gestaciones concurrentes.
- Compilación debug correcta; lint terminó con **0 errores y 26 advertencias** de dependencias, recursos y estilo. No se actualizaron dependencias para ocultar esos avisos.
- Los recorridos visuales usan una cuenta y ganado ficticios. Se compararon temas claro/oscuro y se comprobó que la elección de apariencia sobrevive al reinicio.

Comandos de verificación, con JAVA_HOME apuntando al JDK 17 instalado:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest :app:lintDebug --offline --no-daemon --max-workers=2
adb -s emulator-5582 shell am instrument -w -r com.equipo.miranchopro.test/androidx.test.runner.AndroidJUnitRunner
```

Las últimas comprobaciones de apariencia se compilaron y revisaron nuevamente con `:app:assembleDebug :app:lintDebug`. Los registros completos quedan en `build/validaciones`, que no se versiona.

## Capturas comprobadas

- [Inventario en modo oscuro](validaciones/capturas/inventario-oscuro.png).
- [La misma pantalla en modo claro](validaciones/capturas/inventario-claro.png).
- [Salud en modo oscuro, con las tarjetas unificadas](validaciones/capturas/salud-oscuro.png).
- [Formulario oscuro con teclado y texto al 130 %; Guardar accesible](validaciones/capturas/formulario-teclado.png).

## Alcance pendiente

Este avance trabaja sobre el rancho local existente. La separación de varios ranchos, identificadores estables para sincronización, colaboración entre teléfonos, recuperación del dueño por correo, respaldo/restauración manual y cobros Pro requieren las etapas del [plan general](../PLAN_DE_MEJORAS.md). No se presentan como funciones terminadas.

El sexo y las relaciones actuales de lotes se resuelven sin cambiar el esquema de Room 12; la normalización de esos datos para sincronización queda pendiente. También faltan pruebas en teléfonos físicos, Android anteriores, TalkBack completo y borradores tras terminar el proceso de la aplicación.
