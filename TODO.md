# 🚜 GL Vehicles - Hoja de Ruta y Errores

## 🛠️ Tareas Pendientes (Backlog)

### 🏎️ Físicas y Conducción
- [ ] **Inercia Realista:** Ajustar aceleraciones y deceleraciones para que sean mucho más lentas y fluidas (evitar cambios bruscos).
- [ ] **Suavizado de Movimiento:** Investigar y corregir los "trompicones" (posible interpolación de red o friction).
- [x] **Daño por Colisión:** El vehículo recibe daño al chocar contra bloques sólidos a alta velocidad.
- [x] **Dashboard en HUD:** Implementar Velocímetro y Cuenta-revoluciones (RPM) visuales.

### 🧩 Mecánicas y Componentes
- [ ] **Restricciones de Slots (Ampliación):** Prohibir meter objetos no válidos en los slots de Accesorios (solo deben aceptar Attachments).
- [ ] **Persistencia de Ítems:** Corregir el bug donde las piezas se descolocan o desaparecen al reiniciar el mundo.
- [ ] **Visualización de Estadísticas:** Ver (Grip, Aceleración, Peso, etc.) en la GUI o al mirar el vehículo.
- [ ] **Sistema de Pintura:** Click derecho con tintes para cambiar el color del chasis.
- [ ] **Alertas de Combustible:** Añadir mensaje en actionbar mientras se llena el depósito en la Gas Pump.
- [ ] **Partículas de Desgaste:** Humo negro saliendo del motor cuando el `Wear` sea > 80%.
- [ ] **Luces:** Huesos funcionales para faros que iluminen de noche.

### 🏗️ Progresión y Mundo
- [ ] **Mesa de Trabajo Especial:** 
    - [x] Registro de bloque y ScreenHandler (GUI base lista).
    - [ ] **Recetas de Fabricación:** Implementar la lógica de crafteo real (materiales -> ítems).
    - [ ] **Visuales:** Modelo y textura de la mesa.
- [ ] **Crafteos:** Definir recetas para todos los componentes una vez esté la mesa.

### 🚜 Accesorios (Attachments)
- [ ] **Harvester, Trailer, Seeder:**
    - [x] Registro de ítems (Iconos generados).
    - [ ] **Lógica Funcional:** (En proceso por Antigravity: cosecha, inventario extra y siembra).
    - [ ] **Visuales GeckoLib:** Huesos dinámicos en el modelo del tractor.
    - [ ] **HUD de Carga:** Overlay que muestre la capacidad del remolque cuando esté enganchado.

### 🏗️ Automatización y Silos
- [x] **Rejilla de Descarga:** Bloque de suelo funcional para vaciado automático.
- [x] **Tubo de Sinfín (Auger):** Traslado de ítems direccional implementado.
- [ ] **Tanques de Almacenamiento:** Bloques de metal corrugado (Silo Wall registrado, falta lógica de capacidad).
- [ ] **Indicador de Gas Pump:** Forma visual o mediante mensaje de ver el combustible restante en el surtidor.
- [ ] **Animación visual:** Añadir texturas y modelos específicos para los tubos y rejillas.

---

## 🎨 Apartado Visual y Arte
- [ ] **Posición del Jugador:** Ajustar altura final (se ha bajado a 0.35D, verificar si es suficiente).
- [ ] **Alineación de Piezas:** Corregir pivotes del modelo `gl_tractor.geo.json` (actualmente en 0,0,0).
- [ ] **Texturizado de Ítems:** Verificar nuevas texturas generadas (Wrench, Ruedas, Fuel Can, Kart e iconos).
- [ ] **Sonidos Dinámicos:** Sonido de motor (Idle y Aceleración con Pitch variable).

---

## ✅ Completado
- [x] Motor de físicas avanzada (Drag, Brake, Steering, Peso de Tractor).
- [x] Sincronización de Rotación (Yaw) manual en Renderer e interpolación.
- [x] Barra de combustible vertical a la derecha (HUD).
- [x] Surtidor de gasolina funcional (Gas Pump).
- [x] Modelo base placeholder y textura blanca para tinte.
- [x] Integración GeckoLib 4.
- [x] Sistema de slots dinámicos por vehículo.
- [x] Sistema de desgaste (Wear) persistente en NBT.
- [x] Bloqueo de movimiento si faltan componentes.
- [x] **Alineación GUI:** El inventario del jugador ya coincide con la textura.
- [x] **Restricciones de Slots (Base):** Motor y Ruedas ya filtran correctamente sus ítems.
- [x] **Pestaña Creativo:** Grupo GL_VEHICLES creado con todos los ítems.
- [x] **Limpieza:** Eliminado `gasoline_bucket`.
- [x] **Kart:** Modelo y físicas de carreras implementados (corregida textura y giro).
