# GL Vehicles - Fabric 1.20.1

Este mod implementa vehículos personalizables con sistemas de combustible realista y accesorios para tractores.

## Características:
- **Tractor**: Puede usar accesorios (Plow, Harvester) en el slot 0.
- **Personalización**: GUI dinámica para cambiar Motores y Ruedas.
- **Sistema de Combustible**: Gasolina y Diesel. Repostaje mediante Gas Pumps o Bidones (Fuel Cans).
- **Control**: Movimiento fluido basado en paquetes C2S.

## Estructura de Clases:
- `AbstractVehicleEntity`: Lógica base de movimiento, combustible e inventario.
- `TractorEntity`: Lógica específica para accesorios de granja.
- `VehicleInputC2SPacket`: Sincronización de teclado (WASD) entre cliente y servidor.
- `GasPumpBlock`: Bloque para repostar vehículos cercanos.

## Cómo implementar con Antigravity:
1. Abre esta carpeta en Antigravity IDE.
2. Usa `./gradlew build` para compilar.
3. Los modelos JSON deben ir en `src/main/resources/assets/gl_vehicles/models/entity/`.
4. Las texturas en `src/main/resources/assets/gl_vehicles/textures/entity/`.

## Lógica Pendiente:
- Registrar los EntityRenderers en `GLVehiclesClient`.
- Implementar la GUI de ScreenHandler en `VehicleScreenHandler`.
- Añadir recetas en `data/gl_vehicles/recipes/`.
