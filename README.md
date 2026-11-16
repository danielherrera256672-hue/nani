# Analizador de Viajes Vivo — compilar en la nube

Este proyecto fuente está listo para **intentar generar una APK de prueba mediante GitHub Actions**, sin instalar Android Studio ni Gradle en el computador.

**Empieza por `INSTRUCCIONES_GITHUB.md`.**

Usa Android SDK 35, Android Gradle Plugin 8.6.1, Gradle 8.7 y JDK 17. El workflow está en `.github/workflows/generar-apk.yml` y publica `app-debug.apk` en Artifacts tras un build exitoso.

Incluye: ventana flotante, OCR local con autorización para captura, campos editables y simulación de utilidad de las ofertas. La app no interactúa con los botones de inDrive ni acepta viajes de forma automática. No se han realizado pruebas sobre un teléfono real ni compilación completa en este entorno.

**Importante:** Si GitHub Actions falla, revisar el registro de error: el proyecto todavía puede requerir correcciones.
