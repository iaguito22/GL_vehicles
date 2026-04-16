# 🏎️ GL Vehicles - Fabric 1.20.1

Implementación de vehículos premium con motor de físicas **v2.0**, sistemas de combustible realista y accesorios industriales.

## 📖 Manual del Conductor (Físicas v2.0)

¡Bienvenido a la nueva era de conducción en Minecraft! GL Vehicles ahora cuenta con un manejo premium diseñado para la máxima respuesta y diversión.

### 1. 🎮 Controles de Conducción
Dominar tu vehículo es intuitivo pero profundo.
- **W/S/A/D**: Los controles clásicos para acelerar, frenar y girar.
- **Sistema de Marchas Automático**: El vehículo detecta tu intención: arrancará suavemente al acelerar y entrará automáticamente en modo **Parking (P)** cuando te detengas o desciendas, asegurando seguridad total.

### 2. 💨 Sistema de Derrape (Drift Premium)
Siente la adrenalina con nuestro sistema de drift estilo arcade.
- **Entrada**: A velocidad, mantén **ESPACIO + Dirección (A/D)** para romper la tracción.
- **Mantenimiento**: El coche bloqueará la dirección de derrape. Puedes soltar las teclas laterales y el coche seguirá deslizando.
- **Control Fino**: Usa la dirección **contraria** al giro para abrir la curva o la **misma** dirección para cerrarla agresivamente.
- **Salida**: Suelta **ESPACIO** para recuperar el agarre instantáneamente.

### 3. 🔧 Mantenimiento y Desguace
- **Diagnóstico**: Haz **Click Derecho** con la **Llave Inglesa (Wrench)** para gestionar componentes (Motores y Ruedas) y ver estadísticas.
- **Desguace**: Realiza un **Click Izquierdo (Ataque) doble** con la Llave Inglesa para desmantelar la unidad y recuperar todas sus piezas.

### 4. ⛽ Gestión de Combustible
- **El Surtidor (Gas Pump)**: Haz **Click Derecho** con un Bidón (Fuel Can) para cargar el surtidor.
- **Gestión de Bidones**: Haz **Shift + Click Derecho** con un bidón en el surtidor para extraer combustible y llevarlo contigo.
- **Repostaje Inteligente**: Los vehículos se repuestan automáticamente si están detenidos cerca de un surtidor con combustible.

### 5. 📊 Estadísticas Dinámicas
En la interfaz verás barras de **Potencia**, **Agarre** y **Velocidad**. Estos valores cambian dinámicamente según el motor y las ruedas instaladas. ¡Optimiza tu configuración para cada terreno!

---

## 🛠️ Estructura Técnica:
- `AbstractVehicleEntity`: Núcleo del motor de físicas v2.0, gestión de inercia y daños.
- `TractorEntity`: Lógica avanzada de trabajo (cosecha y siembra automática).
- `VehicleInputC2SPacket`: Sincronización de inputs de baja latencia.
- `GasPumpBlock`: Entidad de bloque para gestión de fluidos y repostaje.

## 🚀 Implementación:
1. Usa `./gradlew build` para compilar el mod.
2. Los modelos JSON se ubican en `src/main/resources/assets/gl_vehicles/models/entity/`.
3. Las texturas en `src/main/resources/assets/gl_vehicles/textures/entity/`.
