# APK del Analizador de Viajes — SIN Android Studio

**Objetivo:** compilar `app-debug.apk` desde el navegador, en servidores de GitHub Actions. No requiere Gradle, Java o SDK en tu computador.

## A. Preparar una cuenta
1. Abre https://github.com/ y accede a tu cuenta. Si no tienes, crea una.
2. Crea un repositorio en https://github.com/new con el nombre `analizador-viajes-vivo`.
3. Selecciona **Private**. Deja **Add a README file** desactivado y crea el repositorio.

## B. Subir el proyecto
4. Descomprime `Analizador_Vivo_APK_Compilacion_En_Nube.zip` en Windows.
5. Abre la carpeta que contiene **`app`, `tests`, `.github`, `settings.gradle` y `build.gradle`**.
6. Desde el repositorio vacío de GitHub, elige **uploading an existing file** (o `Add file` > `Upload files`).
7. En el Explorador de Windows, selecciona **todos los archivos y carpetas DENTRO** de esa carpeta (`Ctrl+A`) y arrástralos al área de carga de GitHub. No subas el ZIP cerrado ni la carpeta exterior como una carpeta adicional.
8. Verifica que GitHub muestre `.github/workflows/generar-apk.yml`, `app/build.gradle` y `settings.gradle` en las rutas correctas.
9. Pulsa **Commit changes** para subirlos a la rama `main`.

## C. Generar y descargar la APK
10. Abre la pestaña **Actions** del repositorio. Si GitHub muestra una opción para habilitar los workflows, habilítalos para este repositorio.
11. Selecciona el flujo **Generar APK Android**. La primera ejecución puede iniciarse automáticamente al cargar los archivos; si no, pulsa **Run workflow** > **Run workflow** con la rama `main`.
12. Una ejecución con marca verde significa que la compilación terminó bien. Entra en esa ejecución.
13. Baja a **Artifacts** y descarga `Analizador-Vivo-V2206-Android14-APK`.
14. Extrae ese ZIP: en su interior debe aparecer **`app-debug.apk`**. Copia este archivo a tu Vivo V2206 e instálalo desde Archivos, autorizando la instalación desde esa fuente solo para esta prueba.

## Si falla la compilación
Abre la ejecución roja en Actions > `android-apk`, copia las últimas líneas del primer paso fallido y compártelas para corregir el proyecto. No se ha compilado este APK en el entorno de preparación.

## Parámetros iniciales
- Combustible $140.000 COP / 8,4 galones; 40 km/galón.
- Recorrido anual 17.000 km.
- SOAT $750.000/año, seguro adicional $89.000/año, tecnomecánica $400.000/año.
- Llantas $650.000/2 años; mantenimiento $2.000.000/año.
- Comisión inDrive 10,5 %; metas mínimas ilustrativas editables.

## Seguridad y limitaciones
Este programa es una app independiente. Usa captura con autorización de Android y reconocimiento OCR local. No existe una integración oficial autorizada con inDrive; el OCR y el indicador flotante siguen siendo experimentales y deben probarse en tu Vivo. No acepta viajes automáticamente. La APK de depuración es para pruebas personales; no publicar ni distribuir como versión final.
