# Primer avance: compilación y conservación de datos

Fecha: 14 de octubre de 2026.

## Cambios implementados

1. Se comprobó que el proyecto original compilaba y se preservaron sus cambios en curso de animales y embarazos.
2. Se sustituyó Room 2.7.0-alpha11 por Room 2.8.5, se centralizaron sus dependencias y se alinearon Kotlin/Compose 2.2.10, KSP 2.2.10-2.0.2 y el destino JVM 11. Se incorporó el plugin de Room para guardar esquemas y utilizarlos en pruebas.
3. La base pasó a versión 12. Se guardó el esquema 11 antes de modificar el modelo y se exportó el esquema 12. Las migraciones 9→10→11→12 añaden fecha de nacimiento, tabla de embarazos e índice de vacaId conservando filas existentes. La versión de desarrollo 10 admite las variantes comprobadas con/sin fecha de nacimiento y con una tabla de embarazos ya existente.
4. Se retiró fallbackToDestructiveMigration(). Si no existe una ruta compatible, Room rechaza la apertura conservando el archivo; no lo sustituye por una base vacía.
5. Completar un nacimiento pendiente ahora borra el temporal e inserta el definitivo dentro de una transacción. Ante un arete duplicado se revierte el borrado. Se comprueba que el registro de origen siga pendiente y que el arete definitivo sea diferente.
6. Los nacimientos rápidos utilizan UUID en sus identificadores temporales.
7. El formulario de registro guarda cada fotografía nueva en un archivo independiente, fuera del hilo de interfaz. Una inserción fallida no sobrescribe la foto de otro animal. La limpieza comprueba que el archivo nuevo no haya quedado asociado a un registro confirmado y conserva el archivo si no puede verificarlo.
8. Se corrigió la prueba instrumentada existente que aún esperaba el identificador antiguo com.example.miranchopro.

## Verificación realizada

- `:app:assembleDebug`: correcta.
- `:app:assembleDebugAndroidTest`: correcta.
- `:app:testDebugUnitTest`: correcta; una prueba unitaria existente.
- AndroidJUnitRunner: 16 pruebas correctas sobre Android 16/API 36, en un dispositivo virtual nuevo ubicado en build/verification, independiente de los datos del usuario.
- Revisión del diff de los archivos de este bloque: sin errores de espacios.

Las 15 pruebas Android nuevas cubren:

- Conservación de registros de las ocho tablas existentes al migrar desde la estructura de versión 9, incluyendo peso, hora, fecha de registro y ruta de foto.
- Conservación de fecha de nacimiento, embarazos e índice al actualizar las estructuras recientes.
- Rechazo de una versión sin migración sin borrar sus registros.
- Guardado correcto del nacimiento, arete duplicado, pendiente ausente, intento sobre un animal ordinario y reintento tras un guardado correcto.
- Registro de nacimientos rápidos consecutivos.
- Conservación de imagen y nacimiento ante arete duplicado o fotografía ilegible; copia de fotografía independiente de su archivo de origen.

Comandos ejecutados con JAVA_HOME apuntando al JDK 17 instalado:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:assembleDebugAndroidTest --offline --no-daemon --max-workers=2
```

La ejecución `connectedDebugAndroidTest` de Gradle encontró un componente UTP ausente en la caché offline. Las pruebas se ejecutaron con el ejecutor Android real, mediante ADB, en el emulador aislado:

```powershell
adb -s emulator-5580 install -r app\build\outputs\apk\debug\app-debug.apk
adb -s emulator-5580 install -r app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk
adb -s emulator-5580 shell am instrument -w -r com.equipo.miranchopro.test/androidx.test.runner.AndroidJUnitRunner
```

Resultado final: `OK (16 tests)`. El registro completo quedó en build/verification/android-tests.log, dentro de los archivos de compilación ignorados por Git.

## Límites del avance

- Las bases anteriores a la versión 9 y las estructuras de desarrollo distintas de las comprobadas necesitan su propia migración. Su información se conserva, pero no podrán abrirse hasta incorporar esa ruta.
- Esta etapa añade conservación durante actualizaciones; todavía no incluye la función de respaldo y restauración manual.
- El flujo de edición de fotografías sigue usando su implementación anterior. Compresión, orientación y limpieza general de archivos temporales siguen pendientes.
- Quedan pendientes la validación completa de formularios, autenticación segura, correcciones de lotes y existencias, sincronización, facturación y preparación de publicación.
- No se ha realizado una revisión visual completa ni pruebas en teléfonos físicos. Persisten advertencias de API obsoletas en pantallas ajenas a este bloque.

## Siguiente bloque propuesto

Implementar respaldo y restauración verificable de registros y fotografías, y después corregir validaciones y relaciones antes de introducir rancho, cuentas en línea y permisos.
